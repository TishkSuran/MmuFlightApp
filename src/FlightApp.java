import flightModel.Flight;
import service.FlightDataService;
import ui.AnalysisPanel;
import ui.FlightDetailPanel;
import ui.FlightTableModel;
import ui.SearchPanel;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class FlightApp extends JFrame {

    // Services & models/
    private FlightDataService dataService;
    private FlightTableModel tableModel;

    // UI components.
    private JTable flightTable;
    private FlightDetailPanel detailPanel;
    private SearchPanel searchPanel;
    private AnalysisPanel analysisPanel;
    private JLabel statusLabel;

    // UI constants
    private final Color PRIMARY_COLOR = new Color(41, 128, 185); // Blue
    private final Font HEADER_FONT = new Font("Arial", Font.BOLD, 14);

    // Quick access to shared borders
    private Border standardBorder;


    public FlightApp() throws SQLException {
        super("Flight Punctuality Tracker");

        try {
            // setIconImage(new ImageIcon(getClass().getResource("/images/icon_image.png")).getImage());
        } catch (Exception e) {
            System.err.println("Icon missing: " + e.getMessage());
        }

        // Init DB connection.
        dataService = new FlightDataService();

        // Set up UI components.
        tableModel = new FlightTableModel();
        flightTable = makeTable(tableModel);
        detailPanel = new FlightDetailPanel();

        // Create standardBorder
        standardBorder = BorderFactory.createCompoundBorder(
                BorderFactory.createEtchedBorder(),
                new EmptyBorder(5, 5, 5, 5)
        );

        searchPanel = new SearchPanel(
                dataService.getAirlines(),
                dataService.getAirports(),
                this::handleSearch,
                this::handleClear
        );

        analysisPanel = new AnalysisPanel();
        statusLabel = new JLabel("Ready");
        statusLabel.setBorder(new EmptyBorder(3, 10, 3, 10));

        buildUI();

        // DB cleanup on exit.
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    dataService.disconnect();
                    System.out.println("DB connection closed");
                } catch (SQLException ex) {
                    // Just log and exit anyway.
                    System.err.println("Error on disconnect: " + ex.getMessage());
                }
            }
        });

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
    }

    private JTable makeTable(FlightTableModel model) {
        JTable table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        table.setRowHeight(25);

        // Style header.
        table.getTableHeader().setFont(HEADER_FONT);
        table.getTableHeader().setBackground(PRIMARY_COLOR);
        table.getTableHeader().setForeground(Color.WHITE);

        // Stripe rows for readability.
        table.setDefaultRenderer(Object.class, new StripedRowRenderer());

        return table;
    }

    private void buildUI() {
        // Hook up flight selection -> detail panel.
        flightTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = flightTable.getSelectedRow();
                if (selectedRow >= 0) {
                    int modelRow = flightTable.convertRowIndexToModel(selectedRow);
                    Flight f = tableModel.getFlightAt(modelRow);
                    detailPanel.setFlight(f);
                } else {
                    detailPanel.clearDetails();
                }
            }
        });

        // Set up sorter
        TableRowSorter<FlightTableModel> sorter = new TableRowSorter<>(tableModel);
        flightTable.setRowSorter(sorter);

        // Create scrolling table with border.
        JScrollPane tableScroller = new JScrollPane(flightTable);
        tableScroller.setBorder(makeTitledBorder("Flight Results"));

        // Tabs for right side.
        JTabbedPane rightTabs = new JTabbedPane();
        rightTabs.setFont(HEADER_FONT);

        // Add detail panel to tabs.
        JPanel detailsContainer = new JPanel(new BorderLayout());
        detailsContainer.add(detailPanel, BorderLayout.CENTER);
        rightTabs.addTab("Flight Details", detailsContainer);

        // Analysis tab.
        rightTabs.addTab("Analysis", analysisPanel);

        // Main split pane dividing table and details.
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(750);
        splitPane.setResizeWeight(0.6);

        // Left side: search + results table.
        JPanel leftPanel = new JPanel(new BorderLayout(0, 10));
        leftPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        leftPanel.add(searchPanel, BorderLayout.NORTH);
        leftPanel.add(tableScroller, BorderLayout.CENTER);

        // Add both sides to splitter.
        splitPane.setLeftComponent(leftPanel);
        splitPane.setRightComponent(rightTabs);

        // Create toolbar with buttons.
        JToolBar toolBar = makeToolBar();

        // Status bar at bottom.
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY),
                new EmptyBorder(3, 10, 3, 10)
        ));
        statusBar.add(statusLabel, BorderLayout.WEST);

        // Help button.
        JButton helpBtn = new JButton("Help");
        helpBtn.addActionListener(e -> showHelp());
        statusBar.add(helpBtn, BorderLayout.EAST);

        // Add everything to frame.
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(toolBar, BorderLayout.NORTH);
        getContentPane().add(splitPane, BorderLayout.CENTER);
        getContentPane().add(statusBar, BorderLayout.SOUTH);

        setJMenuBar(makeMenuBar());
    }

    private JToolBar makeToolBar() {
        JToolBar bar = new JToolBar();
        bar.setFloatable(false);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
                new EmptyBorder(3, 10, 3, 10)
        ));

        // Search button
        JButton searchBtn = new JButton("Search");
        searchBtn.setFont(HEADER_FONT);
        searchBtn.addActionListener(this::handleSearch);

        // Clear button
        JButton clearBtn = new JButton("Clear");
        clearBtn.setFont(HEADER_FONT);
        clearBtn.addActionListener(this::handleClear);

        // Analysis buttons
        JButton airlineBtn = new JButton("Airline Analysis");
        airlineBtn.setFont(HEADER_FONT);
        airlineBtn.addActionListener(this::handleAirlineAnalysis);

        JButton airportBtn = new JButton("Airport Analysis");
        airportBtn.setFont(HEADER_FONT);
        airportBtn.addActionListener(this::handleAirportAnalysis);

        // Export - FIXME: Still needs file format selection
        JButton exportBtn = new JButton("Export Results");
        exportBtn.setFont(HEADER_FONT);
        exportBtn.addActionListener(this::handleExport);

        // Add buttons to toolbar
        bar.add(searchBtn);
        bar.add(clearBtn);
        bar.addSeparator(new Dimension(20, 0));
        bar.add(airlineBtn);
        bar.add(airportBtn);
        bar.addSeparator(new Dimension(20, 0));
        bar.add(exportBtn);

        return bar;
    }

    private JMenuBar makeMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // File menu.
        JMenu fileMenu = new JMenu("File");
        fileMenu.add(new JMenuItem("Export Results")).addActionListener(this::handleExport);
        fileMenu.add(new JMenuItem("Print")).addActionListener(e ->
                JOptionPane.showMessageDialog(this, "Print not implemented yet.")
        );
        fileMenu.addSeparator();
        fileMenu.add(new JMenuItem("Exit")).addActionListener(e -> System.exit(0));

        // Analysis menu.
        JMenu analysisMenu = new JMenu("Analysis");
        analysisMenu.add(new JMenuItem("Airline Delays by Year"))
                .addActionListener(this::handleAirlineAnalysis);
        analysisMenu.add(new JMenuItem("Airport Delays by Year"))
                .addActionListener(this::handleAirportAnalysis);
        analysisMenu.add(new JMenuItem("Airport Delays Over Time"))
                .addActionListener(this::handleTimeSeriesAnalysis);

        // Help menu.
        JMenu helpMenu = new JMenu("Help");
        helpMenu.add(new JMenuItem("User Guide")).addActionListener(e -> showHelp());
        helpMenu.add(new JMenuItem("About")).addActionListener(e -> showAbout());

        menuBar.add(fileMenu);
        menuBar.add(analysisMenu);
        menuBar.add(helpMenu);

        return menuBar;
    }

    private Border makeTitledBorder(String title) {
        TitledBorder titledBorder = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(PRIMARY_COLOR, 1),
                title
        );
        titledBorder.setTitleFont(HEADER_FONT);
        titledBorder.setTitleColor(PRIMARY_COLOR);

        return new CompoundBorder(
                titledBorder,
                new EmptyBorder(5, 5, 5, 5)
        );
    }


    private void handleSearch(ActionEvent e) {
        try {
            statusLabel.setText("Searching...");
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            // Grab search params.
            String airline = searchPanel.getAirline();
            String flightNum = searchPanel.getFlightNumber();
            String origin = searchPanel.getOrigin();
            String dest = searchPanel.getDestination();
            LocalDate startDate = searchPanel.getStartDate();
            LocalDate endDate = searchPanel.getEndDate();
            Integer minDelay = searchPanel.getMinDelay();
            Integer maxDelay = searchPanel.getMaxDelay();
            String delayReason = searchPanel.getDelayReason();

            // Show what we're searching for.
            StringBuilder searchDesc = new StringBuilder("Searching for flights");
            if (airline != null && !airline.isEmpty())
                searchDesc.append(" with ").append(airline);
            if (origin != null && !origin.isEmpty())
                searchDesc.append(" from ").append(origin);
            if (dest != null && !dest.isEmpty())
                searchDesc.append(" to ").append(dest);

            statusLabel.setText(searchDesc.toString());

            // Run the search.
            List<Flight> results = dataService.searchFlights(
                    airline, flightNum, origin, dest,
                    startDate, endDate, minDelay, maxDelay, delayReason
            );

            // Update UI.
            tableModel.setFlights(results);
            flightTable.clearSelection();

            // Show result stats.
            int delayed = countDelays(results);
            int cancelled = countCancellations(results);

            String resultText = "Found " + results.size() + " flights";
            if (results.size() > 0) {
                resultText += " (" + delayed + " delayed, " + cancelled + " cancelled)";
            }
            statusLabel.setText(resultText);

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "DB error: " + ex.getMessage(),
                    "Search Error", JOptionPane.ERROR_MESSAGE);
            statusLabel.setText("Search failed");
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    private int countDelays(List<Flight> flights) {
        int count = 0;
        for (Flight f : flights) {
            if (!f.isCancelled() && !f.isDiverted() && f.getDelayMinutes() > 0) {
                count++;
            }
        }
        return count;
    }

    private int countCancellations(List<Flight> flights) {
        int count = 0;
        for (Flight f : flights) {
            if (f.isCancelled()) {
                count++;
            }
        }
        return count;
    }


    private void handleClear(ActionEvent e) {
        searchPanel.clearFields();
        tableModel.setFlights(new java.util.ArrayList<>());
        detailPanel.clearDetails();
        statusLabel.setText("Search cleared");
    }

    private void handleAirlineAnalysis(ActionEvent e) {
        // Years we have data for
        Object[] years = {"2019", "2020", "2021", "2022", "2023"};

        Object selectedYear = JOptionPane.showInputDialog(
                this,
                "Select year for analysis:",
                "Airline Delay Analysis",
                JOptionPane.QUESTION_MESSAGE,
                null,
                years,
                "2023" // default to latest year
        );

        if (selectedYear != null) {
            try {
                int year = Integer.parseInt(selectedYear.toString());

                statusLabel.setText("Getting airline delays for " + year + "...");
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

                // Get data.
                Map<String, Double> data = dataService.getAverageDelayByAirline(year);

                // Show chart.
                analysisPanel.showAirlineDelayChart(data, year);

                // Switch to chart tab.
                ((JTabbedPane)analysisPanel.getParent()).setSelectedComponent(analysisPanel);

                statusLabel.setText("Airline analysis done for " + year);

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(
                        this,
                        "DB error: " + ex.getMessage(),
                        "Analysis Error",
                        JOptionPane.ERROR_MESSAGE
                );
                statusLabel.setText("Analysis failed");
            } finally {
                setCursor(Cursor.getDefaultCursor());
            }
        }
    }

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

                statusLabel.setText("Getting airport delays for " + year + "...");
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

                Map<String, Double> data = dataService.getAverageDelayByAirport(year);
                analysisPanel.showAirportDelayChart(data, year);

                // Switch to chart tab
                ((JTabbedPane)analysisPanel.getParent()).setSelectedComponent(analysisPanel);

                statusLabel.setText("Airport analysis done for " + year);

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(
                        this,
                        "DB error: " + ex.getMessage(),
                        "Analysis Error",
                        JOptionPane.ERROR_MESSAGE
                );
                statusLabel.setText("Analysis failed");
            } finally {
                setCursor(Cursor.getDefaultCursor());
            }
        }
    }


    private void handleTimeSeriesAnalysis(ActionEvent e) {
        try {
            List<String> airports = dataService.getAirports();
            String[] airportOptions = airports.toArray(new String[0]);

            String selectedAirport = (String) JOptionPane.showInputDialog(
                    this,
                    "Select airport:",
                    "Airport Delays Over Time",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    airportOptions,
                    airportOptions[0]);

            if (selectedAirport != null && !selectedAirport.trim().isEmpty()) {
                String airportCode = selectedAirport.substring(0, selectedAirport.indexOf(" - "));
                String airportName = selectedAirport.substring(selectedAirport.indexOf(" - ") + 3);

                statusLabel.setText("Analysing delays for " + airportName + "...");
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

                // Get data
                Map<String, Double> data = dataService.getDelaysByMonth(airportCode, 2019, 2023);

                // Show chart and switch tabs
                analysisPanel.showTimeSeriesChart(data, airportName);
                ((JTabbedPane)analysisPanel.getParent()).setSelectedComponent(analysisPanel);

                statusLabel.setText("Analysis complete for " + airportName);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "DB error: " + ex.getMessage(),
                    "Analysis Error",
                    JOptionPane.ERROR_MESSAGE
            );
            statusLabel.setText("Analysis failed");
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    /**
     * Export results to file
     * TODO: Add CSV/Excel options
     */
    private void handleExport(ActionEvent e) {
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this,
                    "No data to export. Do a search first.",
                    "Export", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Export Flight Data");
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);

        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            // This would actually save in a real implementation.
            JOptionPane.showMessageDialog(this,
                    "Data would have been saved to: " + fc.getSelectedFile().getPath() + " but I was too lazy to implement this feature.",
                    "Export", JOptionPane.INFORMATION_MESSAGE);

            statusLabel.setText("Exported to " + fc.getSelectedFile().getName());
        }
    }

    private void showHelp() {
        JOptionPane.showMessageDialog(this,
                "Flight Punctuality Tracker Help\n\n" +
                        "Common Issue: Please ensure you have updated the dates correctly if you are not receiving any values.\n" +
                        "Search: Enter airline, flight#, origin or destination\n" +
                        "Analysis: Check delay stats via Analysis menu",
                "Help", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showAbout() {
        JOptionPane.showMessageDialog(this,
                "Flight Punctuality Tracker\n" +
                        "A tool for analysing flight delays and cancellations.",
                "About", JOptionPane.INFORMATION_MESSAGE);
    }

    private class StripedRowRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {

            Component c = super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);

            if (!isSelected) {
                c.setBackground(row % 2 == 0 ?
                        Color.WHITE : new Color(240, 240, 250)); // Light blue-gray
            }

            return c;
        }
    }


    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Look and feel error: " + e.getMessage());
        }

        // Launch app.
        SwingUtilities.invokeLater(() -> {
            try {
                new FlightApp().setVisible(true);
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(
                        null,
                        "Database error: " + e.getMessage() + "\n\n" +
                                "Make sure flights.db exists and is accessible.",
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE
                );
                System.exit(1);
            }
        });
    }
}