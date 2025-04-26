package ui;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Search panel for querying flight data with UK date formatting.
 */
public class SearchPanel extends JPanel {

    // UI appearance.
    private final Color panelBackground = new Color(245, 245, 250);
    private final Color borderColor = new Color(41, 128, 185);
    private final Font labelFont = new Font("Arial", Font.BOLD, 12);

    // Main input components.
    private final JComboBox<String> airlineComboBox;
    private final JTextField flightNumberField;
    private final JComboBox<String> originComboBox;
    private final JComboBox<String> destinationComboBox;
    private final JFormattedTextField startDateField;
    private final JFormattedTextField endDateField;
    private final JSpinner minDelaySpinner;
    private final JSpinner maxDelaySpinner;
    private final JComboBox<String> delayReasonComboBox;
    private final JCheckBox includeCancelledCheckbox;
    private final JCheckBox includeDivertedCheckbox;
    private final JButton searchButton;
    private final JButton clearButton;

    // UK date format - DD/MM/YYYY.
    private final DateTimeFormatter ukDateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public SearchPanel(List<String> airlines, List<String> airports,
                       ActionListener searchListener, ActionListener clearListener) {
        setLayout(new BorderLayout(10, 10));
        setBorder(new CompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(borderColor, 1),
                        "Search Flights"
                ),
                new EmptyBorder(10, 10, 10, 10)
        ));
        setBackground(panelBackground);

        // Create the two main panels.
        JPanel leftPanel = createPanel("Flight Information");
        JPanel rightPanel = createPanel("Date & Delay");

        // Set up components.
        airlineComboBox = createComboBox(airlines, "Select airline or type to search");
        flightNumberField = createTextField("e.g. BA123");
        originComboBox = createComboBox(airports, "Select origin airport");
        destinationComboBox = createComboBox(airports, "Select destination airport");

        // Date fields.
        startDateField = createDateField();
        endDateField = createDateField();
        setDateFieldToToday(startDateField);
        setDateFieldToToday(endDateField);

        // Spinner for delay values (max 24 hours).
        minDelaySpinner = new JSpinner(new SpinnerNumberModel(0, 0, 1440, 5));
        maxDelaySpinner = new JSpinner(new SpinnerNumberModel(0, 0, 1440, 5));

        // Delay reasons.
        String[] reasons = {
                "Any Reason", "Airline (CARRIER)", "Weather", "Air Traffic (NAS)",
                "Security", "Late Aircraft"
        };
        delayReasonComboBox = new JComboBox<>(reasons);

        // Flight status options.
        includeCancelledCheckbox = new JCheckBox("Include Cancelled Flights", true);
        includeDivertedCheckbox = new JCheckBox("Include Diverted Flights", true);

        // Build left panel (flight info).
        addFlightInfoControls(leftPanel);

        // Build right panel (dates and delays).
        addDateDelayControls(rightPanel);

        // Create and set up buttons.
        searchButton = new JButton("Search");
        searchButton.addActionListener(searchListener);
        searchButton.setBackground(borderColor);
        searchButton.setForeground(Color.BLACK);
        searchButton.setFont(new Font("Arial", Font.BOLD, 12));

        clearButton = new JButton("Clear");
        clearButton.addActionListener(clearListener);
        clearButton.setFont(new Font("Arial", Font.BOLD, 12));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(panelBackground);
        buttonPanel.add(clearButton);
        buttonPanel.add(searchButton);

        // Layout main panels.
        JPanel mainPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        mainPanel.setBackground(panelBackground);
        mainPanel.add(leftPanel);
        mainPanel.add(rightPanel);

        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    // Set up the flight information side.
    private void addFlightInfoControls(JPanel panel) {
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(5, 5, 5, 5);

        // Airline.
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        panel.add(createLabel("Airline:"), c);

        c.gridx = 1;
        c.weightx = 1;
        panel.add(airlineComboBox, c);

        // Flight number.
        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 0;
        panel.add(createLabel("Flight Number:"), c);

        c.gridx = 1;
        c.weightx = 1;
        panel.add(flightNumberField, c);

        // Origin.
        c.gridx = 0;
        c.gridy = 2;
        c.weightx = 0;
        panel.add(createLabel("Origin:"), c);

        c.gridx = 1;
        c.weightx = 1;
        panel.add(originComboBox, c);

        // Destination.
        c.gridx = 0;
        c.gridy = 3;
        c.weightx = 0;
        panel.add(createLabel("Destination:"), c);

        c.gridx = 1;
        c.weightx = 1;
        panel.add(destinationComboBox, c);

        // Status checkboxes.
        c.gridx = 0;
        c.gridy = 4;
        c.weightx = 0;
        c.gridwidth = 2;
        JPanel checkboxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        checkboxPanel.setBackground(panelBackground);
        checkboxPanel.add(includeCancelledCheckbox);
        checkboxPanel.add(includeDivertedCheckbox);
        panel.add(checkboxPanel, c);
    }

    // Set up the date and delay filter controls.
    private void addDateDelayControls(JPanel panel) {
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(5, 5, 5, 5);

        // Start date.
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        panel.add(createLabel("Start Date (DD/MM/YYYY):"), c);

        c.gridx = 1;
        c.weightx = 1;
        panel.add(startDateField, c);

        // End date.
        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 0;
        panel.add(createLabel("End Date (DD/MM/YYYY):"), c);

        c.gridx = 1;
        c.weightx = 1;
        panel.add(endDateField, c);

        // Min delay.
        c.gridx = 0;
        c.gridy = 2;
        c.weightx = 0;
        panel.add(createLabel("Min Delay (min):"), c);

        c.gridx = 1;
        c.weightx = 1;
        panel.add(minDelaySpinner, c);

        // Max delay.
        c.gridx = 0;
        c.gridy = 3;
        c.weightx = 0;
        panel.add(createLabel("Max Delay (min):"), c);

        c.gridx = 1;
        c.weightx = 1;
        panel.add(maxDelaySpinner, c);

        // Delay reason.
        c.gridx = 0;
        c.gridy = 4;
        c.weightx = 0;
        panel.add(createLabel("Delay Reason:"), c);

        c.gridx = 1;
        c.weightx = 1;
        panel.add(delayReasonComboBox, c);
    }

    // Creates a panel with a title.
    private JPanel createPanel(String title) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(title));
        panel.setBackground(panelBackground);
        return panel;
    }

    // Creates a standard label.
    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(labelFont);
        return label;
    }

    // Creates a text field with tooltip.
    private JTextField createTextField(String tooltip) {
        JTextField field = new JTextField();
        field.setToolTipText(tooltip);
        return field;
    }

    // Creates a combo box with the given items.
    private JComboBox<String> createComboBox(List<String> items, String tooltip) {
        JComboBox<String> comboBox = new JComboBox<>();
        comboBox.setEditable(true);
        comboBox.setToolTipText(tooltip);

        // Add empty option.
        comboBox.addItem("");

        // Add all items.
        if (items != null) {
            for (String item : items) {
                comboBox.addItem(item);
            }
        }

        return comboBox;
    }

    // Creates a date field.
    private JFormattedTextField createDateField() {
        JFormattedTextField field = new JFormattedTextField();
        field.setColumns(10);
        field.setToolTipText("DD/MM/YYYY");
        return field;
    }

    // Sets a date field to today's date.
    private void setDateFieldToToday(JFormattedTextField field) {
        field.setText(LocalDate.now().format(ukDateFormat));
    }

    /**
     * Resets all search fields to their default values.
     */
    public void clearFields() {
        // Reset dropdowns and text.
        airlineComboBox.setSelectedIndex(0);
        flightNumberField.setText("");
        originComboBox.setSelectedIndex(0);
        destinationComboBox.setSelectedIndex(0);

        // Reset dates.
        setDateFieldToToday(startDateField);
        setDateFieldToToday(endDateField);

        // Reset delay filters.
        minDelaySpinner.setValue(0);
        maxDelaySpinner.setValue(0);
        delayReasonComboBox.setSelectedIndex(0);

        // Reset checkboxes.
        includeCancelledCheckbox.setSelected(true);
        includeDivertedCheckbox.setSelected(true);
    }

    /**
     * Gets the selected airline or null if none selected.
     */
    public String getAirline() {
        String airline = (String) airlineComboBox.getSelectedItem();
        if (airline == null || airline.isEmpty()) {
            return null;
        }

        // Extract code if from format "XXX - Name".
        if (airline.contains(" - ")) {
            return airline.substring(0, airline.indexOf(" - "));
        }

        return airline;
    }

    /**
     * Gets the entered flight number or null if empty.
     */
    public String getFlightNumber() {
        String flightNumber = flightNumberField.getText().trim();
        return flightNumber.isEmpty() ? null : flightNumber;
    }

    /**
     * Gets the selected origin airport or null if none.
     */
    public String getOrigin() {
        String origin = (String) originComboBox.getSelectedItem();
        if (origin == null || origin.isEmpty()) {
            return null;
        }

        // Extract airport code if needed.
        if (origin.contains(" - ")) {
            return origin.substring(0, origin.indexOf(" - "));
        }

        return origin;
    }

    /**
     * Gets the selected destination airport or null if none.
     */
    public String getDestination() {
        String destination = (String) destinationComboBox.getSelectedItem();
        if (destination == null || destination.isEmpty()) {
            return null;
        }

        // Extract airport code if needed.
        if (destination.contains(" - ")) {
            return destination.substring(0, destination.indexOf(" - "));
        }

        return destination;
    }

    /**
     * Gets the selected start date in UK format.
     * Returns null if the date is invalid.
     */
    public LocalDate getStartDate() {
        try {
            String dateText = startDateField.getText().trim();
            if (dateText.isEmpty()) return null;
            return LocalDate.parse(dateText, ukDateFormat);
        } catch (DateTimeParseException e) {
            return null; // Invalid date format.
        }
    }

    /**
     * Gets the selected end date in UK format.
     * Returns null if the date is invalid.
     */
    public LocalDate getEndDate() {
        try {
            String dateText = endDateField.getText().trim();
            if (dateText.isEmpty()) return null;
            return LocalDate.parse(dateText, ukDateFormat);
        } catch (DateTimeParseException e) {
            return null; // Invalid date format.
        }
    }

    /**
     * Gets the minimum delay or null if zero.
     */
    public Integer getMinDelay() {
        int minDelay = (Integer) minDelaySpinner.getValue();
        return minDelay > 0 ? minDelay : null;
    }

    /**
     * Gets the maximum delay or null if zero.
     */
    public Integer getMaxDelay() {
        int maxDelay = (Integer) maxDelaySpinner.getValue();
        return maxDelay > 0 ? maxDelay : null;
    }

    /**
     * Gets the selected delay reason code.
     */
    public String getDelayReason() {
        int index = delayReasonComboBox.getSelectedIndex();
        if (index <= 0) return null;

        // Map UI labels to database codes.
        switch (index) {
            case 1: return "CARRIER";
            case 2: return "WEATHER";
            case 3: return "NAS";
            case 4: return "SECURITY";
            case 5: return "LATE_AIRCRAFT";
            default: return null;
        }
    }

    /**
     * Checks if cancelled flights should be included.
     */
    public boolean includeCancelled() {
        return includeCancelledCheckbox.isSelected();
    }

    /**
     * Checks if diverted flights should be included.
     */
    public boolean includeDiverted() {
        return includeDivertedCheckbox.isSelected();
    }
}