package ui;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.time.Month;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

/**
 * Enhanced panel for displaying analysis charts with UK-specific formatting.
 */
public class AnalysisPanel extends JPanel {

    // UI constants
    private final Color primaryColor = new Color(41, 128, 185);
    private final Color secondaryColor = new Color(231, 76, 60);
    private final Color backgroundColor = new Color(245, 245, 250);
    private final Font headerFont = new Font("Arial", Font.BOLD, 14);
    private final Font labelFont = new Font("Arial", Font.PLAIN, 12);

    // Components
    private final JPanel chartPanel;
    private final JPanel summaryPanel;
    private final JLabel chartTitleLabel;
    private final JTextArea summaryTextArea;

    // Chart state tracking
    private String currentChartType = "none";

    /**
     * Creates a new simplified analysis panel with UK formatting.
     */
    public AnalysisPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(backgroundColor);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // Create chart panel with placeholder
        chartPanel = new JPanel(new BorderLayout());
        chartPanel.setBackground(backgroundColor);
        chartPanel.setBorder(createTitledBorder("Analysis Chart"));

        // Create header for chart title
        chartTitleLabel = new JLabel("No Analysis Selected", JLabel.CENTER);
        chartTitleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        chartTitleLabel.setForeground(primaryColor);
        chartTitleLabel.setBorder(new EmptyBorder(5, 0, 10, 0));

        // Create summary panel
        summaryPanel = new JPanel(new BorderLayout());
        summaryPanel.setBackground(backgroundColor);
        summaryPanel.setBorder(createTitledBorder("Analysis Summary"));

        summaryTextArea = new JTextArea();
        summaryTextArea.setEditable(false);
        summaryTextArea.setLineWrap(true);
        summaryTextArea.setWrapStyleWord(true);
        summaryTextArea.setFont(new Font("Arial", Font.PLAIN, 13));
        summaryTextArea.setBackground(backgroundColor);
        summaryTextArea.setBorder(new EmptyBorder(5, 5, 5, 5));

        JScrollPane summaryScrollPane = new JScrollPane(summaryTextArea);
        summaryScrollPane.setBorder(BorderFactory.createEmptyBorder());
        summaryPanel.add(summaryScrollPane, BorderLayout.CENTER);

        // Add instruction label to chart panel
        showWelcomeMessage();

        // Layout components
        setLayout(new BorderLayout(10, 10));

        // Create split pane for chart and summary
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setTopComponent(chartPanel);
        splitPane.setBottomComponent(summaryPanel);
        splitPane.setResizeWeight(0.7); // Give more weight to the chart
        splitPane.setDividerLocation(400);

        add(splitPane, BorderLayout.CENTER);
    }

    /**
     * Creates a styled titled border for panels.
     */
    private javax.swing.border.Border createTitledBorder(String title) {
        return new CompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(primaryColor, 1),
                        title,
                        javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                        javax.swing.border.TitledBorder.DEFAULT_POSITION,
                        headerFont,
                        primaryColor
                ),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        );
    }

    /**
     * Displays welcome message and instructions.
     */
    private void showWelcomeMessage() {
        // Clear chart panel
        chartPanel.removeAll();

        // Update title
        chartTitleLabel.setText("Flight Analysis");
        chartPanel.add(chartTitleLabel, BorderLayout.NORTH);

        // Create instruction panel
        JPanel instructionPanel = new JPanel();
        instructionPanel.setLayout(new BoxLayout(instructionPanel, BoxLayout.Y_AXIS));
        instructionPanel.setBackground(backgroundColor);
        instructionPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Add instructions
        JLabel welcomeLabel = new JLabel("Welcome to Flight Analysis");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 18));
        welcomeLabel.setForeground(primaryColor);
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextArea instructionText = new JTextArea(
                "Use the Analysis menu or toolbar buttons to generate reports:\n\n" +
                        "• Airline Delays by Year - Compare which airlines have the most delays\n" +
                        "• Airport Delays by Year - See which airports experience the most delays\n" +
                        "• Airport Delays Over Time - Track how delays change throughout the year\n\n" +
                        "All date information is displayed in UK format (DD/MM/YYYY)."
        );
        instructionText.setEditable(false);
        instructionText.setLineWrap(true);
        instructionText.setWrapStyleWord(true);
        instructionText.setFont(new Font("Arial", Font.PLAIN, 14));
        instructionText.setBackground(backgroundColor);
        instructionText.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Add to instruction panel
        instructionPanel.add(welcomeLabel);
        instructionPanel.add(Box.createVerticalStrut(20));
        instructionPanel.add(instructionText);

        // Add to chart panel
        chartPanel.add(instructionPanel, BorderLayout.CENTER);

        // Update summary text
        summaryTextArea.setText("Select an analysis type from the Analysis menu to get started.\n\n" +
                "The analysis will be displayed in the chart area above, and a summary of findings " +
                "will appear here. You can resize this panel by dragging the divider bar.");

        // Refresh UI
        revalidate();
        repaint();
    }

    /**
     * Displays a chart of average delay by airline with UK formatting.
     */
    public void showAirlineDelayChart(Map<String, Double> data, int year) {
        currentChartType = "airline";

        // Create dataset
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        // Sort data by delay in descending order
        TreeMap<Double, String> sortedData = new TreeMap<>((a, b) -> b.compareTo(a));
        for (Map.Entry<String, Double> entry : data.entrySet()) {
            sortedData.put(entry.getValue(), entry.getKey());
        }

        // Add top airlines to dataset (limit to top 10 for readability)
        int count = 0;
        for (Map.Entry<Double, String> entry : sortedData.entrySet()) {
            dataset.addValue(entry.getKey(), "Delay", entry.getValue());
            count++;
            if (count >= 10) break; // Limit to top 10
        }

        // Create chart
        JFreeChart chart = ChartFactory.createBarChart(
                null,  // Title set separately
                "Airline",               // X-Axis Label
                "Average Delay (minutes)", // Y-Axis Label
                dataset,                 // Dataset
                PlotOrientation.HORIZONTAL, // Orientation
                false,                   // Show Legend
                true,                    // Use tooltips
                false                    // Generate URLs
        );

        // Customize chart appearance
        chart.setBackgroundPaint(backgroundColor);

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlinePaint(null);
        plot.setRangeGridlinePaint(new Color(230, 230, 230));

        // Customize renderer
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setBarPainter(new StandardBarPainter());
        renderer.setSeriesPaint(0, primaryColor);
        renderer.setShadowVisible(false);

        // Improve axis appearance
        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setTickLabelFont(labelFont);
        domainAxis.setLabelFont(headerFont);

        plot.getRangeAxis().setTickLabelFont(labelFont);
        plot.getRangeAxis().setLabelFont(headerFont);

        // Create chart panel
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(600, 400));
        chartPanel.setBackground(backgroundColor);

        // Update the panel
        this.chartPanel.removeAll();

        // Add title
        chartTitleLabel.setText("Average Delay by Airline in " + year);
        this.chartPanel.add(chartTitleLabel, BorderLayout.NORTH);

        // Add chart
        this.chartPanel.add(chartPanel, BorderLayout.CENTER);

        // Generate summary text
        StringBuilder summary = new StringBuilder();
        summary.append("Average Delay by Airline in ").append(year).append("\n\n");

        if (data.isEmpty()) {
            summary.append("No delay data available for the selected year.");
        } else {
            // Calculate overall average
            double totalDelay = 0;
            for (Double delay : data.values()) {
                totalDelay += delay;
            }
            double avgDelay = totalDelay / data.size();

            summary.append("Analysis shows the average delay times across ")
                    .append(data.size()).append(" airlines in ").append(year).append(".\n\n");

            // Top 3 airlines with most delays
            count = 0;
            summary.append("Airlines with longest average delays:\n");
            for (Map.Entry<Double, String> entry : sortedData.entrySet()) {
                summary.append("• ").append(entry.getValue())
                        .append(": ").append(String.format("%.1f", entry.getKey()))
                        .append(" minutes\n");
                count++;
                if (count >= 3) break;
            }

            summary.append("\nOverall average delay across all airlines: ")
                    .append(String.format("%.1f", avgDelay)).append(" minutes.");
        }

        // Update summary text
        summaryTextArea.setText(summary.toString());

        // Refresh UI
        revalidate();
        repaint();
    }

    /**
     * Displays a chart of average delay by airport with UK formatting.
     */
    public void showAirportDelayChart(Map<String, Double> data, int year) {
        currentChartType = "airport";

        // Create dataset
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        // Sort data by delay in descending order
        TreeMap<Double, String> sortedData = new TreeMap<>((a, b) -> b.compareTo(a));
        for (Map.Entry<String, Double> entry : data.entrySet()) {
            sortedData.put(entry.getValue(), entry.getKey());
        }

        // Add top airports to dataset (limit to top 10 for readability)
        int count = 0;
        for (Map.Entry<Double, String> entry : sortedData.entrySet()) {
            dataset.addValue(entry.getKey(), "Delay", entry.getValue());
            count++;
            if (count >= 10) break; // Limit to top 10
        }

        // Create chart
        JFreeChart chart = ChartFactory.createBarChart(
                null,  // Title set separately
                "Airport",               // X-Axis Label
                "Average Delay (minutes)", // Y-Axis Label
                dataset,                 // Dataset
                PlotOrientation.HORIZONTAL, // Orientation
                false,                   // Show Legend
                true,                    // Use tooltips
                false                    // Generate URLs
        );

        // Customize chart appearance
        chart.setBackgroundPaint(backgroundColor);

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlinePaint(null);
        plot.setRangeGridlinePaint(new Color(230, 230, 230));

        // Customize renderer
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setBarPainter(new StandardBarPainter());
        renderer.setSeriesPaint(0, secondaryColor);
        renderer.setShadowVisible(false);

        // Improve axis appearance
        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setTickLabelFont(labelFont);
        domainAxis.setLabelFont(headerFont);

        plot.getRangeAxis().setTickLabelFont(labelFont);
        plot.getRangeAxis().setLabelFont(headerFont);

        // Create chart panel
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(600, 400));
        chartPanel.setBackground(backgroundColor);

        // Update the panel
        this.chartPanel.removeAll();

        // Add title
        chartTitleLabel.setText("Average Delay by Airport in " + year);
        this.chartPanel.add(chartTitleLabel, BorderLayout.NORTH);

        // Add chart
        this.chartPanel.add(chartPanel, BorderLayout.CENTER);

        // Generate summary text
        StringBuilder summary = new StringBuilder();
        summary.append("Average Delay by Airport in ").append(year).append("\n\n");

        if (data.isEmpty()) {
            summary.append("No delay data available for the selected year.");
        } else {
            // Calculate overall average
            double totalDelay = 0;
            for (Double delay : data.values()) {
                totalDelay += delay;
            }
            double avgDelay = totalDelay / data.size();

            summary.append("Analysis shows the average departure delay times across ")
                    .append(data.size()).append(" airports in ").append(year).append(".\n\n");

            // Top 3 airports with most delays
            count = 0;
            summary.append("Airports with longest average delays:\n");
            for (Map.Entry<Double, String> entry : sortedData.entrySet()) {
                summary.append("• ").append(entry.getValue())
                        .append(": ").append(String.format("%.1f", entry.getKey()))
                        .append(" minutes\n");
                count++;
                if (count >= 3) break;
            }

            summary.append("\nOverall average delay across all airports: ")
                    .append(String.format("%.1f", avgDelay)).append(" minutes.");

            summary.append("\n\nFactors that typically contribute to airport delays include:");
            summary.append("\n• Weather conditions");
            summary.append("\n• Airport congestion");
            summary.append("\n• Air traffic control constraints");
            summary.append("\n• Airline operational issues");
        }

        // Update summary text
        summaryTextArea.setText(summary.toString());

        // Refresh UI
        revalidate();
        repaint();
    }

    /**
     * Displays a chart of average delay over time for a specific airport with UK date formatting.
     */
    public void showTimeSeriesChart(Map<String, Double> data, String airportName) {
        currentChartType = "timeseries";

        // Create time series
        TimeSeries series = new TimeSeries("Average Delay");

        // UK date format
        SimpleDateFormat ukDateFormat = new SimpleDateFormat("MM/yyyy");

        // Add data to series in chronological order
        for (Map.Entry<String, Double> entry : data.entrySet()) {
            try {
                // Parse month/year
                Date date = ukDateFormat.parse(entry.getKey());
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);

                int month = cal.get(Calendar.MONTH) + 1; // Calendar months are 0-based
                int year = cal.get(Calendar.YEAR);

                series.add(new Month(month, year), entry.getValue());
            } catch (ParseException e) {
                System.err.println("Invalid month/year format: " + entry.getKey());
            }
        }

        // Create dataset
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(series);

        // Create chart
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                null,  // Title set separately
                "Date",                   // X-Axis Label
                "Average Delay (minutes)", // Y-Axis Label
                dataset,                  // Dataset
                false,                    // Show Legend
                true,                     // Use tooltips
                false                     // Generate URLs
        );

        // Customize chart appearance
        chart.setBackgroundPaint(backgroundColor);

        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlinePaint(null);
        plot.setDomainGridlinePaint(new Color(230, 230, 230));
        plot.setRangeGridlinePaint(new Color(230, 230, 230));

        // Customize renderer
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesPaint(0, primaryColor);
        renderer.setSeriesStroke(0, new BasicStroke(2.0f));
        renderer.setSeriesShapesVisible(0, true);
        plot.setRenderer(renderer);

        // Customize date axis with UK format (DD/MM/YYYY)
        DateAxis dateAxis = (DateAxis) plot.getDomainAxis();
        dateAxis.setDateFormatOverride(new SimpleDateFormat("MM/yyyy")); // Show as month/year
        dateAxis.setTickLabelFont(labelFont);
        dateAxis.setLabelFont(headerFont);

        plot.getRangeAxis().setTickLabelFont(labelFont);
        plot.getRangeAxis().setLabelFont(headerFont);

        // Create chart panel
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(600, 400));
        chartPanel.setBackground(backgroundColor);

        // Update the panel
        this.chartPanel.removeAll();

        // Add title
        chartTitleLabel.setText("Delay Trends for " + airportName);
        this.chartPanel.add(chartTitleLabel, BorderLayout.NORTH);

        // Add chart
        this.chartPanel.add(chartPanel, BorderLayout.CENTER);

        // Generate summary text
        StringBuilder summary = new StringBuilder();
        summary.append("Delay Trends Analysis for ").append(airportName).append("\n\n");

        if (data.isEmpty()) {
            summary.append("No delay trend data available for this airport.");
        } else {
            // Calculate statistics
            double totalDelay = 0;
            double maxDelay = Double.MIN_VALUE;
            double minDelay = Double.MAX_VALUE;
            String peakMonth = "";
            String lowestMonth = "";

            for (Map.Entry<String, Double> entry : data.entrySet()) {
                double delay = entry.getValue();
                totalDelay += delay;

                if (delay > maxDelay) {
                    maxDelay = delay;
                    peakMonth = entry.getKey();
                }

                if (delay < minDelay) {
                    minDelay = delay;
                    lowestMonth = entry.getKey();
                }
            }

            double avgDelay = totalDelay / data.size();

            summary.append("This chart shows how average flight delays have changed over time ")
                    .append("for departures from ").append(airportName).append(".\n\n");

            summary.append("Key findings:\n");
            summary.append("• Average delay across all months: ")
                    .append(String.format("%.1f", avgDelay)).append(" minutes\n");
            summary.append("• Peak delay: ").append(String.format("%.1f", maxDelay))
                    .append(" minutes in ").append(peakMonth).append("\n");
            summary.append("• Lowest delay: ").append(String.format("%.1f", minDelay))
                    .append(" minutes in ").append(lowestMonth).append("\n\n");

            // Seasonal pattern analysis
            summary.append("Possible factors affecting seasonal delay patterns include:");
            summary.append("\n• Weather conditions (winter snowfall, summer thunderstorms)");
            summary.append("\n• Holiday traffic increases (summer holidays, Christmas period)");
            summary.append("\n• Scheduled maintenance periods");
            summary.append("\n• Air traffic and staffing changes");
        }

        // Update summary text
        summaryTextArea.setText(summary.toString());

        // Refresh UI
        revalidate();
        repaint();
    }
}