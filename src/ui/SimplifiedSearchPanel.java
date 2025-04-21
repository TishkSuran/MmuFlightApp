package ui;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

/**
 * Simplified search panel with UK-specific formatting.
 * Features a cleaner interface with UK date formats (ddMMyyyy).
 */
public class SimplifiedSearchPanel extends JPanel {

    // Colors for a clean, professional look
    private final Color panelBackground = new Color(245, 245, 250);
    private final Color borderColor = new Color(41, 128, 185);
    private final Font labelFont = new Font("Arial", Font.BOLD, 12);

    // Search components
    private final JComboBox<String> airlineComboBox;
    private final JTextField flightNumberField;
    private final JComboBox<String> originComboBox;
    private final JComboBox<String> destinationComboBox;

    // UK formatted date fields
    private final JFormattedTextField startDateField;
    private final JFormattedTextField endDateField;

    // Delay filter components
    private final JSpinner minDelaySpinner;
    private final JSpinner maxDelaySpinner;
    private final JComboBox<String> delayReasonComboBox;

    // Status filter components
    private final JCheckBox includeCancelledCheckbox;
    private final JCheckBox includeDivertedCheckbox;

    // Action buttons
    private final JButton searchButton;
    private final JButton clearButton;

    // UK date formatter - DD/MM/YYYY
    private final DateTimeFormatter ukDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Creates a new simplified search panel with UK formatting.
     */
    public SimplifiedSearchPanel(List<String> airlines, List<String> airports,
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

        // Create main panels for organization
        JPanel leftPanel = createPanel("Flight Information");
        JPanel rightPanel = createPanel("Date & Delay");

        // Create components with UK specifications
        airlineComboBox = createComboBox(airlines, "Start typing to filter or select airline");
        flightNumberField = createTextField("e.g. BA123");
        originComboBox = createComboBox(airports, "Start typing to filter or select origin airport");
        destinationComboBox = createComboBox(airports, "Start typing to filter or select destination airport");

        // UK formatted date fields
        startDateField = createDateField();
        endDateField = createDateField();

        // Set to current date as default
        setDateFieldToToday(startDateField);
        setDateFieldToToday(endDateField);

        // Delay components
        SpinnerNumberModel minDelayModel = new SpinnerNumberModel(0, 0, 1440, 5);
        minDelaySpinner = new JSpinner(minDelayModel);

        SpinnerNumberModel maxDelayModel = new SpinnerNumberModel(0, 0, 1440, 5);
        maxDelaySpinner = new JSpinner(maxDelayModel);

        // Delay reason dropdown
        delayReasonComboBox = new JComboBox<>(new String[]{
                "Any Reason", "Airline (CARRIER)", "Weather", "Air Traffic (NAS)",
                "Security", "Late Aircraft"
        });

        // Status filters
        includeCancelledCheckbox = new JCheckBox("Include Cancelled Flights", true);
        includeDivertedCheckbox = new JCheckBox("Include Diverted Flights", true);

        // Build left panel - flight information
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(5, 5, 5, 5);
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.weightx = 0;

        leftPanel.add(createLabel("Airline:"), c);

        c.gridx = 1;
        c.weightx = 1;
        leftPanel.add(airlineComboBox, c);

        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 0;
        leftPanel.add(createLabel("Flight Number:"), c);

        c.gridx = 1;
        c.weightx = 1;
        leftPanel.add(flightNumberField, c);

        c.gridx = 0;
        c.gridy = 2;
        c.weightx = 0;
        leftPanel.add(createLabel("Origin:"), c);

        c.gridx = 1;
        c.weightx = 1;
        leftPanel.add(originComboBox, c);

        c.gridx = 0;
        c.gridy = 3;
        c.weightx = 0;
        leftPanel.add(createLabel("Destination:"), c);

        c.gridx = 1;
        c.weightx = 1;
        leftPanel.add(destinationComboBox, c);

        c.gridx = 0;
        c.gridy = 4;
        c.weightx = 0;
        c.gridwidth = 2;
        JPanel checkboxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        checkboxPanel.setBackground(panelBackground);
        checkboxPanel.add(includeCancelledCheckbox);
        checkboxPanel.add(includeDivertedCheckbox);
        leftPanel.add(checkboxPanel, c);

        // Build right panel - date and delay information
        c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(5, 5, 5, 5);
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.weightx = 0;

        rightPanel.add(createLabel("Start Date (DD/MM/YYYY):"), c);

        c.gridx = 1;
        c.weightx = 1;
        rightPanel.add(startDateField, c);

        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 0;
        rightPanel.add(createLabel("End Date (DD/MM/YYYY):"), c);

        c.gridx = 1;
        c.weightx = 1;
        rightPanel.add(endDateField, c);

        c.gridx = 0;
        c.gridy = 2;
        c.weightx = 0;
        rightPanel.add(createLabel("Min Delay (min):"), c);

        c.gridx = 1;
        c.weightx = 1;
        rightPanel.add(minDelaySpinner, c);

        c.gridx = 0;
        c.gridy = 3;
        c.weightx = 0;
        rightPanel.add(createLabel("Max Delay (min):"), c);

        c.gridx = 1;
        c.weightx = 1;
        rightPanel.add(maxDelaySpinner, c);

        c.gridx = 0;
        c.gridy = 4;
        c.weightx = 0;
        rightPanel.add(createLabel("Delay Reason:"), c);

        c.gridx = 1;
        c.weightx = 1;
        rightPanel.add(delayReasonComboBox, c);

        // Create button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(panelBackground);

        searchButton = new JButton("Search");
        searchButton.setBackground(borderColor);
        searchButton.setForeground(Color.WHITE);
        searchButton.setFont(new Font("Arial", Font.BOLD, 12));
        searchButton.addActionListener(searchListener);

        clearButton = new JButton("Clear");
        clearButton.setFont(new Font("Arial", Font.BOLD, 12));
        clearButton.addActionListener(clearListener);

        buttonPanel.add(clearButton);
        buttonPanel.add(searchButton);

        // Layout panels
        JPanel mainPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        mainPanel.setBackground(panelBackground);
        mainPanel.add(leftPanel);
        mainPanel.add(rightPanel);

        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * Creates a styled panel with title.
     */
    private JPanel createPanel(String title) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(title));
        panel.setBackground(panelBackground);
        return panel;
    }

    /**
     * Creates a styled label.
     */
    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(labelFont);
        return label;
    }

    /**
     * Creates a styled text field with placeholder.
     */
    private JTextField createTextField(String placeholder) {
        JTextField field = new JTextField();
        field.setToolTipText(placeholder);
        return field;
    }

    /**
     * Creates a combo box with items and autocomplete.
     */
    private JComboBox<String> createComboBox(List<String> items, String tooltip) {
        JComboBox<String> comboBox = new JComboBox<>();
        comboBox.setEditable(true);
        comboBox.setToolTipText(tooltip);

        // Add empty option
        comboBox.addItem("");

        // Add items
        for (String item : items) {
            comboBox.addItem(item);
        }

        return comboBox;
    }

    /**
     * Creates a UK formatted date field (DD/MM/YYYY).
     */
    private JFormattedTextField createDateField() {
        JFormattedTextField field = new JFormattedTextField();
        field.setColumns(10);

        // Set placeholder/tool tip
        field.setToolTipText("DD/MM/YYYY");

        return field;
    }

    /**
     * Sets a date field to today's date in UK format.
     */
    private void setDateFieldToToday(JFormattedTextField field) {
        field.setText(LocalDate.now().format(ukDateFormatter));
    }

    /**
     * Clears all search fields and resets to defaults.
     */
    public void clearFields() {
        airlineComboBox.setSelectedIndex(0);
        flightNumberField.setText("");
        originComboBox.setSelectedIndex(0);
        destinationComboBox.setSelectedIndex(0);

        // Reset dates to today
        setDateFieldToToday(startDateField);
        setDateFieldToToday(endDateField);

        minDelaySpinner.setValue(0);
        maxDelaySpinner.setValue(0);
        delayReasonComboBox.setSelectedIndex(0);
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

        // Extract airline code if a selection was made from dropdown
        if (airline.contains(" - ")) {
            return airline.substring(0, airline.indexOf(" - "));
        }

        return airline;
    }

    /**
     * Gets the entered flight number or null if none entered.
     */
    public String getFlightNumber() {
        String flightNumber = flightNumberField.getText().trim();
        return flightNumber.isEmpty() ? null : flightNumber;
    }

    /**
     * Gets the selected origin airport or null if none selected.
     */
    public String getOrigin() {
        String origin = (String) originComboBox.getSelectedItem();
        if (origin == null || origin.isEmpty()) {
            return null;
        }

        // Extract airport code if a selection was made from dropdown
        if (origin.contains(" - ")) {
            return origin.substring(0, origin.indexOf(" - "));
        }

        return origin;
    }

    /**
     * Gets the selected destination airport or null if none selected.
     */
    public String getDestination() {
        String destination = (String) destinationComboBox.getSelectedItem();
        if (destination == null || destination.isEmpty()) {
            return null;
        }

        // Extract airport code if a selection was made from dropdown
        if (destination.contains(" - ")) {
            return destination.substring(0, destination.indexOf(" - "));
        }

        return destination;
    }

    /**
     * Gets the selected start date in UK format or null if invalid.
     */
    public LocalDate getStartDate() {
        try {
            String dateText = startDateField.getText().trim();
            if (dateText.isEmpty()) return null;

            // Parse date in UK format (DD/MM/YYYY)
            return LocalDate.parse(dateText, ukDateFormatter);
        } catch (Exception e) {
            // If parsing fails, return null
            return null;
        }
    }

    /**
     * Gets the selected end date in UK format or null if invalid.
     */
    public LocalDate getEndDate() {
        try {
            String dateText = endDateField.getText().trim();
            if (dateText.isEmpty()) return null;

            // Parse date in UK format (DD/MM/YYYY)
            return LocalDate.parse(dateText, ukDateFormatter);
        } catch (Exception e) {
            // If parsing fails, return null
            return null;
        }
    }

    /**
     * Gets the minimum delay or null if not specified.
     */
    public Integer getMinDelay() {
        int minDelay = (Integer) minDelaySpinner.getValue();
        return minDelay > 0 ? minDelay : null;
    }

    /**
     * Gets the maximum delay or null if not specified.
     */
    public Integer getMaxDelay() {
        int maxDelay = (Integer) maxDelaySpinner.getValue();
        return maxDelay > 0 ? maxDelay : null;
    }

    /**
     * Gets the selected delay reason or null if none selected.
     */
    public String getDelayReason() {
        int index = delayReasonComboBox.getSelectedIndex();
        if (index <= 0) return null;

        // Convert user-friendly names to database codes
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
     * Checks if cancelled flights should be included in search results.
     */
    public boolean includeCancelled() {
        return includeCancelledCheckbox.isSelected();
    }

    /**
     * Checks if diverted flights should be included in search results.
     */
    public boolean includeDiverted() {
        return includeDivertedCheckbox.isSelected();
    }
}