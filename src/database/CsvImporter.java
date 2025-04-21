package database;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class CsvImporter {
    private static final int BATCH_SIZE = 1000;
    private static final int PROGRESS_INTERVAL = 5000;

    private final Connection connection;
    private final Map<String, Integer> airportIds = new HashMap<>();
    private final Map<String, Integer> airlineIds = new HashMap<>();

    // Track records for statistics.
    private int processedRows = 0;
    private int totalRows = 0;
    private int skippedRows = 0;
    private int attemptedRows = 0;
    private int cancelledFlights = 0;
    private int divertedFlights = 0;

    // Track reasons why flights have been skipped.
    private final Map<String, Integer> skipReasons = new HashMap<>();

    // Track unique airlines and airports for report.
    private final Set<String> uniqueAirlines = new HashSet<>();
    private final Set<String> uniqueAirports = new HashSet<>();

    private PrintWriter errorLogWriter;
    private String errorLogPath;

    public CsvImporter(Connection connection) {
        this.connection = connection;
        setupErrorLog();
    }

    private void setupErrorLog() {
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("ddMMyyyy_HHmmss"));
            Path logDir = Paths.get("logs");
            if (!Files.exists(logDir)) {
                Files.createDirectories(logDir);
            }
            errorLogPath = logDir.resolve("import_errors_" + timestamp + ".log").toString();
            errorLogWriter = new PrintWriter(new BufferedWriter(new FileWriter(errorLogPath)));
            logError("Error log started at " + LocalDateTime.now());
            logError("----------------------------------------");
        } catch (IOException e) {
            System.err.println("Failed to create error log file, this should not be happening: " + e.getMessage());
        }
    }

    private void logError(String message) {
        if (errorLogWriter != null) {
            errorLogWriter.println(message);
            errorLogWriter.flush(); // Will catch logs even if the program fails.
        } else {
            System.err.println(message);
        }
    }

    private int countTotalRows(String csvFilePath) throws IOException {
        int count =0;
        try (BufferedReader reader = new BufferedReader(new FileReader(csvFilePath))) {
            reader.readLine(); // We shall skip the first line, we have no need to count the header!

            while (reader.readLine() != null) {
                count++;
            }
        }
        return count;
    }

    private void recordSkipReason(String reason) {
        skipReasons.put(reason, skipReasons.getOrDefault(reason, 0) + 1);
        skippedRows++;
    }

    public void importCsv(String csvFilePath) throws IOException, SQLException {
        System.out.println("Counting total rows in CSV file...");
        totalRows = countTotalRows(csvFilePath);
        System.out.println("Found " + totalRows + " rows to process!");

        try (BufferedReader reader = new BufferedReader(new FileReader(csvFilePath))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new IOException("No header? This CSV file empty!");
            }

            String[] headers = parseCsvLine(headerLine);
            Map<String, Integer> columnMap = mapColumnIndices(headers);

            // Log the detected columns
            logError("Detected columns: " + String.join(", ", headers));

            // Prepare statements for inserting data, we do not want to get hacked!
            PreparedStatement airlineStmt = connection.prepareStatement(
                    "INSERT OR IGNORE INTO Airline (iata_code, name) VALUES (?, ?)"
            );

            PreparedStatement airportStmt = connection.prepareStatement(
                    "INSERT OR IGNORE INTO Airport (iata_code, name) VALUES (?, ?)"
            );

            PreparedStatement flightStmt = connection.prepareStatement(
                    "INSERT INTO Flight (date, airline_code, flight_number, flight_origin, " +
                            "flight_destination, scheduled_departure, actual_departure, " +
                            "scheduled_arrival, actual_arrival, cancelled, cancellation_code, diverted) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    PreparedStatement.RETURN_GENERATED_KEYS
            );

            PreparedStatement delayStmt = connection.prepareStatement(
                    "INSERT INTO Delay_Reason (flight_id, reason, delay_length) VALUES (?, ?, ?)"
            );

            // Process data rows.
            String line;
            int batchCount = 0;
            int lineNumber = 1; // We have already read the header.

            connection.setAutoCommit(false);

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                attemptedRows++;

                try {
                    String[] data = parseCsvLine(line);

                    if (line.trim().isEmpty()) {
                        recordSkipReason("Empty line");
                        logError("Line " + lineNumber + ": Skipped - Empty line");
                        continue;
                    }

                    // Skip rows that don't have enough data.
                    if (data.length < getMinRequiredColumns(columnMap)) {
                        recordSkipReason("Insufficient columns");
                        logError("Line " + lineNumber + ": Skipped - Insufficient columns. Found " + data.length +
                                " columns, needed at least " + getMinRequiredColumns(columnMap) + " so it shall be skipped and logged!");
                        continue;
                    }

                    // Getting values from the CSV row.
                    String flDate = getColumnValue(data, columnMap, "FL_DATE");
                    String airlineName = getColumnValue(data, columnMap, "AIRLINE");
                    String airlineCode = getColumnValue(data, columnMap, "AIRLINE_CODE");
                    String flNumberStr = getColumnValue(data, columnMap, "FL_NUMBER");
                    String origin = getColumnValue(data, columnMap, "ORIGIN");
                    String originCity = getColumnValue(data, columnMap, "ORIGIN_CITY");
                    String dest = getColumnValue(data, columnMap, "DEST");
                    String destCity = getColumnValue(data, columnMap, "DEST_CITY");
                    String crsDepTimeStr = getColumnValue(data, columnMap, "CRS_DEP_TIME");
                    String depTimeStr = getColumnValue(data, columnMap, "DEP_TIME");
                    String crsArrTimeStr = getColumnValue(data, columnMap, "CRS_ARR_TIME");
                    String arrTimeStr = getColumnValue(data, columnMap, "ARR_TIME");
                    String cancelledStr = getColumnValue(data, columnMap, "CANCELLED");
                    String cancellationCode = getColumnValue(data, columnMap, "CANCELLATION_CODE");
                    String divertedStr = getColumnValue(data, columnMap, "DIVERTED");

                    // Standardise the date formats.
                    flDate = flDate.replace("-", "");

                    // Skip if date is missing or invalid, and log it.
                    if (isEmptyOrNull(flDate) || flDate.length() != 8) {
                        recordSkipReason("Invalid date");
                        logError("Line " + lineNumber + ": Skipped - Invalid or missing date: " + flDate);
                        continue;
                    }

                    // Handle missing airline code - try to derive it if possible
                    if (isEmptyOrNull(airlineCode) && !isEmptyOrNull(airlineName)) {
                        // Try to extract from the DOT_CODE or AIRLINE_DOT
                        String airlineDotCode = getColumnValue(data, columnMap, "DOT_CODE");
                        String airlineDot = getColumnValue(data, columnMap, "AIRLINE_DOT");

                        if (!isEmptyOrNull(airlineDot) && airlineDot.contains(":")) {
                            // Format like "Delta Air Lines Inc.: DL", I'm not sure if this is ever the case, but to prevent failure.
                            String[] parts = airlineDot.split(":");
                            if (parts.length > 1) {
                                airlineCode = parts[1].trim();
                            }
                        }

                        if (isEmptyOrNull(airlineCode)) {
                            recordSkipReason("Missing airline code");
                            logError("Line " + lineNumber + ": Skipped - Could not determine airline code");
                            continue;
                        }
                    }

                    // Handle missing origin/dest - required fields.
                    if (isEmptyOrNull(origin) || isEmptyOrNull(dest)) {
                        recordSkipReason("Missing origin/destination");
                        logError("Line " + lineNumber + ": Skipped - Missing origin or destination");
                        continue;
                    }

                    // Handle missing city names - use airport code if city is missing.
                    if (isEmptyOrNull(originCity)) {
                        originCity = origin;
                    }

                    if (isEmptyOrNull(destCity)) {
                        destCity = dest;
                    }

                    // Determine if the flight is cancelled
                    boolean isCancelled = false;
                    if (!isEmptyOrNull(cancelledStr)) {
                        cancelledStr = cancelledStr.trim().toLowerCase();
                        isCancelled = cancelledStr.equals("1") ||
                                cancelledStr.equals("1.0");
                    }

                    // Also check cancellation code
                    if (!isEmptyOrNull(cancellationCode)) {
                        isCancelled = true;
                    }

                    if (isCancelled) {
                        cancelledFlights++;
                    }

                    // Determine if the flight is diverted
                    boolean isDiverted = false;
                    if (!isEmptyOrNull(divertedStr)) {
                        divertedStr = divertedStr.trim().toLowerCase();
                        isDiverted = divertedStr.equals("1") ||
                                divertedStr.equals("1.0");
                    }

                    if (isDiverted) {
                        divertedFlights++;
                    }

                    String cleanAirlineCode = cleanCode(airlineCode);
                    String cleanAirlineName = cleanText(airlineName);

                    // Track unique airlines.
                    uniqueAirlines.add(cleanAirlineCode);

                    // Insert airline
                    airlineStmt.setString(1, cleanAirlineCode);
                    airlineStmt.setString(2, cleanAirlineName);
                    try {
                        airlineStmt.executeUpdate();
                    } catch (SQLException e) {
                        logError("Line " + lineNumber + ": Warning - Could not insert airline: " + e.getMessage());
                    }

                    // Clean up airport data
                    String cleanOriginCode = cleanCode(origin);
                    String cleanOriginCity = cleanText(originCity);
                    String cleanDestCode = cleanCode(dest);
                    String cleanDestCity = cleanText(destCity);

                    // Track unique airports
                    uniqueAirports.add(cleanOriginCode);
                    uniqueAirports.add(cleanDestCode);

                    // Insert origin airport
                    airportStmt.setString(1, cleanOriginCode);
                    airportStmt.setString(2, cleanOriginCity);

                    try {
                        airportStmt.executeUpdate();
                    } catch (SQLException e) {
                        logError("Line " + lineNumber + ": Warning - Could not insert origin airport: " + e.getMessage());
                    }

                    airportStmt.setString(1, cleanDestCode);
                    airportStmt.setString(2, cleanDestCity);
                    try {
                        airportStmt.executeUpdate();
                    } catch (SQLException e) {
                        logError("Line " + lineNumber + ": Warning - Could not insert destination airport: " + e.getMessage());
                    }

                    int flightNumber;
                    try {
                        flightNumber = Integer.parseInt(flNumberStr.trim());
                    } catch (NumberFormatException e) {
                        // We will take an interesting approach with dealing with invalid flight numbers.
                        // If flight number is missing or not a number, we will generate a synthetic one.
                        flightNumber = Math.abs((flDate + cleanAirlineCode + cleanOriginCode + cleanDestCode).hashCode() % 10000);
                        logError("Line " + lineNumber + ": Warning - Invalid flight number '" + flNumberStr +
                                "', using generated number: " + flightNumber);
                    }

                    // Insert flight data
                    flightStmt.setString(1, flDate);
                    flightStmt.setString(2, cleanAirlineCode);
                    flightStmt.setInt(3, flightNumber);
                    flightStmt.setString(4, cleanOriginCode);
                    flightStmt.setString(5, cleanDestCode);

                    // Handle scheduled times - these are required even for cancelled flights
                    int crsDepTime = parseTimeValue(crsDepTimeStr);
                    int crsArrTime = parseTimeValue(crsArrTimeStr);

                    // For cancelled flights, actual times may be missing
                    int depTime = parseTimeValue(depTimeStr);
                    int arrTime = parseTimeValue(arrTimeStr);

                    // Validate that we have at least the scheduled times
                    if (crsDepTime == 0 || crsArrTime == 0) {
                        recordSkipReason("Missing scheduled times");
                        logError("Line " + lineNumber + ": Skipped - Missing scheduled departure or arrival time");
                        continue;
                    }

                    flightStmt.setInt(6, crsDepTime);
                    flightStmt.setInt(7, depTime);   // May be 0 for cancelled flights
                    flightStmt.setInt(8, crsArrTime);
                    flightStmt.setInt(9, arrTime);   // May be 0 for cancelled flights

                    // Add cancellation and diversion information
                    flightStmt.setBoolean(10, isCancelled);
                    flightStmt.setString(11, isEmptyOrNull(cancellationCode) ? null : cancellationCode.trim());
                    flightStmt.setBoolean(12, isDiverted);

                    try {
                        flightStmt.executeUpdate();
                    } catch (SQLException E) {
                        recordSkipReason("Database error: " + E.getMessage());
                        logError("Line " + lineNumber + ": Database error inserting flight: " + E.getMessage());
                        continue;
                    }

                    // Get the generated flight_id.
                    int flightId = -1;
                    try (var rs = flightStmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            flightId = rs.getInt(1);
                        } else {
                            recordSkipReason("Failed to get flight ID");
                            logError("Line " + lineNumber + ": Failed to get flight ID");
                            continue;
                        }
                    }

                    // Only insert delay reasons for non-cancelled flights.
                    if (!isCancelled) {
                        // Insert delay reasons if present.
                        boolean anyDelayInserted = false;
                        anyDelayInserted |= insertDelayReason(delayStmt, flightId, "CARRIER",
                                getColumnValue(data, columnMap, "DELAY_DUE_CARRIER"), lineNumber);
                        anyDelayInserted |= insertDelayReason(delayStmt, flightId, "WEATHER",
                                getColumnValue(data, columnMap, "DELAY_DUE_WEATHER"), lineNumber);
                        anyDelayInserted |= insertDelayReason(delayStmt, flightId, "NAS",
                                getColumnValue(data, columnMap, "DELAY_DUE_NAS"), lineNumber);
                        anyDelayInserted |= insertDelayReason(delayStmt, flightId, "SECURITY",
                                getColumnValue(data, columnMap, "DELAY_DUE_SECURITY"), lineNumber);
                        anyDelayInserted |= insertDelayReason(delayStmt, flightId, "LATE_AIRCRAFT",
                                getColumnValue(data, columnMap, "DELAY_DUE_LATE_AIRCRAFT"), lineNumber);

                        // Calculate arrival delay manually if no specific delays were inserted.
                        if (!anyDelayInserted && depTime > 0 && arrTime > 0 && crsArrTime > 0) {
                            String arrDelayStr = getColumnValue(data, columnMap, "ARR_DELAY");
                            if (!isEmptyOrNull(arrDelayStr)) {
                                try {
                                    float arrDelay = Float.parseFloat(arrDelayStr.trim());
                                    if (arrDelay > 0) {
                                        int delayMinutes = Math.round(arrDelay);
                                        delayStmt.setInt(1, flightId);
                                        delayStmt.setString(2, "UNSPECIFIED");
                                        delayStmt.setInt(3, delayMinutes);
                                        delayStmt.executeUpdate();
                                    }
                                } catch (NumberFormatException e) {
                                    logError("Line " + lineNumber + ": Warning - Invalid arrival delay value: " + arrDelayStr);
                                }
                            }
                        }
                    }

                    // Commit in batches for better performance, can be set at the global params.
                    batchCount++;
                    if (batchCount >= BATCH_SIZE) {
                        connection.commit();
                        batchCount = 0;
                    }

                    processedRows++;
                    if (processedRows % PROGRESS_INTERVAL == 0) {
                        updateProgressDisplay();
                    }
                } catch (Exception e) {
                    recordSkipReason("Unexpected error: " + e.getClass().getSimpleName());
                    logError("Line " + lineNumber + ": Error - " + e.getMessage());
                    for (StackTraceElement element : e.getStackTrace()) {
                        logError("    " + element.toString());
                    }
                }
            }

            // Final commit for any remaining batches
            if (batchCount > 0) {
                connection.commit();
            }

            // Final progress update
            updateProgressDisplay();

            // Log summary statistics
            logError("\n----------------------------------------");
            logError("Import completed at " + LocalDateTime.now());
            logError("Total rows in file: " + totalRows);
            logError("Attempted rows: " + attemptedRows);
            logError("Processed rows: " + processedRows);
            logError("Skipped rows: " + skippedRows);
            // We track these for stats but don't log details about individual flights
            logError("Cancelled flights: " + cancelledFlights);
            logError("Diverted flights: " + divertedFlights);
            logError("Success rate: " + String.format("%.2f%%", (processedRows * 100.0) / attemptedRows));

            // Log skip reasons
            logError("\nSkip reasons:");
            skipReasons.entrySet().stream()
                    .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                    .forEach(entry -> logError("  " + entry.getKey() + ": " + entry.getValue()));

            // Log statistics about flights
            logError("\nUnique airlines: " + uniqueAirlines.size());
            logError("Unique airports: " + uniqueAirports.size());

            System.out.println("\nImport completed. Summary:");
            System.out.println("- Total rows: " + totalRows);
            System.out.println("- Successfully processed: " + processedRows);
            System.out.println("- Cancelled flights: " + cancelledFlights);
            System.out.println("- Diverted flights: " + divertedFlights);
            System.out.println("- Skipped: " + skippedRows);
            System.out.println("- Success rate: " + String.format("%.2f%%", (processedRows * 100.0) / attemptedRows));
            System.out.println("- Unique airlines: " + uniqueAirlines.size());
            System.out.println("- Unique airports: " + uniqueAirports.size());
            System.out.println("- Error log saved to: " + errorLogPath);
        } finally {
            if (errorLogWriter != null) {
                errorLogWriter.close();
            }
        }
    }

    private boolean insertDelayReason(PreparedStatement stmt, int flightId, String reason,
                                      String delayStr, int lineNumber) throws SQLException {
        if (isEmptyOrNull(delayStr)) {
            return false;
        }

        try {
            float delay = Float.parseFloat(delayStr.trim());
            if (delay > 0) {
                int delayMinutes = Math.round(delay);
                stmt.setInt(1, flightId);
                stmt.setString(2, reason);
                stmt.setInt(3, delayMinutes);
                stmt.executeUpdate();
                return true;
            }
        } catch (NumberFormatException e) {
            logError("Line " + lineNumber + ": Warning - Invalid delay value for " + reason + ": " + delayStr);
        }

        return false;
    }

    private int parseTimeValue(String timeStr) {
        if (isEmptyOrNull(timeStr)) {
            return 0;
        }

        // Remove any decimal part if present.
        if (timeStr.contains(".")) {
            timeStr = timeStr.substring(0, timeStr.indexOf('.'));
        }

        try {
            // Try parsing directly.
            int timeValue = Integer.parseInt(timeStr.trim());

            // Handle times that might be just minutes (e.g., "45" should be "0045")
            if (timeValue >= 0 && timeValue < 60) {
                return timeValue; // Just minutes
            } else if (timeValue >= 100 && timeValue < 2400) {
                // Standard HHMM format between 0100 and 2359.
                return timeValue;
            } else if (timeValue >= 0 && timeValue <= 99) {
                // Convert 1-2 digit numbers to proper time format (e.g., 45 becomes 45 minutes, or 0045).
                return timeValue;
            } else if (timeValue >= 2400) {
                // Handle times greater than 2359 by converting to within 24-hour range.
                // Just take modulo 2400.
                return timeValue % 2400;
            }
            return timeValue;
        } catch (NumberFormatException e) {
            try {
                // Try parsing as a time (HH:MM)
                if (timeStr.contains(":")) {
                    String[] parts = timeStr.split(":");
                    int hours = Integer.parseInt(parts[0].trim());
                    int minutes = Integer.parseInt(parts[1].trim());

                    // Validate hours and minutes
                    hours = Math.max(0, Math.min(23, hours));
                    minutes = Math.max(0, Math.min(59, minutes));

                    return hours * 100 + minutes;
                }
            } catch (Exception ex) {
                // Parsing as HH:MM failed
            }
            return 0;
        }
    }

    private Map<String, Integer> mapColumnIndices(String[] headers) {
        Map<String, Integer> columnMap = new HashMap<>();
        for (int i = 0; i < headers.length; i++) {
            columnMap.put(headers[i].trim(), i);
        }
        return columnMap;
    }

    private void updateProgressDisplay() {
        if (totalRows > 0) {
            double percentage = (processedRows * 100.0) / totalRows;
            System.out.print(String.format("\rProgress: %.2f%% (%d/%d rows) | Skipped: %d | Cancelled: %d | Diverted: %d",
                    percentage, processedRows, totalRows, skippedRows, cancelledFlights, divertedFlights));
        } else {
            System.out.print("\rProcessed " + processedRows + " rows");
        }
    }

    // Parses a single CSV line, handling quoted fields and escaped quotes.
    // Returns an array of strings, representing the individual fields in the CSV line.
    private String[] parseCsvLine(String line) {
        if (line == null || line.isEmpty()){
            return new  String[0];
        }

        boolean inQuotes = false;
        StringBuilder field = new StringBuilder();
        List<String> fields = new ArrayList<>();

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    field.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                fields.add(field.toString());
                field.setLength(0);
            } else {
                field.append(c);
            }
        }

        fields.add(field.toString());

        return fields.toArray(new String[0]);
    }

    // Assuming all data for flights come in a similar fashion.
    private int getMinRequiredColumns(Map<String, Integer> columnMap) {
        int maxIndex = 0;
        String[] essentialColumns = {"FL_DATE", "AIRLINE_CODE", "FL_NUMBER", "ORIGIN", "DEST", "CRS_DEP_TIME", "CRS_ARR_TIME"};

        for (String column : essentialColumns) {
            Integer index = columnMap.get(column);
            if (index != null && index > maxIndex) {
                maxIndex = index;
            }
        }

        return maxIndex + 1;
    }

    private String getColumnValue(String[] data, Map<String, Integer> columnMap, String columnName) {
        Integer index = columnMap.get(columnName);
        if (index == null || index >= data.length) {
            return "";
        }
        return data[index].trim();
    }

    private boolean isEmptyOrNull(String str) {
        return str == null || str.trim().isEmpty();
    }

    private String cleanCode(String code) {
        if (code == null) {
            return "";
        }

        // Remove all whitespace and convert to uppercase.
        return code.replaceAll("\\s+", "").toUpperCase();
    }

    private String cleanText(String text) {
        if (text == null) {
            return "";
        }

        // Remove quotes, excessive spaces, and trim.
        return text.replace("\"", "").replaceAll("\\s+", " ").trim();
    }

    // These be the getters, no setters./
    public int getProcessedRows() {
        return processedRows;
    }


    public int getTotalRows() {
        return totalRows;
    }


    public int getSkippedRows() {
        return skippedRows;
    }


    public int getCancelledFlightsCount() {
        return cancelledFlights;
    }


    public int getDivertedFlightsCount() {
        return divertedFlights;
    }


    public String getErrorLogPath() {
        return errorLogPath;
    }

    public Map<String, Integer> getSkipReasons() {
        return new HashMap<>(skipReasons);
    }

    public int getUniqueAirlinesCount() {
        return uniqueAirlines.size();
    }

    public int getUniqueAirportsCount() {
        return uniqueAirports.size();
    }


}
