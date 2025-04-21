package ui;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.Map;

/**
 * Enhanced panel for displaying analysis charts and reports with improved UX.
 */
public class AnalysisPanel extends JPanel {

    private final JTabbedPane tabbedPane;
    private final JPanel airlinePanel;
    private final JPanel airportPanel;
    private final JPanel timeSeriesPanel;

    // Interactive elements to improve UX
    private final JPanel instructionPanel;
    private ActionListener airlineAnalysisAction;
    private ActionListener airportAnalysisAction;
    private ActionListener timeSeriesAnalysisAction;

    /**
     * Creates a new improved analysis panel.
     */
    public AnalysisPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Analysis and Reports"));

        // Create main instruction panel that will show initially
        instructionPanel = createInstructionPanel();

        tabbedPane = new JTabbedPane();

        // Create panels for each analysis type
        airlinePanel = new JPanel(new BorderLayout());
        airportPanel = new JPanel(new BorderLayout());
        timeSeriesPanel = new JPanel(new BorderLayout());

        // Add tabs
        tabbedPane.addTab("By Airline", airlinePanel);
        tabbedPane.addTab("By Airport", airportPanel);
        tabbedPane.addTab("Over Time", timeSeriesPanel);

        // Create empty charts for initial state
        initializeWithEmptyCharts();

        // Add components
        add(tabbedPane, BorderLayout.CENTER);
    }

    /**
     * Creates an instructional panel to guide users.
     * @return the instruction panel
     */
    private JPanel createInstructionPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Add heading
        JLabel headingLabel = new JLabel("Flight Analysis Guide");
        headingLabel.setFont(new Font("Arial", Font.BOLD, 18));
        headingLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(headingLabel);

        panel.add(Box.createVerticalStrut(15));

        // Add instructions
        JLabel instructionsLabel = new JLabel("<html><div style='text-align: center;'>"+
                "Use the Analysis menu to generate charts:<br><br>" +
                "1. Click Analysis → Airline Delays by Year<br>" +
                "2. Click Analysis → Airport Delays by Year<br>" +
                "3. Click Analysis → Airport Delays Over Time<br><br>" +
                "Or use the buttons below to get started:" +
                "</div></html>");
        instructionsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(instructionsLabel);

        panel.add(Box.createVerticalStrut(20));

        // Add quick access buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        JButton airlineButton = new JButton("Analyze Airlines");
        airlineButton.setToolTipText("Compare average delay times across different airlines");

        JButton airportButton = new JButton("Analyze Airports");
        airportButton.setToolTipText("See which airports have the most delays");

        JButton timeSeriesButton = new JButton("Analyze Trends");
        timeSeriesButton.setToolTipText("Track delay patterns over time for a specific airport");

        // The action listeners will be set by the main app
        buttonPanel.add(airlineButton);
        buttonPanel.add(airportButton);
        buttonPanel.add(timeSeriesButton);

        panel.add(buttonPanel);

        return panel;
    }

    /**
     * Initializes all tabs with empty charts and instructions.
     */
    public void initializeWithEmptyCharts() {
        // Create empty charts for each panel
        initializeEmptyPanel(airlinePanel, "Airline Delay Analysis",
                "Compare average delay times across different airlines.",
                "Select 'Analysis → Airline Delays by Year' from the menu");

        initializeEmptyPanel(airportPanel, "Airport Delay Analysis",
                "See which airports have the most delays.",
                "Select 'Analysis → Airport Delays by Year' from the menu");

        initializeEmptyPanel(timeSeriesPanel, "Delay Trends Analysis",
                "Track delay patterns over time for a specific airport.",
                "Select 'Analysis → Airport Delays Over Time' from the menu");

        // Make sure the first tab is selected
        tabbedPane.setSelectedIndex(0);

        // Force refresh
        revalidate();
        repaint();
    }

    /**
     * Initializes a single panel with an empty chart and instructions.
     */
    private void initializeEmptyPanel(JPanel panel, String title, String description, String instructions) {
        panel.removeAll();

        // Create a panel for instructions
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel descLabel = new JLabel("<html><div style='text-align: center;'>" + description + "</div></html>");
        descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel instructLabel = new JLabel("<html><div style='text-align: center;'><b>How to use:</b><br>" +
                instructions + "</div></html>");
        instructLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Create a simple empty dataset
        DefaultCategoryDataset emptyDataset = new DefaultCategoryDataset();
        emptyDataset.addValue(0.0, "No Data", "Run analysis to see data");

        // Create empty chart
        JFreeChart emptyChart = ChartFactory.createBarChart(
                "No Data Available - Run Analysis First",  // Title
                "",                // X-Axis Label
                "",                // Y-Axis Label
                emptyDataset,      // Dataset
                PlotOrientation.HORIZONTAL, // Orientation
                false,             // Show Legend
                true,              // Use tooltips
                false              // Generate URLs
        );

        // Create chart panel
        ChartPanel chartPanel = new ChartPanel(emptyChart);
        chartPanel.setPreferredSize(new Dimension(600, 300));

        // Add components to info panel
        infoPanel.add(titleLabel);
        infoPanel.add(Box.createVerticalStrut(10));
        infoPanel.add(descLabel);
        infoPanel.add(Box.createVerticalStrut(15));
        infoPanel.add(instructLabel);

        // Add components to main panel
        panel.add(infoPanel, BorderLayout.NORTH);
        panel.add(chartPanel, BorderLayout.CENTER);
    }

    /**
     * Sets the action listeners for the quick access buttons.
     */
    public void setAnalysisActions(ActionListener airlineAction, ActionListener airportAction, ActionListener timeSeriesAction) {
        this.airlineAnalysisAction = airlineAction;
        this.airportAnalysisAction = airportAction;
        this.timeSeriesAnalysisAction = timeSeriesAction;

        // Apply actions to buttons
        if (instructionPanel != null) {
            Component[] components = ((JPanel)instructionPanel.getComponent(4)).getComponents();
            if (components.length >= 3) {
                ((JButton)components[0]).addActionListener(airlineAction);
                ((JButton)components[1]).addActionListener(airportAction);
                ((JButton)components[2]).addActionListener(timeSeriesAction);
            }
        }
    }

    /**
     * Displays a chart of average delay by airline.
     * @param data map of airline names to average delay
     * @param year the year of the analysis
     */
    public void showAirlineDelayChart(Map<String, Double> data, int year) {
        // Create dataset
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        // Add data to dataset, sorted by delay in descending order
        data.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .forEach(entry -> dataset.addValue(entry.getValue(), "Delay", entry.getKey()));

        // Create chart
        JFreeChart chart = ChartFactory.createBarChart(
                "Average Delay by Airline in " + year,  // Title
                "Airline",               // X-Axis Label
                "Average Delay (minutes)", // Y-Axis Label
                dataset,                 // Dataset
                PlotOrientation.HORIZONTAL, // Orientation
                false,                   // Show Legend
                true,                    // Use tooltips
                false                    // Generate URLs
        );

        // Customize chart
        CategoryPlot plot = chart.getCategoryPlot();
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setBarPainter(new StandardBarPainter());
        renderer.setSeriesPaint(0, new Color(79, 129, 189));

        // Remove space between bars
        renderer.setItemMargin(0.0);

        // Create chart panel
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(600, 400));

        // Add to panel
        airlinePanel.removeAll();

        // Add a header panel with information
        JPanel headerPanel = createHeaderPanel(
                "Airline Delay Analysis for " + year,
                "This chart shows the average delay in minutes for each airline in " + year + ".",
                "Longer bars indicate longer average delays."
        );

        // Add components
        airlinePanel.add(headerPanel, BorderLayout.NORTH);
        airlinePanel.add(chartPanel, BorderLayout.CENTER);
        airlinePanel.revalidate();
        airlinePanel.repaint();

        // Select tab
        tabbedPane.setSelectedComponent(airlinePanel);
    }

    /**
     * Displays a chart of average delay by airport.
     * @param data map of airport names to average delay
     * @param year the year of the analysis
     */
    public void showAirportDelayChart(Map<String, Double> data, int year) {
        // Create dataset
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        // Add data to dataset, sorted by delay in descending order
        data.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .forEach(entry -> dataset.addValue(entry.getValue(), "Delay", entry.getKey()));

        // Create chart
        JFreeChart chart = ChartFactory.createBarChart(
                "Average Delay by Departure Airport in " + year,  // Title
                "Airport",               // X-Axis Label
                "Average Delay (minutes)", // Y-Axis Label
                dataset,                 // Dataset
                PlotOrientation.HORIZONTAL, // Orientation
                false,                   // Show Legend
                true,                    // Use tooltips
                false                    // Generate URLs
        );

        // Customize chart
        CategoryPlot plot = chart.getCategoryPlot();
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setBarPainter(new StandardBarPainter());
        renderer.setSeriesPaint(0, new Color(192, 80, 77));

        // Rotate category labels for better readability
        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setCategoryMargin(0.25);

        // Create chart panel
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(600, 400));

        // Add to panel
        airportPanel.removeAll();

        // Add a header panel with information
        JPanel headerPanel = createHeaderPanel(
                "Airport Delay Analysis for " + year,
                "This chart shows the average delay in minutes for departures from each airport in " + year + ".",
                "Longer bars indicate longer average delays."
        );

        // Add components
        airportPanel.add(headerPanel, BorderLayout.NORTH);
        airportPanel.add(chartPanel, BorderLayout.CENTER);
        airportPanel.revalidate();
        airportPanel.repaint();

        // Select tab
        tabbedPane.setSelectedComponent(airportPanel);
    }

    /**
     * Displays a chart of average delay over time for a specific airport.
     * @param data map of month-year to average delay
     * @param airportName the name of the airport
     */
    public void showTimeSeriesChart(Map<String, Double> data, String airportName) {
        // Create dataset
        XYSeriesCollection dataset = new XYSeriesCollection();
        XYSeries series = new XYSeries("Average Delay");

        // Add data to series in chronological order
        data.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    // Parse month/year to get a numeric x value
                    String[] parts = entry.getKey().split("/");
                    if (parts.length == 2) {
                        try {
                            int month = Integer.parseInt(parts[0]);
                            int year = Integer.parseInt(parts[1]);
                            // Convert to a decimal year value (e.g., 2020.5 for June 2020)
                            double xValue = year + (month - 1) / 12.0;
                            series.add(xValue, entry.getValue());
                        } catch (NumberFormatException e) {
                            System.err.println("Invalid month/year: " + entry.getKey());
                        }
                    }
                });

        dataset.addSeries(series);

        // Create chart
        JFreeChart chart = ChartFactory.createXYLineChart(
                "Average Delay for Flights Departing " + airportName,  // Title
                "Date",                  // X-Axis Label
                "Average Delay (minutes)", // Y-Axis Label
                dataset,                 // Dataset
                PlotOrientation.VERTICAL, // Orientation
                false,                   // Show Legend
                true,                    // Use tooltips
                false                    // Generate URLs
        );

        // Customize chart
        chart.getPlot().setBackgroundPaint(Color.WHITE);

        // Create chart panel
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(600, 400));

        // Add to panel
        timeSeriesPanel.removeAll();

        // Add a header panel with information
        JPanel headerPanel = createHeaderPanel(
                "Delay Trends Analysis for " + airportName,
                "This chart shows how average delays have changed over time for flights departing from " + airportName + ".",
                "Higher points indicate periods with longer delays."
        );

        // Add components
        timeSeriesPanel.add(headerPanel, BorderLayout.NORTH);
        timeSeriesPanel.add(chartPanel, BorderLayout.CENTER);
        timeSeriesPanel.revalidate();
        timeSeriesPanel.repaint();

        // Select tab
        tabbedPane.setSelectedComponent(timeSeriesPanel);
    }

    /**
     * Creates a header panel with explanation text for charts.
     */
    private JPanel createHeaderPanel(String title, String description, String interpretation) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel descLabel = new JLabel(description);
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel interpLabel = new JLabel(interpretation);
        interpLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        interpLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(2));
        panel.add(descLabel);
        panel.add(Box.createVerticalStrut(2));
        panel.add(interpLabel);

        return panel;
    }
}