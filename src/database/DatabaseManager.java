package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:flights.db";
    private Connection connection;

    public void connect() throws SQLException {
        connection = DriverManager.getConnection(DB_URL);
        connection.setAutoCommit(false); // Since we are doing bulk inserts, this will speed things up greatly.
        System.out.println("Connected to the database, yippie! :)");
    }

    public void disconnect() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
            System.out.println("Database connection closed. Goodbye!");
        }
    }

    public void createSchema() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("DROP TABLE IF EXISTS Delay_Reason");
            stmt.executeUpdate("DROP TABLE IF EXISTS Flight");
            stmt.executeUpdate("DROP TABLE IF EXISTS Airline");
            stmt.executeUpdate("DROP TABLE IF EXISTS Airport");

            // Creating schemas based on the document given, whilst also supporting cancelled and diverted flights.

            // Airport table
            stmt.executeUpdate(
                    "CREATE TABLE Airport (" +
                            "iata_code CHAR(3) PRIMARY KEY, " +
                            "name TEXT" +
                            ")"
            );

            // Airline table
            stmt.executeUpdate(
                    "CREATE TABLE Airline (" +
                            "iata_code CHAR(2) PRIMARY KEY, " +
                            "name TEXT" +
                            ")"
            );

            // Flight table - enhanced with cancelled and diverted flight information
            stmt.executeUpdate(
                    "CREATE TABLE Flight (" +
                            "flight_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            "date CHAR(8), " +
                            "airline_code CHAR(2), " +
                            "flight_number INTEGER, " +
                            "flight_origin CHAR(3), " +
                            "flight_destination CHAR(3), " +
                            "scheduled_departure INTEGER, " +
                            "actual_departure INTEGER, " +
                            "scheduled_arrival INTEGER, " +
                            "actual_arrival INTEGER, " +
                            "cancelled BOOLEAN DEFAULT 0, " +
                            "cancellation_code CHAR(1), " +
                            "diverted BOOLEAN DEFAULT 0, " +
                            "FOREIGN KEY (airline_code) REFERENCES Airline(iata_code), " +
                            "FOREIGN KEY (flight_origin) REFERENCES Airport(iata_code), " +
                            "FOREIGN KEY (flight_destination) REFERENCES Airport(iata_code)" +
                            ")"
            );

            // Delay_Reason table
            stmt.executeUpdate(
                    "CREATE TABLE Delay_Reason (" +
                            "delay_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            "flight_id INTEGER, " +
                            "reason TEXT, " +
                            "delay_length INTEGER, " +
                            "FOREIGN KEY (flight_id) REFERENCES Flight(flight_id)" +
                            ")"
            );

            connection.commit();
            System.out.println("Database schema created with support for cancelled and diverted flights.");
        }

    }

    public Connection getConnection() {
        return connection;
    }
}
