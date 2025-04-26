package ui;

import flightModel.Flight;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;


public class FlightTableModel extends AbstractTableModel {

    // Column data definitions.
    private static final String[] COLUMN_NAMES = {
            "Flight", "Date", "Airline", "Origin", "Destination",
            "Scheduled Dep", "Actual Dep", "Scheduled Arr", "Actual Arr", "Status"
    };

    // Column types for proper sorting.
    private static final Class<?>[] COLUMN_TYPES = {
            String.class, String.class, String.class, String.class, String.class,
            String.class, String.class, String.class, String.class, String.class
    };
    private List<Flight> flights = new ArrayList<>();


    public void setFlights(List<Flight> flights) {
        this.flights = new ArrayList<>(flights);
        fireTableDataChanged();
    }


    public Flight getFlightAt(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < flights.size()) {
            return flights.get(rowIndex);
        }
        return null;
    }

    @Override
    public int getRowCount() {
        return flights.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMN_NAMES.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMN_NAMES[column];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return COLUMN_TYPES[columnIndex];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex < 0 || rowIndex >= flights.size()) {
            return null;
        }

        Flight flight = flights.get(rowIndex);

        switch (columnIndex) {
            case 0: // Flight Number
                return flight.getFullFlightNumber();

            case 1: // Date in format (DD/MM/YYYY)
                return flight.getFormattedDate();

            case 2: // Airline
                return flight.getAirlineName();

            case 3: // Origin
                return flight.getOriginDisplay();

            case 4: // Destination
                return flight.getDestinationDisplay();

            case 5: // Scheduled Departure
                return Flight.formatTime(flight.getScheduledDeparture());

            case 6: // Actual Departure
                if (flight.isCancelled()) {
                    return "Cancelled";
                }
                return Flight.formatTime(flight.getActualDeparture());

            case 7: // Scheduled Arrival
                return Flight.formatTime(flight.getScheduledArrival());

            case 8: // Actual Arrival
                if (flight.isCancelled()) {
                    return "Cancelled";
                } else if (flight.isDiverted()) {
                    return "Diverted";
                }
                return Flight.formatTime(flight.getActualArrival());

            case 9: // Status
                return flight.getStatus();

            default:
                return null;
        }
    }
}