package ui;

import flightModel.Flight;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.format.DateTimeFormatter;


public class FlightDetailPanel extends JPanel {

    // UI constants
    private final Color headerColor = new Color(41, 128, 185);
    private final Color bgColor = new Color(245, 245, 250);
    private final Font headerFont = new Font("Arial", Font.BOLD, 14);
    private final Font valueFont = new Font("Arial", Font.PLAIN, 13);
    private final Font statusFont = new Font("Arial", Font.BOLD, 16);

    // Status colors
    private final Color onTimeColor = new Color(46, 204, 113);
    private final Color delayedColor = new Color(231, 76, 60);
    private final Color cancelledColor = new Color(192, 57, 43);
    private final Color divertedColor = new Color(230, 126, 34);

    // UK date formatter - DD/MM/YYYY
    private final DateTimeFormatter ukDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Detail components
    private final JLabel statusLabel = new JLabel("No Flight Selected", JLabel.CENTER);
    private final JPanel flightInfoPanel = new JPanel(new GridBagLayout());
    private final JPanel timeInfoPanel = new JPanel(new GridBagLayout());
    private final JPanel delayInfoPanel = new JPanel(new BorderLayout());

    // Flight information fields
    private final JLabel flightNumberValue = new JLabel();
    private final JLabel dateValue = new JLabel();
    private final JLabel airlineValue = new JLabel();
    private final JLabel originValue = new JLabel();
    private final JLabel destinationValue = new JLabel();

    // Time information fields
    private final JLabel scheduledDepValue = new JLabel();
    private final JLabel actualDepValue = new JLabel();
    private final JLabel scheduledArrValue = new JLabel();
    private final JLabel actualArrValue = new JLabel();
    private final JLabel delayValue = new JLabel();

    // Delay reason panel
    private final JPanel delayReasonList = new JPanel();

    /**
     * Creates a new flight detail panel with enhanced UK-style formatting.
     */
    public FlightDetailPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(bgColor);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // Status bar at top
        setupStatusBar();

        // Main content in the center
        JPanel contentPanel = new JPanel(new BorderLayout(15, 15));
        contentPanel.setBackground(bgColor);
        contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Configure info panels
        setupFlightInfoPanel();
        setupTimeInfoPanel();
        setupDelayInfoPanel();

        // Add info panels to content panel with proper layout
        JPanel topInfoPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        topInfoPanel.setBackground(bgColor);
        topInfoPanel.add(flightInfoPanel);
        topInfoPanel.add(timeInfoPanel);

        contentPanel.add(topInfoPanel, BorderLayout.CENTER);
        contentPanel.add(delayInfoPanel, BorderLayout.SOUTH);

        // Add components to main panel
        add(statusLabel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);

        // Initial state
        clearDetails();
    }

    /**
     * Sets up the status bar with styling.
     */
    private void setupStatusBar() {
        statusLabel.setFont(statusFont);
        statusLabel.setOpaque(true);
        statusLabel.setBackground(headerColor);
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setBorder(new EmptyBorder(8, 10, 8, 10));
    }

    /**
     * Sets up the flight information panel with clear headers and values.
     */
    private void setupFlightInfoPanel() {
        flightInfoPanel.setBackground(bgColor);
        flightInfoPanel.setBorder(createTitledBorder("Flight Information"));

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(4, 8, 4, 8);

        // Flight Number
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.WEST;
        flightInfoPanel.add(createHeaderLabel("Flight Number:"), c);

        c.gridx = 1;
        c.weightx = 1.0;
        flightInfoPanel.add(flightNumberValue, c);

        // Date
        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 0.0;
        flightInfoPanel.add(createHeaderLabel("Date:"), c);

        c.gridx = 1;
        c.weightx = 1.0;
        flightInfoPanel.add(dateValue, c);

        // Airline
        c.gridx = 0;
        c.gridy = 2;
        c.weightx = 0.0;
        flightInfoPanel.add(createHeaderLabel("Airline:"), c);

        c.gridx = 1;
        c.weightx = 1.0;
        flightInfoPanel.add(airlineValue, c);

        // Origin
        c.gridx = 0;
        c.gridy = 3;
        c.weightx = 0.0;
        flightInfoPanel.add(createHeaderLabel("Origin:"), c);

        c.gridx = 1;
        c.weightx = 1.0;
        flightInfoPanel.add(originValue, c);

        // Destination
        c.gridx = 0;
        c.gridy = 4;
        c.weightx = 0.0;
        flightInfoPanel.add(createHeaderLabel("Destination:"), c);

        c.gridx = 1;
        c.weightx = 1.0;
        flightInfoPanel.add(destinationValue, c);
    }

    /**
     * Sets up the time information panel with clear headers and values.
     */
    private void setupTimeInfoPanel() {
        timeInfoPanel.setBackground(bgColor);
        timeInfoPanel.setBorder(createTitledBorder("Time Information"));

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(4, 8, 4, 8);

        // Scheduled Departure
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.WEST;
        timeInfoPanel.add(createHeaderLabel("Scheduled Departure:"), c);

        c.gridx = 1;
        c.weightx = 1.0;
        timeInfoPanel.add(scheduledDepValue, c);

        // Actual Departure
        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 0.0;
        timeInfoPanel.add(createHeaderLabel("Actual Departure:"), c);

        c.gridx = 1;
        c.weightx = 1.0;
        timeInfoPanel.add(actualDepValue, c);

        // Scheduled Arrival
        c.gridx = 0;
        c.gridy = 2;
        c.weightx = 0.0;
        timeInfoPanel.add(createHeaderLabel("Scheduled Arrival:"), c);

        c.gridx = 1;
        c.weightx = 1.0;
        timeInfoPanel.add(scheduledArrValue, c);

        // Actual Arrival
        c.gridx = 0;
        c.gridy = 3;
        c.weightx = 0.0;
        timeInfoPanel.add(createHeaderLabel("Actual Arrival:"), c);

        c.gridx = 1;
        c.weightx = 1.0;
        timeInfoPanel.add(actualArrValue, c);

        // Delay
        c.gridx = 0;
        c.gridy = 4;
        c.weightx = 0.0;
        timeInfoPanel.add(createHeaderLabel("Delay (minutes):"), c);

        c.gridx = 1;
        c.weightx = 1.0;
        timeInfoPanel.add(delayValue, c);
    }

    /**
     * Sets up the delay information panel with clear headers and values.
     */
    private void setupDelayInfoPanel() {
        delayInfoPanel.setBackground(bgColor);
        delayInfoPanel.setBorder(createTitledBorder("Delay Information"));

        // Configure delay reason list
        delayReasonList.setLayout(new BoxLayout(delayReasonList, BoxLayout.Y_AXIS));
        delayReasonList.setBackground(bgColor);

        JScrollPane scrollPane = new JScrollPane(delayReasonList);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setBackground(bgColor);

        delayInfoPanel.add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * Creates a header label with consistent styling.
     */
    private JLabel createHeaderLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(headerFont);
        label.setForeground(headerColor);
        return label;
    }

    /**
     * Creates a value label with consistent styling.
     */
    private JLabel createValueLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(valueFont);
        return label;
    }

    /**
     * Creates a styled titled border for panels.
     */
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
     * Displays the details of the specified flight with UK formatting.
     * @param flight the flight to display
     */
    public void setFlight(Flight flight) {
        if (flight == null) {
            clearDetails();
            return;
        }

        // Update status header with appropriate color
        String status = flight.getStatus();
        statusLabel.setText(status);

        // Set status color based on flight status
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

        // Update flight information
        flightNumberValue.setText(flight.getFullFlightNumber());

        // Format date in UK style (DD/MM/YYYY)
        if (flight.getDate() != null) {
            dateValue.setText(flight.getDate().format(ukDateFormatter));
        } else {
            dateValue.setText("N/A");
        }

        airlineValue.setText(flight.getAirlineName());
        originValue.setText(flight.getOriginDisplay());
        destinationValue.setText(flight.getDestinationDisplay());

        // Update time information
        scheduledDepValue.setText(Flight.formatTime(flight.getScheduledDeparture()));

        // Display appropriate text for actual times based on flight status
        if (flight.isCancelled()) {
            actualDepValue.setText("Cancelled");
            actualArrValue.setText("Cancelled");

            // Set text color for cancelled values
            actualDepValue.setForeground(cancelledColor);
            actualArrValue.setForeground(cancelledColor);

            delayValue.setText("N/A");
            delayValue.setForeground(Color.BLACK);
        } else {
            actualDepValue.setText(Flight.formatTime(flight.getActualDeparture()));
            actualDepValue.setForeground(Color.BLACK);

            // For diverted flights, indicate the diversion
            if (flight.isDiverted()) {
                actualArrValue.setText("Diverted");
                actualArrValue.setForeground(divertedColor);
                delayValue.setText("N/A");
                delayValue.setForeground(Color.BLACK);
            } else {
                actualArrValue.setText(Flight.formatTime(flight.getActualArrival()));
                actualArrValue.setForeground(Color.BLACK);

                // Show delay with color coding
                int delayMinutes = flight.getDelayMinutes();
                delayValue.setText(String.valueOf(delayMinutes));

                // Format delay label based on delay severity
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

        // Clear and update delay reasons
        delayReasonList.removeAll();

        if (flight.isCancelled()) {
            // Show cancellation reason
            String cancellationCode = flight.getCancellationCode();
            String cancelReason = formatCancellationCode(cancellationCode);

            JLabel cancelLabel = new JLabel("Flight cancelled due to: " + cancelReason);
            cancelLabel.setFont(new Font("Arial", Font.BOLD, 13));
            cancelLabel.setForeground(cancelledColor);
            cancelLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
            delayReasonList.add(cancelLabel);

        } else if (flight.isDiverted()) {
            // Show diversion information
            JLabel divertedLabel = new JLabel("Flight was diverted to another airport");
            divertedLabel.setFont(new Font("Arial", Font.BOLD, 13));
            divertedLabel.setForeground(divertedColor);
            divertedLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
            delayReasonList.add(divertedLabel);

        } else if (flight.getDelays().isEmpty()) {
            // No specific delay reasons
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
            // Display each delay reason with details
            for (Flight.Delay delay : flight.getDelays()) {
                JPanel reasonPanel = new JPanel(new BorderLayout());
                reasonPanel.setBackground(bgColor);
                reasonPanel.setBorder(new EmptyBorder(3, 0, 3, 0));

                JLabel reasonLabel = new JLabel(delay.getFormattedReason() + ":");
                reasonLabel.setFont(new Font("Arial", Font.BOLD, 13));

                JLabel minutesLabel = new JLabel(delay.getMinutes() + " minutes");
                minutesLabel.setFont(valueFont);

                // Color code by delay severity
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

        // Force refresh
        revalidate();
        repaint();
    }

    /**
     * Formats a cancellation code into a user-friendly description.
     */
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
     * Clears all flight details from the panel.
     */
    public void clearDetails() {
        // Reset status
        statusLabel.setText("No Flight Selected");
        statusLabel.setBackground(headerColor);

        // Reset flight information
        flightNumberValue.setText("N/A");
        dateValue.setText("N/A");
        airlineValue.setText("N/A");
        originValue.setText("N/A");
        destinationValue.setText("N/A");

        // Reset time information
        scheduledDepValue.setText("N/A");
        actualDepValue.setText("N/A");
        scheduledArrValue.setText("N/A");
        actualArrValue.setText("N/A");
        delayValue.setText("N/A");

        // Reset colors
        actualDepValue.setForeground(Color.BLACK);
        actualArrValue.setForeground(Color.BLACK);
        delayValue.setForeground(Color.BLACK);

        // Clear delay reasons
        delayReasonList.removeAll();
        JLabel noFlightLabel = new JLabel("No flight selected");
        noFlightLabel.setFont(valueFont);
        noFlightLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
        delayReasonList.add(noFlightLabel);

        revalidate();
        repaint();
    }
}