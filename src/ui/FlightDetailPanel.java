package ui;

import model.Flight;

import javax.swing.*;
import java.awt.*;

/**
 * Panel for displaying detailed information about a flight.
 * Enhanced with support for cancelled and diverted flights.
 */
public class FlightDetailPanel extends JPanel {

    private final JLabel dateLabel = new JLabel();
    private final JLabel flightNumberLabel = new JLabel();
    private final JLabel airlineLabel = new JLabel();
    private final JLabel originLabel = new JLabel();
    private final JLabel destinationLabel = new JLabel();
    private final JLabel scheduledDepLabel = new JLabel();
    private final JLabel actualDepLabel = new JLabel();
    private final JLabel scheduledArrLabel = new JLabel();
    private final JLabel actualArrLabel = new JLabel();
    private final JLabel statusLabel = new JLabel();
    private final JLabel delayLabel = new JLabel();
    private final JPanel delayReasonPanel = new JPanel(new GridLayout(0, 1));

    /**
     * Creates a new flight detail panel.
     */
    public FlightDetailPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Flight Details"));

        JPanel infoPanel = new JPanel(new GridLayout(0, 2, 10, 5));

        infoPanel.add(new JLabel("Date:"));
        infoPanel.add(dateLabel);

        infoPanel.add(new JLabel("Flight Number:"));
        infoPanel.add(flightNumberLabel);

        infoPanel.add(new JLabel("Airline:"));
        infoPanel.add(airlineLabel);

        infoPanel.add(new JLabel("Origin:"));
        infoPanel.add(originLabel);

        infoPanel.add(new JLabel("Destination:"));
        infoPanel.add(destinationLabel);

        infoPanel.add(new JLabel("Status:"));
        infoPanel.add(statusLabel);

        infoPanel.add(new JLabel("Scheduled Departure:"));
        infoPanel.add(scheduledDepLabel);

        infoPanel.add(new JLabel("Actual Departure:"));
        infoPanel.add(actualDepLabel);

        infoPanel.add(new JLabel("Scheduled Arrival:"));
        infoPanel.add(scheduledArrLabel);

        infoPanel.add(new JLabel("Actual Arrival:"));
        infoPanel.add(actualArrLabel);

        infoPanel.add(new JLabel("Delay (minutes):"));
        infoPanel.add(delayLabel);

        // Delay reasons section
        JPanel delaySection = new JPanel(new BorderLayout());
        delaySection.add(new JLabel("Delay Reasons:"), BorderLayout.NORTH);
        delaySection.add(delayReasonPanel, BorderLayout.CENTER);

        add(infoPanel, BorderLayout.NORTH);
        add(delaySection, BorderLayout.CENTER);

        // Initial state
        clearDetails();
    }

    /**
     * Displays the details of the specified flight.
     * @param flight the flight to display
     */
    public void setFlight(Flight flight) {
        if (flight == null) {
            clearDetails();
            return;
        }

        dateLabel.setText(flight.getFormattedDate());
        flightNumberLabel.setText(flight.getFullFlightNumber());
        airlineLabel.setText(flight.getAirlineName());
        originLabel.setText(flight.getOriginDisplay());
        destinationLabel.setText(flight.getDestinationDisplay());

        // Display flight status (cancelled, diverted, or delay info)
        String status = flight.getStatus();
        statusLabel.setText(status);

        // Set status label color based on flight status
        if (status.startsWith("Cancelled")) {
            statusLabel.setForeground(new Color(192, 0, 0)); // Dark red
        } else if (status.startsWith("Diverted")) {
            statusLabel.setForeground(new Color(255, 140, 0)); // Dark orange
        } else if (status.startsWith("Delayed")) {
            statusLabel.setForeground(new Color(255, 69, 0)); // Red-orange
        } else if (status.startsWith("On Time")) {
            statusLabel.setForeground(new Color(0, 128, 0)); // Dark green
        } else {
            statusLabel.setForeground(Color.BLACK);
        }

        scheduledDepLabel.setText(Flight.formatTime(flight.getScheduledDeparture()));

        // For cancelled flights, show "Cancelled" for actual times
        if (flight.isCancelled()) {
            actualDepLabel.setText("Cancelled");
            actualArrLabel.setText("Cancelled");
            actualDepLabel.setForeground(new Color(192, 0, 0));
            actualArrLabel.setForeground(new Color(192, 0, 0));
            delayLabel.setText("N/A");
            delayLabel.setForeground(Color.BLACK);
        } else {
            actualDepLabel.setText(Flight.formatTime(flight.getActualDeparture()));
            actualArrLabel.setText(Flight.formatTime(flight.getActualArrival()));
            actualDepLabel.setForeground(Color.BLACK);
            actualArrLabel.setForeground(Color.BLACK);

            // For diverted flights, indicate the diversion
            if (flight.isDiverted()) {
                actualArrLabel.setText("Diverted");
                actualArrLabel.setForeground(new Color(255, 140, 0));
                delayLabel.setText("N/A");
                delayLabel.setForeground(Color.BLACK);
            } else {
                int delayMinutes = flight.getDelayMinutes();
                delayLabel.setText(String.valueOf(delayMinutes));

                // Format delay label based on value
                if (delayMinutes > 60) {
                    delayLabel.setForeground(Color.RED);
                } else if (delayMinutes > 15) {
                    delayLabel.setForeground(Color.ORANGE);
                } else {
                    delayLabel.setForeground(new Color(0, 128, 0)); // Dark green
                }
            }
        }

        scheduledArrLabel.setText(Flight.formatTime(flight.getScheduledArrival()));

        // Display delay reasons
        delayReasonPanel.removeAll();

        if (flight.isCancelled()) {
            String cancellationCode = flight.getCancellationCode();
            if (cancellationCode != null && !cancellationCode.isEmpty()) {
                JLabel cancelLabel = new JLabel("Flight cancelled due to: " + formatCancellationCode(cancellationCode));
                cancelLabel.setForeground(new Color(192, 0, 0));
                delayReasonPanel.add(cancelLabel);
            } else {
                JLabel cancelLabel = new JLabel("Flight cancelled (reason unknown)");
                cancelLabel.setForeground(new Color(192, 0, 0));
                delayReasonPanel.add(cancelLabel);
            }
        } else if (flight.isDiverted()) {
            JLabel divertedLabel = new JLabel("Flight was diverted");
            divertedLabel.setForeground(new Color(255, 140, 0));
            delayReasonPanel.add(divertedLabel);
        } else if (flight.getDelays().isEmpty()) {
            if (flight.getDelayMinutes() > 15) {
                delayReasonPanel.add(new JLabel("No specific delay reasons recorded"));
            } else {
                delayReasonPanel.add(new JLabel("No delays reported"));
            }
        } else {
            for (Flight.Delay delay : flight.getDelays()) {
                JLabel reasonLabel = new JLabel(delay.getFormattedReason() + ": " + delay.getMinutes() + " minutes");
                delayReasonPanel.add(reasonLabel);
            }
        }

        revalidate();
        repaint();
    }

    /**
     * Formats a cancellation code into a human-readable reason.
     * @param code the cancellation code
     * @return formatted reason
     */
    private String formatCancellationCode(String code) {
        if (code == null) return "Unknown reason";

        switch (code.trim().toUpperCase()) {
            case "A": return "Carrier (airline issue)";
            case "B": return "Weather conditions";
            case "C": return "National Air System";
            case "D": return "Security reasons";
            default: return "Code: " + code;
        }
    }

    /**
     * Clears all flight details from the panel.
     */
    public void clearDetails() {
        dateLabel.setText("N/A");
        flightNumberLabel.setText("N/A");
        airlineLabel.setText("N/A");
        originLabel.setText("N/A");
        destinationLabel.setText("N/A");
        statusLabel.setText("N/A");
        scheduledDepLabel.setText("N/A");
        actualDepLabel.setText("N/A");
        scheduledArrLabel.setText("N/A");
        actualArrLabel.setText("N/A");
        delayLabel.setText("N/A");
        delayLabel.setForeground(Color.BLACK);
        statusLabel.setForeground(Color.BLACK);

        delayReasonPanel.removeAll();
        delayReasonPanel.add(new JLabel("No flight selected"));

        revalidate();
        repaint();
    }
}