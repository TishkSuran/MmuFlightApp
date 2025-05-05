package service;

import flightModel.Flight;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;


public class FlightDataService {

    private static final String DB_URL = "jdbc:sqlite:flights.db";
    private Connection conn;

    // Constructor - connecting to the DB.
    public FlightDataService() throws SQLException {
        conn = DriverManager.getConnection(DB_URL);
        System.out.println("DB connected: " + DB_URL);
    }

    // Close DB connection, goodbye!
    public void disconnect() throws SQLException {
        if (conn != null && !conn.isClosed()) {
            conn.close();
        }
    }


    public List<Flight> searchFlights(String airline, String flightNumber,
                                      String origin, String destination,
                                      LocalDate startDate, LocalDate endDate,
                                      Integer minDelay, Integer maxDelay,
                                      String delayReason) throws SQLException {

        StringBuilder sql = new StringBuilder();
        List<Object> params = new ArrayList<>();

        sql.append(
                "SELECT f.flight_id, f.date, f.cancelled, f.diverted, f.cancellation_code, " +
                        "a.iata_code AS airline_code, a.name AS airline_name, " +
                        "f.flight_number, o.iata_code AS origin_code, o.name AS origin_city, " +
                        "d.iata_code AS dest_code, d.name AS dest_city, " +
                        "f.scheduled_departure, f.actual_departure, f.scheduled_arrival, f.actual_arrival " +
                        "FROM Flight f " +
                        "JOIN Airline a ON f.airline_code = a.iata_code " +
                        "JOIN Airport o ON f.flight_origin = o.iata_code " +
                        "JOIN Airport d ON f.flight_destination = d.iata_code " +
                        "WHERE 1=1 "
        );

        // Add filters.
        if (airline != null && !airline.trim().isEmpty()) {
            sql.append("AND (a.iata_code LIKE ? OR a.name LIKE ?) ");
            String pattern = "%" + airline.trim() + "%";
            params.add(pattern);
            params.add(pattern);
        }

        // Flight number - could be "AA123" or just "123".
        if (flightNumber != null && !flightNumber.trim().isEmpty()) {
            String fn = flightNumber.trim();

            // Try to parse the number part.
            try {
                // Check if starts with letters (airline code).
                if (Character.isLetter(fn.charAt(0))) {
                    // Split into airline code and number.
                    int i = 0;
                    while (i < fn.length() && Character.isLetter(fn.charAt(i))) i++;

                    String airlineCode = fn.substring(0, i);
                    String numPart = fn.substring(i);

                    if (!numPart.isEmpty()) {
                        sql.append("AND a.iata_code = ? AND f.flight_number = ? ");
                        params.add(airlineCode);
                        params.add(Integer.parseInt(numPart));
                    }
                } else {
                    // Just a flight number.
                    sql.append("AND f.flight_number = ? ");
                    params.add(Integer.parseInt(fn));
                }
            } catch (Exception e) {
                // If parsing fails, add impossible condition.
                sql.append("AND 1=0 ");
            }
        }

        // Origin.
        if (origin != null && !origin.trim().isEmpty()) {
            sql.append("AND (o.iata_code = ? OR o.name LIKE ?) ");
            params.add(origin.trim().toUpperCase());
            params.add("%" + origin.trim() + "%");
        }

        // Destination.
        if (destination != null && !destination.trim().isEmpty()) {
            sql.append("AND (d.iata_code = ? OR d.name LIKE ?) ");
            params.add(destination.trim().toUpperCase());
            params.add("%" + destination.trim() + "%");
        }

        if (startDate != null || endDate != null) {
            // Add a computation to extract year, month, day as integers for comparison
            sql.append("AND CAST(substr(f.date, 5, 4) AS INTEGER) * 10000 + " +
                    "CAST(substr(f.date, 3, 2) AS INTEGER) * 100 + " +
                    "CAST(substr(f.date, 1, 2) AS INTEGER) ");

            if (startDate != null) {
                sql.append(">= ? ");
                int dateValue = startDate.getYear() * 10000 +
                        startDate.getMonthValue() * 100 +
                        startDate.getDayOfMonth();
                params.add(dateValue);
            }

            if (startDate != null && endDate != null) {
                sql.append("AND CAST(substr(f.date, 5, 4) AS INTEGER) * 10000 + " +
                        "CAST(substr(f.date, 3, 2) AS INTEGER) * 100 + " +
                        "CAST(substr(f.date, 1, 2) AS INTEGER) ");
            }

            if (endDate != null) {
                sql.append("<= ? ");
                int dateValue = endDate.getYear() * 10000 +
                        endDate.getMonthValue() * 100 +
                        endDate.getDayOfMonth();
                params.add(dateValue);
            }
        }

        // Delay filters - only join if needed.
        boolean hasDelayFilter = (minDelay != null || maxDelay != null ||
                (delayReason != null && !delayReason.trim().isEmpty()));

        if (hasDelayFilter) {
            sql.append("AND f.flight_id IN (SELECT DISTINCT flight_id FROM Delay_Reason WHERE 1=1 ");

            if (delayReason != null && !delayReason.trim().isEmpty()) {
                sql.append("AND reason = ? ");
                params.add(delayReason.trim().toUpperCase());
            }

            if (minDelay != null) {
                sql.append("AND delay_length >= ? ");
                params.add(minDelay);
            }

            if (maxDelay != null) {
                sql.append("AND delay_length <= ? ");
                params.add(maxDelay);
            }

            sql.append(") ");
        }


        // Debug info.
        System.out.println("Search SQL: " + sql);

        // Run query.
        List<Flight> results = new ArrayList<>();
        Map<Integer, Flight> flightMap = new HashMap<>();

        try (PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            // Set parameters.
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Flight f = new Flight();
                    f.setFlightId(rs.getInt("flight_id"));
                    f.setDateFromString(rs.getString("date"));
                    f.setAirlineCode(rs.getString("airline_code"));
                    f.setAirlineName(rs.getString("airline_name"));
                    f.setFlightNumber(rs.getInt("flight_number"));
                    f.setOriginCode(rs.getString("origin_code"));
                    f.setOriginCity(rs.getString("origin_city"));
                    f.setDestCode(rs.getString("dest_code"));
                    f.setDestCity(rs.getString("dest_city"));
                    f.setScheduledDeparture(rs.getInt("scheduled_departure"));
                    f.setActualDeparture(rs.getInt("actual_departure"));
                    f.setScheduledArrival(rs.getInt("scheduled_arrival"));
                    f.setActualArrival(rs.getInt("actual_arrival"));
                    f.setCancelled(rs.getBoolean("cancelled"));
                    f.setDiverted(rs.getBoolean("diverted"));
                    f.setCancellationCode(rs.getString("cancellation_code"));

                    results.add(f);
                    flightMap.put(f.getFlightId(), f);
                }
            }
        }

        System.out.println("Found " + results.size() + " flights");

        // Fetch delay reasons if we have results.
        if (!results.isEmpty()) {
            fetchDelays(flightMap);
        }

        return results;
    }
    // Get delay reasons for flights.
    private void fetchDelays(Map<Integer, Flight> flightMap) throws SQLException {
        if (flightMap.isEmpty()) return;

        // Build list of IDs.
        StringBuilder ids = new StringBuilder();
        for (Integer id : flightMap.keySet()) {
            if (ids.length() > 0) ids.append(",");
            ids.append(id);
        }

        String sql = "SELECT flight_id, reason, delay_length FROM Delay_Reason WHERE flight_id IN (" + ids + ")";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int flightId = rs.getInt("flight_id");
                String reason = rs.getString("reason");
                int delayLength = rs.getInt("delay_length");

                Flight f = flightMap.get(flightId);
                if (f != null) {
                    f.addDelay(new Flight.Delay(reason, delayLength));
                }
            }
        }
    }

    // Get all airlines for dropdown.
    public List<String> getAirlines() throws SQLException {
        List<String> airlines = new ArrayList<>();

        String sql = "SELECT iata_code, name FROM Airline ORDER BY name";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String code = rs.getString("iata_code");
                String name = rs.getString("name");
                airlines.add(code + " - " + name);
            }
        }

        return airlines;
    }

    // Get all airports for dropdown.
    public List<String> getAirports() throws SQLException {
        List<String> airports = new ArrayList<>();

        String sql = "SELECT iata_code, name FROM Airport ORDER BY name";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String code = rs.getString("iata_code");
                String name = rs.getString("name");
                airports.add(code + " - " + name);
            }
        }

        return airports;
    }

    // Get average delay by airline for a year.
    public Map<String, Double> getAverageDelayByAirline(int year) throws SQLException {
        Map<String, Double> results = new HashMap<>();

        // Try with delay_reason table first.
        String sql =
                "SELECT a.name AS airline_name, " +
                        "AVG(dr.delay_length) AS avg_delay " +
                        "FROM Flight f " +
                        "JOIN Airline a ON f.airline_code = a.iata_code " +
                        "JOIN Delay_Reason dr ON f.flight_id = dr.flight_id " +
                        "WHERE substr(f.date, 5, 4) = ? " +
                        "GROUP BY a.name " +
                        "HAVING COUNT(*) > 1 " +
                        "ORDER BY avg_delay DESC";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, String.valueOf(year));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.put(rs.getString("airline_name"), rs.getDouble("avg_delay"));
                }
            }
        }

        // If empty, try fallback approach.
        if (results.isEmpty()) {
            sql = "SELECT a.name AS airline_name, " +
                    "AVG(CASE WHEN f.actual_arrival > f.scheduled_arrival " +
                    "THEN (f.actual_arrival - f.scheduled_arrival) ELSE 0 END) AS avg_delay " +
                    "FROM Flight f " +
                    "JOIN Airline a ON f.airline_code = a.iata_code " +
                    "WHERE substr(f.date, 5, 4) = ? " +
                    "AND f.scheduled_arrival > 0 AND f.actual_arrival > 0 " +
                    "GROUP BY a.name " +
                    "HAVING COUNT(*) > 1 " +
                    "ORDER BY avg_delay DESC";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, String.valueOf(year));

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        String name = rs.getString("airline_name");
                        double delay = rs.getDouble("avg_delay");

                        // Convert HHMM to minutes if needed
                        if (delay > 100) {
                            delay = (Math.floor(delay / 100) * 60) + (delay % 100);
                        }

                        results.put(name, delay);
                    }
                }
            }
        }

        return results;
    }

    // Get average delay by airport for a year.
    public Map<String, Double> getAverageDelayByAirport(int year) throws SQLException {
        Map<String, Double> results = new HashMap<>();

        // Try delay_reason table first.
        String sql =
                "SELECT o.name AS airport_name, " +
                        "AVG(dr.delay_length) AS avg_delay " +
                        "FROM Flight f " +
                        "JOIN Airport o ON f.flight_origin = o.iata_code " +
                        "JOIN Delay_Reason dr ON f.flight_id = dr.flight_id " +
                        "WHERE substr(f.date, 5, 4) = ? " +
                        "GROUP BY o.name " +
                        "HAVING COUNT(*) > 1 " +
                        "ORDER BY avg_delay DESC " +
                        "LIMIT 2000000";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, String.valueOf(year));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.put(rs.getString("airport_name"), rs.getDouble("avg_delay"));
                }
            }
        }

        // If empty, try fallback approach
        if (results.isEmpty()) {
            sql = "SELECT o.name AS airport_name, " +
                    "AVG(CASE WHEN f.actual_arrival > f.scheduled_arrival " +
                    "THEN (f.actual_arrival - f.scheduled_arrival) ELSE 0 END) AS avg_delay " +
                    "FROM Flight f " +
                    "JOIN Airport o ON f.flight_origin = o.iata_code " +
                    "WHERE substr(f.date, 5, 4) = ? " +
                    "AND f.scheduled_arrival > 0 AND f.actual_arrival > 0 " +
                    "GROUP BY o.name " +
                    "HAVING COUNT(*) > 1 " +
                    "ORDER BY avg_delay DESC " +
                    "LIMIT 2000000";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, String.valueOf(year));

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        String name = rs.getString("airport_name");
                        double delay = rs.getDouble("avg_delay");

                        // Convert HHMM to minutes if needed
                        if (delay > 100) {
                            delay = (Math.floor(delay / 100) * 60) + (delay % 100);
                        }

                        results.put(name, delay);
                    }
                }
            }
        }

//        if (results.isEmpty()) {
//            results.put("Atlanta, GA", 20.0);
//        }

        return results;
    }

    // Get monthly delays for an airport over a date range.
    public Map<String, Double> getDelaysByMonth(String airportCode, int startYear, int endYear) throws SQLException {
        Map<String, Double> results = new HashMap<>();

        // Try delay_reason table first.
        String sql =
                "SELECT substr(f.date, 3, 2) || '/' || substr(f.date, 5, 4) AS month_year, " +
                        "AVG(dr.delay_length) AS avg_delay " +
                        "FROM Flight f " +
                        "JOIN Delay_Reason dr ON f.flight_id = dr.flight_id " +
                        "WHERE f.flight_origin = ? " +
                        "AND substr(f.date, 5, 4) BETWEEN ? AND ? " +
                        "GROUP BY month_year " +
                        "ORDER BY substr(f.date, 5, 4), substr(f.date, 3, 2)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, airportCode);
            stmt.setString(2, String.valueOf(startYear));
            stmt.setString(3, String.valueOf(endYear));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.put(rs.getString("month_year"), rs.getDouble("avg_delay"));
                }
            }
        }

        return results;
    }
}