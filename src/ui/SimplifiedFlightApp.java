package ui;

import flightModel.Flight;
import service.FlightDataService;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Simplified Flight Punctuality Application for UK users.
 * Features a cleaner interface with UK date formats (ddMMyyyy).
 */
public class SimplifiedFlightApp extends JFrame {

    private final FlightDataService dataService;
    private final ui.FlightTableModel tableModel;
    private final JTable flightTable;
    private final SimplifiedFlightDetailPanel detailPanel;
    private final SimplifiedSearchPanel searchPanel;
    private final SimplifiedAnalysisPanel analysisPanel;
    private final JLabel statusLabel;
    private final Color primaryColor = new Color(41, 128, 185); // A nice blue shade
    private final Font headerFont = new Font("Arial", Font.BOLD, 14);

    /**
     * Creates and initializes the simplified application frame.
     * @throws SQLException if a database access error occurs
     */
    public SimplifiedFlightApp() throws SQLException {
        super("UK Flight Punctuality Tracker");

        // Set the application icon if available
        try {
            // You would need to add this icon file to your resources
            // setIconImage(new ImageIcon(getClass().getResource("/images/app_icon.png")).getImage());
        } catch (Exception e) {
            System.err.println("Icon not found: " + e.getMessage());
        }

        // Set up data service
        dataService = new FlightDataService();

        // Set up main components with UK-specific customizations
        tableModel = new FlightTableModel();
        flightTable = createStyledTable(tableModel);

        detailPanel = new SimplifiedFlightDetailPanel();

        // Initialize search panel with UK date format
        searchPanel = new SimplifiedSearchPanel(
                dataService.getAirlines(),
                dataService.getAirports(),
                this::handleSearch,
                this::handleClear
        );

        analysisPanel = new SimplifiedAnalysisPanel();
        statusLabel = new JLabel("Ready");
        statusLabel.setBorder(new EmptyBorder(3, 10, 3, 10));

        // Configure the UI with British styling
        configureUI();

        // Add a window listener to handle cleanup
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    dataService.disconnect();
                    System.out.println("Database connection closed");
                } catch (SQLException ex) {
                    System.err.println("Error disconnecting from database: " + ex.getMessage());
                }
            }
        });

        // Set up the frame
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
    }

    /**
     * Creates a styled JTable with improved visuals.
     */
    private JTable createStyledTable(FlightTableModel model) {
        JTable table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        table.setRowHeight(25); // Slightly larger rows for better readability
        table.getTableHeader().setFont(headerFont);
        table.getTableHeader().setBackground(primaryColor);
        table.getTableHeader().setForeground(Color.WHITE);

        // Alternate row colors for better readability
        table.setDefaultRenderer(Object.class, new StripedRowRenderer());

        return table;
    }

    /**
     * Configures the user interface components with improved layout.
     */
    private void configureUI() {
        // Set up flight table
        flightTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = flightTable.getSelectedRow();
                if (selectedRow >= 0) {
                    int modelRow = flightTable.convertRowIndexToModel(selectedRow);
                    Flight selectedFlight = tableModel.getFlightAt(modelRow);
                    detailPanel.setFlight(selectedFlight);
                } else {
                    detailPanel.clearDetails();
                }
            }
        });

        // Create table sorter
        TableRowSorter<FlightTableModel> sorter = new TableRowSorter<>(tableModel);
        flightTable.setRowSorter(sorter);

        // Create scroll pane for table with styled border
        JScrollPane tableScrollPane = new JScrollPane(flightTable);
        tableScrollPane.setBorder(createTitledBorder("Flight Results"));

        // Create tabbed pane for right side panels
        JTabbedPane rightTabs = new JTabbedPane();
        rightTabs.setFont(headerFont);

        // Create details panel with styled border
        JPanel detailsContainer = new JPanel(new BorderLayout());
        detailsContainer.add(detailPanel, BorderLayout.CENTER);
        rightTabs.addTab("Flight Details", detailsContainer);

        // Create analysis tab
        rightTabs.addTab("Analysis", analysisPanel);

        // Create main split pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(750);
        splitPane.setResizeWeight(0.6); // Give more weight to the left side when resizing

        // Left panel with search and table
        JPanel leftPanel = new JPanel(new BorderLayout(0, 10));
        leftPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        leftPanel.add(searchPanel, BorderLayout.NORTH);
        leftPanel.add(tableScrollPane, BorderLayout.CENTER);

        // Add components to split pane
        splitPane.setLeftComponent(leftPanel);
        splitPane.setRightComponent(rightTabs);

        // Create toolbar with quick actions
        JToolBar toolBar = createToolBar();

        // Create status bar
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY),
                new EmptyBorder(3, 10, 3, 10)
        ));
        statusBar.add(statusLabel, BorderLayout.WEST);

        // Add help button to status bar
        JButton helpButton = new JButton("Help");
        helpButton.addActionListener(e -> showHelp());
        statusBar.add(helpButton, BorderLayout.EAST);

        // Add components to frame
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(toolBar, BorderLayout.NORTH);
        getContentPane().add(splitPane, BorderLayout.CENTER);
        getContentPane().add(statusBar, BorderLayout.SOUTH);

        // Create menu bar
        setJMenuBar(createMenuBar());
    }

    /**
     * Creates a toolbar with quick access buttons.
     */
    private JToolBar createToolBar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
                new EmptyBorder(3, 10, 3, 10)
        ));

        JButton searchButton = new JButton("Search");
        searchButton.setFont(headerFont);
        searchButton.addActionListener(this::handleSearch);

        JButton clearButton = new JButton("Clear");
        clearButton.setFont(headerFont);
        clearButton.addActionListener(this::handleClear);

        JButton airlineAnalysisButton = new JButton("Airline Analysis");
        airlineAnalysisButton.setFont(headerFont);
        airlineAnalysisButton.addActionListener(this::handleAirlineAnalysis);

        JButton airportAnalysisButton = new JButton("Airport Analysis");
        airportAnalysisButton.setFont(headerFont);
        airportAnalysisButton.addActionListener(this::handleAirportAnalysis);

        JButton exportButton = new JButton("Export Results");
        exportButton.setFont(headerFont);
        exportButton.addActionListener(this::handleExport);

        toolBar.add(searchButton);
        toolBar.add(clearButton);
        toolBar.addSeparator(new Dimension(20, 0));
        toolBar.add(airlineAnalysisButton);
        toolBar.add(airportAnalysisButton);
        toolBar.addSeparator(new Dimension(20, 0));
        toolBar.add(exportButton);

        return toolBar;
    }

    /**
     * Creates the application menu bar with enhanced organization.
     */
    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // File menu
        JMenu fileMenu = new JMenu("File");
        fileMenu.add(new JMenuItem("Export Results")).addActionListener(this::handleExport);
        fileMenu.add(new JMenuItem("Print")).addActionListener(e ->
                JOptionPane.showMessageDialog(this, "Print functionality not implemented yet.")
        );
        fileMenu.addSeparator();
        fileMenu.add(new JMenuItem("Exit")).addActionListener(e -> System.exit(0));

        // Analysis menu
        JMenu analysisMenu = new JMenu("Analysis");
        JMenuItem airlineAnalysisItem = new JMenuItem("Airline Delays by Year");
        airlineAnalysisItem.addActionListener(this::handleAirlineAnalysis);
        analysisMenu.add(airlineAnalysisItem);

        JMenuItem airportAnalysisItem = new JMenuItem("Airport Delays by Year");
        airportAnalysisItem.addActionListener(this::handleAirportAnalysis);
        analysisMenu.add(airportAnalysisItem);

        JMenuItem timeSeriesItem = new JMenuItem("Airport Delays Over Time");
        timeSeriesItem.addActionListener(this::handleTimeSeriesAnalysis);
        analysisMenu.add(timeSeriesItem);

        // Help menu
        JMenu helpMenu = new JMenu("Help");
        helpMenu.add(new JMenuItem("User Guide")).addActionListener(e -> showHelp());
        helpMenu.add(new JMenuItem("About")).addActionListener(e -> showAbout());

        menuBar.add(fileMenu);
        menuBar.add(analysisMenu);
        menuBar.add(helpMenu);

        return menuBar;
    }

    /**
     * Creates a styled titled border for panels.
     */
    private Border createTitledBorder(String title) {
        TitledBorder titledBorder = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(primaryColor, 1),
                title
        );
        titledBorder.setTitleFont(headerFont);
        titledBorder.setTitleColor(primaryColor);

        return new CompoundBorder(
                titledBorder,
                new EmptyBorder(5, 5, 5, 5)
        );
    }

    /**
     * Handles search button click event with improved feedback.
     */
    private void handleSearch(ActionEvent e) {
        try {
            statusLabel.setText("Searching...");
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            // Get search criteria
            String airline = searchPanel.getAirline();
            String flightNumber = searchPanel.getFlightNumber();
            String origin = searchPanel.getOrigin();
            String destination = searchPanel.getDestination();
            LocalDate startDate = searchPanel.getStartDate();
            LocalDate endDate = searchPanel.getEndDate();
            Integer minDelay = searchPanel.getMinDelay();
            Integer maxDelay = searchPanel.getMaxDelay();
            String delayReason = searchPanel.getDelayReason();

            // Build search description for status
            StringBuilder searchDesc = new StringBuilder("Searching for flights");
            if (airline != null && !airline.isEmpty())
                searchDesc.append(" with airline ").append(airline);
            if (origin != null && !origin.isEmpty())
                searchDesc.append(" from ").append(origin);
            if (destination != null && !destination.isEmpty())
                searchDesc.append(" to ").append(destination);

            statusLabel.setText(searchDesc.toString());

            // Perform search
            List<Flight> results = dataService.searchFlights(
                    airline, flightNumber, origin, destination,
                    startDate, endDate, minDelay, maxDelay, delayReason
            );

            // Update table
            tableModel.setFlights(results);

            // Clear selection
            flightTable.clearSelection();

            // Update status with detailed results
            String resultText = "Found " + results.size() + " matching flights";
            if (results.size() > 0) {
                resultText += " (" + countDelayedFlights(results) + " delayed, " +
                        countCancelledFlights(results) + " cancelled)";
            }
            statusLabel.setText(resultText);

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Database error: " + ex.getMessage(),
                    "Search Error", JOptionPane.ERROR_MESSAGE);
            statusLabel.setText("Search failed");
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    /**
     * Counts flights with delays in search results.
     */
    private int countDelayedFlights(List<Flight> flights) {
        int count = 0;
        for (Flight flight : flights) {
            if (!flight.isCancelled() && !flight.isDiverted() && flight.getDelayMinutes() > 0) {
                count++;
            }
        }
        return count;
    }

    /**
     * Counts cancelled flights in search results.
     */
    private int countCancelledFlights(List<Flight> flights) {
        int count = 0;
        for (Flight flight : flights) {
            if (flight.isCancelled()) {
                count++;
            }
        }
        return count;
    }

    /**
     * Handles clear button click with improved feedback.
     */
    private void handleClear(ActionEvent e) {
        searchPanel.clearFields();
        tableModel.setFlights(new java.util.ArrayList<>());
        detailPanel.clearDetails();
        statusLabel.setText("Search criteria cleared");
    }

    /**
     * Handles airline analysis with UK date presentation.
     */
    private void handleAirlineAnalysis(ActionEvent e) {
        Object[] years = {"2019", "2020", "2021", "2022", "2023"};
        Object selectedYear = JOptionPane.showInputDialog(
                this,
                "Select year for analysis:",
                "Airline Delay Analysis",
                JOptionPane.QUESTION_MESSAGE,
                null,
                years,
                "2023"
        );

        if (selectedYear != null) {
            try {
                int year = Integer.parseInt(selectedYear.toString());

                statusLabel.setText("Analyzing airline delays for " + year + "...");
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

                // Get data
                Map<String, Double> data = dataService.getAverageDelayByAirline(year);

                // Show chart
                analysisPanel.showAirlineDelayChart(data, year);

                // Switch to analysis tab automatically
                ((JTabbedPane)analysisPanel.getParent()).setSelectedComponent(analysisPanel);

                statusLabel.setText("Airline delay analysis completed for " + year);

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(
                        this,
                        "Database error: " + ex.getMessage(),
                        "Analysis Error",
                        JOptionPane.ERROR_MESSAGE
                );
                statusLabel.setText("Analysis failed");
            } finally {
                setCursor(Cursor.getDefaultCursor());
            }
        }
    }

    /**
     * Handles airport analysis with UK date presentation.
     */
    private void handleAirportAnalysis(ActionEvent e) {
        Object[] years = {"2019", "2020", "2021", "2022", "2023"};
        Object selectedYear = JOptionPane.showInputDialog(
                this,
                "Select year for analysis:",
                "Airport Delay Analysis",
                JOptionPane.QUESTION_MESSAGE,
                null,
                years,
                "2023"
        );

        if (selectedYear != null) {
            try {
                int year = Integer.parseInt(selectedYear.toString());

                statusLabel.setText("Analyzing airport delays for " + year + "...");
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

                // Get data
                Map<String, Double> data = dataService.getAverageDelayByAirport(year);

                // Show chart
                analysisPanel.showAirportDelayChart(data, year);

                // Switch to analysis tab automatically
                ((JTabbedPane)analysisPanel.getParent()).setSelectedComponent(analysisPanel);

                statusLabel.setText("Airport delay analysis completed for " + year);

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(
                        this,
                        "Database error: " + ex.getMessage(),
                        "Analysis Error",
                        JOptionPane.ERROR_MESSAGE
                );
                statusLabel.setText("Analysis failed");
            } finally {
                setCursor(Cursor.getDefaultCursor());
            }
        }
    }

    /**
     * Handles time series analysis with UK date presentation.
     */
    private void handleTimeSeriesAnalysis(ActionEvent e) {
        try {
            List<String> airports = dataService.getAirports();
            String[] airportOptions = airports.toArray(new String[0]);

            String selectedAirport = (String) JOptionPane.showInputDialog(
                    this,
                    "Select airport:",
                    "Airport Delay Time Series",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    airportOptions,
                    airportOptions[0]);

            if (selectedAirport != null && !selectedAirport.trim().isEmpty()) {
                // Extract airport code
                String airportCode = selectedAirport.substring(0, selectedAirport.indexOf(" - "));
                String airportName = selectedAirport.substring(selectedAirport.indexOf(" - ") + 3);

                statusLabel.setText("Analyzing delays for " + airportName + " over time...");
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

                // Get data
                Map<String, Double> data = dataService.getDelaysByMonth(airportCode, 2019, 2023);

                // Show chart
                analysisPanel.showTimeSeriesChart(data, airportName);

                // Switch to analysis tab automatically
                ((JTabbedPane)analysisPanel.getParent()).setSelectedComponent(analysisPanel);

                statusLabel.setText("Time series analysis completed for " + airportName);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Database error: " + ex.getMessage(),
                    "Analysis Error",
                    JOptionPane.ERROR_MESSAGE
            );
            statusLabel.setText("Analysis failed");
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    /**
     * Handles export functionality.
     */
    private void handleExport(ActionEvent e) {
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this,
                    "No data to export. Please perform a search first.",
                    "Export", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Flight Data");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            // This would actually save the file in a real implementation
            JOptionPane.showMessageDialog(this,
                    "Data would be exported to: " + fileChooser.getSelectedFile().getPath(),
                    "Export", JOptionPane.INFORMATION_MESSAGE);

            statusLabel.setText("Data exported to " + fileChooser.getSelectedFile().getName());
        }
    }

    /**
     * Shows help information.
     */
    private void showHelp() {
        JOptionPane.showMessageDialog(this,
                "UK Flight Punctuality Tracker Help\n\n" +
                        "Search: Enter airline, flight number, origin or destination to find flights\n" +
                        "Analysis: Use the Analysis menu to view delay statistics\n" +
                        "Dates: All dates should be entered in UK format (DD/MM/YYYY)\n\n" +
                        "For more help, please refer to the user manual.",
                "Help", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Shows about information.
     */
    private void showAbout() {
        JOptionPane.showMessageDialog(this,
                "UK Flight Punctuality Tracker\n" +
                        "Version 1.0\n\n" +
                        "A tool for analyzing flight delays and cancellations.\n" +
                        "All dates are in UK format (DD/MM/YYYY).",
                "About", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Custom renderer for alternating row colors.
     */
    private class StripedRowRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {

            Component comp = super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);

            if (!isSelected) {
                if (row % 2 == 0) {
                    comp.setBackground(Color.WHITE);
                } else {
                    comp.setBackground(new Color(240, 240, 250)); // Light blue-gray
                }
            }

            return comp;
        }
    }

    /**
     * Main method to run the application.
     */
    public static void main(String[] args) {
        // Set look and feel to system default
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Error setting look and feel: " + e.getMessage());
        }

        // Launch application on the event dispatch thread
        SwingUtilities.invokeLater(() -> {
            try {
                SimplifiedFlightApp app = new SimplifiedFlightApp();
                app.setVisible(true);
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(
                        null,
                        "Database error: " + e.getMessage() + "\n\n" +
                                "Make sure the flights.db database exists and is accessible.",
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE
                );
                System.exit(1);
            }
        });
    }
}