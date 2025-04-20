import database.CsvImporter;
import database.DatabaseManager;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;

public class DataImportMain {

    public static void main(String[] args) {
        System.out.println("Flight Punctuality Data Import Program");
        System.out.println("-------------------------------------");

        Instant startTime = Instant.now();

        String csvFilePath = args.length > 0 ? args[0] : "src/flights.csv";

        // Check if file exists.
        File csvFile = new File(csvFilePath);
        if (!csvFile.exists() || !csvFile.isFile()) {
            System.err.println("Error: CSV file not found: " + csvFilePath + " this should not be happening.");

            // Creepy penguin art to show this is serious.
            System.err.println("    _____");
            System.err.println("   /     \\");
            System.err.println("  | o   o |");
            System.err.println("  |   ∧   |");
            System.err.println("  |  \\_/  |");
            System.err.println(" /|       |\\");
            System.err.println("/ |  ___  | \\");
            System.err.println("  |_______|");
            System.err.println("  |  | |  |");
            System.err.println("  |  | |  |");
            System.err.println("  L_/   \\_J");
            System.err.println("    |_|_|");
            System.err.println("    |_|_|");
            System.exit(1);
        }

        System.out.println("Using CSV file: " + csvFilePath);
        System.out.println("File size: " + formatFileSize(csvFile.length()));

        DatabaseManager dbManager = new DatabaseManager();

        try {
            dbManager.connect();

            System.out.println("Creating database schema....");
            dbManager.createSchema();

            System.out.println("\nStarting import of CSV...");
            CsvImporter importer = new CsvImporter(dbManager.getConnection());


            // Start import and calculate import duration.
            Instant importStartTime = Instant.now();
            importer.importCsv(csvFilePath);
            Instant importEndTime = Instant.now();

            Duration importDuration = Duration.between(importStartTime, importEndTime);

            System.out.println("\n========== IMPORT SUMMARY ==========");
            System.out.println("Import completed successfully in " + formatDuration(importDuration));
            System.out.println("Total rows in file: " + importer.getTotalRows());
            System.out.println("Rows processed successfully: " + importer.getProcessedRows());
            System.out.println("Rows skipped: " + importer.getSkippedRows());
            System.out.println("Success rate: " + String.format("%.2f%%",
                    (importer.getProcessedRows() * 100.0) / importer.getTotalRows()));

            try {
                int cancelledFlights = importer.getCancelledFlightsCount();
                int divertedFlights = importer.getDivertedFlightsCount();
                System.out.println("Cancelled flights: " + cancelledFlights);
                System.out.println("Diverted flights: " + divertedFlights);
            } catch (NoSuchMethodError e) {
                System.out.println("gulp");
            }

            try {
                int uniqueAirlines = importer.getUniqueAirlinesCount();
                int uniqueAirports = importer.getUniqueAirportsCount();
                System.out.println("Unique airlines: " + uniqueAirlines);
                System.out.println("Unique airports: " + uniqueAirports);
            } catch (NoSuchMethodError e) {
                System.out.println("Whoops " + e);

            }

            try {
                String errorLogPath = importer.getErrorLogPath();
                if (errorLogPath != null && !errorLogPath.isEmpty()) {
                    System.out.println("\nDetailed error log saved to: " + errorLogPath);
                }
            } catch (NoSuchMethodError e) {
                System.out.println(e);
            }

            Instant endTime = Instant.now();
            Duration totalDuration = Duration.between(startTime, endTime);
            System.out.println("\nTotal execution time: " + formatDuration(totalDuration));

        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("I/O error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Ensure database connection is closed
            try {
                dbManager.disconnect();
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
            }
        }
    }

    private static String formatFileSize(long bytes) {
        final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
        int unitIndex = 0;
        double size = bytes;

        while (size > 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }

        return String.format("%.2f %s", size, units[unitIndex]);
    }

    private static String formatDuration(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();

        StringBuilder sb = new StringBuilder();
        if (hours > 0) {
            sb.append(hours).append("h ");
        }
        if (hours > 0 || minutes > 0) {
            sb.append(minutes).append("m ");
        }
        sb.append(seconds).append("s");

        return sb.toString();
    }
}
