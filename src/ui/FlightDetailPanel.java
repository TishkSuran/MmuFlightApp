package ui;

import flightModel.Flight;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.format.DateTimeFormatter;

/**
 * Panel that displays detailed information about a selected flight.
 * Shows flight details, timing information, and delay data.
 */
public class FlightDetailPanel extends JPanel {

    // UI colors and fonts.
    private final Color headerColor = new Color(41, 128, 185);
    private final Color bgColor = new Color(245, 245, 250);
    private final Font headerFont = new Font("Arial", Font.BOLD, 14);
    private final Font valueFont = new Font("Arial", Font.PLAIN, 13);
    private final Font statusFont = new Font("Arial", Font.BOLD, 16);

    // Status indicator colors.
    private final Color onTimeColor = new Color(46, 204, 113);
    private final Color delayedColor = new Color(231, 76, 60);
    private final Color cancelledColor = new Color(192, 57, 43);
    private final Color divertedColor = new Color(230, 126, 34);

    // DD/MM/YYYY format.
    private final DateTimeFormatter ukDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // UI components.
    private final JLabel statusLabel = new JLabel("No Flight Selected", JLabel.CENTER);
    private final JPanel flightInfoPanel = new JPanel(new GridBagLayout());
    private final JPanel timeInfoPanel = new JPanel(new GridBagLayout());
    private final JPanel delayInfoPanel = new JPanel(new BorderLayout());

    // Flight info fields.
    private final JLabel flightNumberValue = new JLabel();
    private final JLabel dateValue = new JLabel();
    private final JLabel airlineValue = new JLabel();
    private final JLabel originValue = new JLabel();
    private final JLabel destinationValue = new JLabel();

    // Time fields.
    private final JLabel scheduledDepValue = new JLabel();
    private final JLabel actualDepValue = new JLabel();
    private final JLabel scheduledArrValue = new JLabel();
    private final JLabel actualArrValue = new JLabel();
    private final JLabel delayValue = new JLabel();

    // Delay reasons container.
    private final JPanel delayReasonList = new JPanel();

    public FlightDetailPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(bgColor);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // Set up status indicator at top.
        setupStatusBar();

        // Main content in center.
        JPanel contentPanel = new JPanel(new BorderLayout(15, 15));
        contentPanel.setBackground(bgColor);
        contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Set up info panels.
        setupFlightInfoPanel();
        setupTimeInfoPanel();
        setupDelayInfoPanel();

        // Add info panels to content with layout.
        JPanel topInfoPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        topInfoPanel.setBackground(bgColor);
        topInfoPanel.add(flightInfoPanel);
        topInfoPanel.add(timeInfoPanel);

        contentPanel.add(topInfoPanel, BorderLayout.CENTER);
        contentPanel.add(delayInfoPanel, BorderLayout.SOUTH);

        // Add components to main panel.
        add(statusLabel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);

        // Start with empty state.
        clearDetails();
    }

    private void setupStatusBar() {
        statusLabel.setFont(statusFont);
        statusLabel.setOpaque(true);
        statusLabel.setBackground(headerColor);
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setBorder(new EmptyBorder(8, 10, 8, 10));
    }

    // Set up the flight info section.
    private void setupFlightInfoPanel() {
        flightInfoPanel.setBackground(bgColor);
        flightInfoPanel.setBorder(createTitledBorder("Flight Information"));

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(4, 8, 4, 8);

        // Flight number.
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.WEST;
        flightInfoPanel.add(createHeaderLabel("Flight Number:"), c);

        c.gridx = 1;
        c.weightx = 1.0;
        flightInfoPanel.add(flightNumberValue, c);

        // Date.
        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 0.0;
        flightInfoPanel.add(createHeaderLabel("Date:"), c);

        c.gridx = 1;
        c.weightx = 1.0;
        flightInfoPanel.add(dateValue, c);

        // Airline.
        c.gridx = 0;
        c.gridy = 2;
        c.weightx = 0.0;
        flightInfoPanel.add(createHeaderLabel("Airline:"), c);

        c.gridx = 1;
        c.weightx = 1.0;
        flightInfoPanel.add(airlineValue, c);

        // Origin.
        c.gridx = 0;
        c.gridy = 3;
        c.weightx = 0.0;
        flightInfoPanel.add(createHeaderLabel("Origin:"), c);

        c.gridx = 1;
        c.weightx = 1.0;
        flightInfoPanel.add(originValue, c);

        // Destination.
        c.gridx = 0;
        c.gridy = 4;
        c.weightx = 0.0;
        flightInfoPanel.add(createHeaderLabel("Destination:"), c);

        c.gridx = 1;
        c.weightx = 1.0;
        flightInfoPanel.add(destinationValue, c);
    }

    private void setupTimeInfoPanel() {
        timeInfoPanel.setBackground(bgColor);
        timeInfoPanel.setBorder(createTitledBorder("Time Information"));

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(4, 8, 4, 8);

        // Scheduled departure.
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.WEST;
        timeInfoPanel.add(createHeaderLabel("Scheduled Departure:"), c);

        c.gridx = 1;
        c.weightx = 1.0;
        timeInfoPanel.add(scheduledDepValue, c);

        // Actual departure.
        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 0.0;
        timeInfoPanel.add(createHeaderLabel("Actual Departure:"), c);

        c.gridx = 1;
        c.weightx = 1.0;
        timeInfoPanel.add(actualDepValue, c);

        // Scheduled arrival.
        c.gridx = 0;
        c.gridy = 2;
        c.weightx = 0.0;
        timeInfoPanel.add(createHeaderLabel("Scheduled Arrival:"), c);

        c.gridx = 1;
        c.weightx = 1.0;
        timeInfoPanel.add(scheduledArrValue, c);

        // Actual arrival.
        c.gridx = 0;
        c.gridy = 3;
        c.weightx = 0.0;
        timeInfoPanel.add(createHeaderLabel("Actual Arrival:"), c);

        c.gridx = 1;
        c.weightx = 1.0;
        timeInfoPanel.add(actualArrValue, c);

        // Delay minutes.
        c.gridx = 0;
        c.gridy = 4;
        c.weightx = 0.0;
        timeInfoPanel.add(createHeaderLabel("Delay (minutes):"), c);

        c.gridx = 1;
        c.weightx = 1.0;
        timeInfoPanel.add(delayValue, c);
    }

    private void setupDelayInfoPanel() {
        delayInfoPanel.setBackground(bgColor);
        delayInfoPanel.setBorder(createTitledBorder("Delay Information"));

        // Set up layout for delay reasons.
        delayReasonList.setLayout(new BoxLayout(delayReasonList, BoxLayout.Y_AXIS));
        delayReasonList.setBackground(bgColor);

        JScrollPane scrollPane = new JScrollPane(delayReasonList);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setBackground(bgColor);

        delayInfoPanel.add(scrollPane, BorderLayout.CENTER);
    }

    private JLabel createHeaderLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(headerFont);
        label.setForeground(headerColor);
        return label;
    }

    private JLabel createValueLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(valueFont);
        return label;
    }

    private javax.swing.border.Border createTitledBorder(String title) {
        return new CompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(headerColor, 1),
                        title,
                        javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                        javax.swing.border.TitledBorder.DEFAULT_POSITION,
                        headerFont,
                        headerColor
                ),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        );
    }

    /**
     * Updates the panel with details from the given flight.
     */
    public void setFlight(Flight flight) {
        if (flight == null) {
            clearDetails();
            return;
        }

        // Set status with color.
        String status = flight.getStatus();
        statusLabel.setText(status);

        // Change status color based on flight status
        if (status.startsWith("Cancelled")) {
            statusLabel.setBackground(cancelledColor);
        } else if (status.startsWith("Diverted")) {
            statusLabel.setBackground(divertedColor);
        } else if (status.startsWith("Delayed")) {
            statusLabel.setBackground(delayedColor);
        } else if (status.startsWith("On Time")) {
            statusLabel.setBackground(onTimeColor);
        } else {
            statusLabel.setBackground(headerColor);
        }

        // Flight info.
        flightNumberValue.setText(flight.getFullFlightNumber());

        // Format date .
        if (flight.getDate() != null) {
            dateValue.setText(flight.getDate().format(ukDateFormatter));
        } else {
            dateValue.setText("N/A");
        }

        airlineValue.setText(flight.getAirlineName());
        originValue.setText(flight.getOriginDisplay());
        destinationValue.setText(flight.getDestinationDisplay());

        // Time info.
        scheduledDepValue.setText(Flight.formatTime(flight.getScheduledDeparture()));

        // Handle different flight statuses.
        if (flight.isCancelled()) {
            actualDepValue.setText("Cancelled");
            actualArrValue.setText("Cancelled");

            actualDepValue.setForeground(cancelledColor);
            actualArrValue.setForeground(cancelledColor);

            delayValue.setText("N/A");
            delayValue.setForeground(Color.BLACK);
        } else {
            actualDepValue.setText(Flight.formatTime(flight.getActualDeparture()));
            actualDepValue.setForeground(Color.BLACK);

            // Handle diverted flights.
            if (flight.isDiverted()) {
                actualArrValue.setText("Diverted");
                actualArrValue.setForeground(divertedColor);
                delayValue.setText("N/A");
                delayValue.setForeground(Color.BLACK);
            } else {
                actualArrValue.setText(Flight.formatTime(flight.getActualArrival()));
                actualArrValue.setForeground(Color.BLACK);

                // Show delay with color.
                int delayMinutes = flight.getDelayMinutes();
                delayValue.setText(String.valueOf(delayMinutes));

                // Color code by severity.
                if (delayMinutes > 60) {
                    delayValue.setForeground(delayedColor);
                } else if (delayMinutes > 15) {
                    delayValue.setForeground(Color.ORANGE.darker());
                } else if (delayMinutes > 0) {
                    delayValue.setForeground(Color.ORANGE);
                } else {
                    delayValue.setForeground(onTimeColor);
                }
            }
        }

        scheduledArrValue.setText(Flight.formatTime(flight.getScheduledArrival()));

        // Update delay reasons.
        delayReasonList.removeAll();

        if (flight.isCancelled()) {
            // Show cancellation reason.
            String cancellationCode = flight.getCancellationCode();
            String cancelReason = formatCancellationCode(cancellationCode);

            JLabel cancelLabel = new JLabel("Flight cancelled due to: " + cancelReason);
            cancelLabel.setFont(new Font("Arial", Font.BOLD, 13));
            cancelLabel.setForeground(cancelledColor);
            cancelLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
            delayReasonList.add(cancelLabel);
        } else if (flight.isDiverted()) {
            // Show diversion info.
            JLabel divertedLabel = new JLabel("Flight was diverted to another airport");
            divertedLabel.setFont(new Font("Arial", Font.BOLD, 13));
            divertedLabel.setForeground(divertedColor);
            divertedLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
            delayReasonList.add(divertedLabel);
        } else if (flight.getDelays().isEmpty()) {
            // No specific reasons.
            if (flight.getDelayMinutes() > 15) {
                JLabel noReasonLabel = new JLabel("Delay occurred but no specific reasons recorded");
                noReasonLabel.setFont(valueFont);
                noReasonLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
                delayReasonList.add(noReasonLabel);
            } else {
                JLabel noDelayLabel = new JLabel("No significant delays reported");
                noDelayLabel.setFont(valueFont);
                noDelayLabel.setForeground(onTimeColor);
                noDelayLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
                delayReasonList.add(noDelayLabel);
            }
        } else {
            // Show each delay reason.
            for (Flight.Delay delay : flight.getDelays()) {
                JPanel reasonPanel = new JPanel(new BorderLayout());
                reasonPanel.setBackground(bgColor);
                reasonPanel.setBorder(new EmptyBorder(3, 0, 3, 0));

                JLabel reasonLabel = new JLabel(delay.getFormattedReason() + ":");
                reasonLabel.setFont(new Font("Arial", Font.BOLD, 13));

                JLabel minutesLabel = new JLabel(delay.getMinutes() + " minutes");
                minutesLabel.setFont(valueFont);

                // Color by severity
                if (delay.getMinutes() > 60) {
                    reasonLabel.setForeground(delayedColor);
                    minutesLabel.setForeground(delayedColor);
                } else if (delay.getMinutes() > 30) {
                    reasonLabel.setForeground(Color.ORANGE.darker());
                    minutesLabel.setForeground(Color.ORANGE.darker());
                }

                reasonPanel.add(reasonLabel, BorderLayout.WEST);
                reasonPanel.add(minutesLabel, BorderLayout.EAST);

                delayReasonList.add(reasonPanel);
            }
        }

        // Refresh display
        revalidate();
        repaint();
    }

    private String formatCancellationCode(String code) {
        if (code == null || code.isEmpty()) return "Unknown reason";

        switch (code.trim().toUpperCase()) {
            case "A": return "Airline operational issue";
            case "B": return "Adverse weather conditions";
            case "C": return "Air traffic control / National Air System";
            case "D": return "Security concern";
            default: return "Code: " + code;
        }
    }

    /**
     * Clears all flight details.
     */
    public void clearDetails() {
        // Reset status
        statusLabel.setText("No Flight Selected");
        statusLabel.setBackground(headerColor);

        // Reset flight info
        flightNumberValue.setText("N/A");
        dateValue.setText("N/A");
        airlineValue.setText("N/A");
        originValue.setText("N/A");
        destinationValue.setText("N/A");

        // Reset time info
        scheduledDepValue.setText("N/A");
        actualDepValue.setText("N/A");
        scheduledArrValue.setText("N/A");
        actualArrValue.setText("N/A");
        delayValue.setText("N/A");

        // Reset colors
        actualDepValue.setForeground(Color.BLACK);
        actualArrValue.setForeground(Color.BLACK);
        delayValue.setForeground(Color.BLACK);

        // Clear delay info
        delayReasonList.removeAll();
        JLabel noFlightLabel = new JLabel("No flight selected");
        noFlightLabel.setFont(valueFont);
        noFlightLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
        delayReasonList.add(noFlightLabel);

        revalidate();
        repaint();
    }
}