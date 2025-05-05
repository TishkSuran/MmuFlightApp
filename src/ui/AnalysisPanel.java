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


public class AnalysisPanel extends JPanel {

    // Colours.
    private final Color blueColour = new Color(41, 128, 185);
    private final Color redColour = new Color(231, 76, 60);
    private final Color bgColour = new Color(245, 245, 250);

    // Fonts.
    private final Font titleFont = new Font("Arial", Font.BOLD, 16);
    private final Font headerFont = new Font("Arial", Font.BOLD, 14);
    private final Font normalFont = new Font("Arial", Font.PLAIN, 12);

    // UI components.
    private final JPanel chartPanel;
    private final JPanel summaryPanel;
    private final JLabel chartTitleLabel;
    private final JTextArea summaryTextArea;
    private String currentChart = "none";

    public AnalysisPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(bgColour);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // Chart area setup.
        chartPanel = new JPanel(new BorderLayout());
        chartPanel.setBackground(bgColour);
        chartPanel.setBorder(createBorder("Analysis Chart"));

        // Title label.
        chartTitleLabel = new JLabel("No Analysis Selected", JLabel.CENTER);
        chartTitleLabel.setFont(titleFont);
        chartTitleLabel.setForeground(blueColour);
        chartTitleLabel.setBorder(new EmptyBorder(5, 0, 10, 0));

        // Summary area setup.
        summaryPanel = new JPanel(new BorderLayout());
        summaryPanel.setBackground(bgColour);
        summaryPanel.setBorder(createBorder("Analysis Summary"));

        // Text area for analysis text.
        summaryTextArea = new JTextArea();
        summaryTextArea.setEditable(false);
        summaryTextArea.setLineWrap(true);
        summaryTextArea.setWrapStyleWord(true);
        summaryTextArea.setFont(new Font("Arial", Font.PLAIN, 13));
        summaryTextArea.setBackground(bgColour);
        summaryTextArea.setBorder(new EmptyBorder(5, 5, 5, 5));

        JScrollPane summaryScrollPane = new JScrollPane(summaryTextArea);
        summaryScrollPane.setBorder(BorderFactory.createEmptyBorder());
        summaryPanel.add(summaryScrollPane, BorderLayout.CENTER);

        // Show welcome screen initially.
        showWelcomeScreen();

        // Layout with split pane.
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setTopComponent(chartPanel);
        splitPane.setBottomComponent(summaryPanel);
        splitPane.setResizeWeight(0.7);
        splitPane.setDividerLocation(400);

        add(splitPane, BorderLayout.CENTER);
    }

    private javax.swing.border.Border createBorder(String title) {
        return new CompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(blueColour, 1),
                        title,
                        javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                        javax.swing.border.TitledBorder.DEFAULT_POSITION,
                        headerFont,
                        blueColour
                ),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        );
    }

    private void showWelcomeScreen() {
        chartPanel.removeAll();
        chartTitleLabel.setText("Flight Analysis");
        chartPanel.add(chartTitleLabel, BorderLayout.NORTH);

        // Welcome message.
        JPanel welcomePanel = new JPanel();
        welcomePanel.setLayout(new BoxLayout(welcomePanel, BoxLayout.Y_AXIS));
        welcomePanel.setBackground(bgColour);
        welcomePanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel welcomeLabel = new JLabel("Welcome to Flight Analysis");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 18));
        welcomeLabel.setForeground(blueColour);
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
        instructionText.setBackground(bgColour);
        instructionText.setAlignmentX(Component.CENTER_ALIGNMENT);

        welcomePanel.add(welcomeLabel);
        welcomePanel.add(Box.createVerticalStrut(20));
        welcomePanel.add(instructionText);
        chartPanel.add(welcomePanel, BorderLayout.CENTER);

        // Basic instructions
        summaryTextArea.setText("Select an analysis type from the Analysis menu to get started.\n\n" +
                "The analysis will be displayed in the chart area above, and a summary of findings " +
                "will appear here. You can resize this panel by dragging the divider bar.");

        revalidate();
        repaint();
    }

    public void showAirlineDelayChart(Map<String, Double> data, int year) {
        currentChart = "airline";

        // Sort airlines by delay time (descending).
        TreeMap<Double, String> sortedAirlines = new TreeMap<>((a, b) -> b.compareTo(a));
        for (Map.Entry<String, Double> entry : data.entrySet()) {
            sortedAirlines.put(entry.getValue(), entry.getKey());
        }

        // Create chart dataset with top 10 airlines.
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        int count = 0;
        for (Map.Entry<Double, String> entry : sortedAirlines.entrySet()) {
            dataset.addValue(entry.getKey(), "Delay", entry.getValue());
            count++;
            if (count >= 10) break;
        }

        // Create horizontal bar chart.
        JFreeChart chart = ChartFactory.createBarChart(
                null,
                "Airline",
                "Average Delay (minutes)",
                dataset,
                PlotOrientation.HORIZONTAL,
                false,
                true,
                false
        );

        // Style the chart.
        chart.setBackgroundPaint(bgColour);
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlinePaint(null);
        plot.setRangeGridlinePaint(new Color(230, 230, 230));

        // Style the bars.
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setBarPainter(new StandardBarPainter());
        renderer.setSeriesPaint(0, blueColour);
        renderer.setShadowVisible(false);

        // Style the axes.
        CategoryAxis xAxis = plot.getDomainAxis();
        xAxis.setTickLabelFont(normalFont);
        xAxis.setLabelFont(headerFont);
        plot.getRangeAxis().setTickLabelFont(normalFont);
        plot.getRangeAxis().setLabelFont(headerFont);

        // Create and add chart panel.
        ChartPanel chartComponent = new ChartPanel(chart);
        chartComponent.setPreferredSize(new Dimension(600, 400));
        chartComponent.setBackground(bgColour);

        // Update UI.
        chartPanel.removeAll();
        chartTitleLabel.setText("Average Delay by Airline in " + year);
        chartPanel.add(chartTitleLabel, BorderLayout.NORTH);
        chartPanel.add(chartComponent, BorderLayout.CENTER);

        // Create summary text.
        StringBuilder summary = new StringBuilder();
        summary.append("Average Delay by Airline in ").append(year).append("\n\n");

        if (data.isEmpty()) {
            summary.append("No delay data available for ").append(year).append(".");
        } else {
            // Calculate overall average.
            double totalDelay = 0;
            for (Double delay : data.values()) {
                totalDelay += delay;
            }
            double avgDelay = totalDelay / data.size();

            summary.append("This chart shows average delay times across ")
                    .append(data.size()).append(" airlines in ").append(year).append(".\n\n");

            // Top 3 airlines with most delays.
            count = 0;
            summary.append("Airlines with longest average delays:\n");
            for (Map.Entry<Double, String> entry : sortedAirlines.entrySet()) {
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
        revalidate();
        repaint();
    }


    public void showAirportDelayChart(Map<String, Double> data, int year) {
        currentChart = "airport";

        // Sort airports by delay time (descending).
        TreeMap<Double, String> sortedAirports = new TreeMap<>((a, b) -> b.compareTo(a));
        for (Map.Entry<String, Double> entry : data.entrySet()) {
            sortedAirports.put(entry.getValue(), entry.getKey());
        }

        // Create dataset with top 10 airports.
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        int count = 0;
        for (Map.Entry<Double, String> entry : sortedAirports.entrySet()) {
            dataset.addValue(entry.getKey(), "Delay", entry.getValue());
            if (++count >= 10) break;
        }

        // Create horizontal bar chart.
        JFreeChart chart = ChartFactory.createBarChart(
                null,
                "Airport",
                "Average Delay (minutes)",
                dataset,
                PlotOrientation.HORIZONTAL,
                false,
                true,
                false
        );

        // Style the chart.
        chart.setBackgroundPaint(bgColour);
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlinePaint(null);
        plot.setRangeGridlinePaint(new Color(230, 230, 230));

        // Style the bars - use red for airports.
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setBarPainter(new StandardBarPainter());
        renderer.setSeriesPaint(0, redColour);
        renderer.setShadowVisible(false);

        // Style the axes.
        CategoryAxis xAxis = plot.getDomainAxis();
        xAxis.setTickLabelFont(normalFont);
        xAxis.setLabelFont(headerFont);
        plot.getRangeAxis().setTickLabelFont(normalFont);
        plot.getRangeAxis().setLabelFont(headerFont);

        // Create and add chart panel.
        ChartPanel chartComponent = new ChartPanel(chart);
        chartComponent.setPreferredSize(new Dimension(600, 400));
        chartComponent.setBackground(bgColour);

        // Update UI.
        chartPanel.removeAll();
        chartTitleLabel.setText("Average Delay by Airport in " + year);
        chartPanel.add(chartTitleLabel, BorderLayout.NORTH);
        chartPanel.add(chartComponent, BorderLayout.CENTER);

        // Create summary.
        StringBuilder summary = new StringBuilder();
        summary.append("Average Delay by Airport in ").append(year).append("\n\n");

        if (data.isEmpty()) {
            summary.append("No airport delay data available for ").append(year).append(".");
        } else {
            // Calculate overall average.
            double total = 0;
            for (Double delay : data.values()) {
                total += delay;
            }
            double avg = total / data.size();

            summary.append("This chart shows the average departure delay times across ")
                    .append(data.size()).append(" airports in ").append(year).append(".\n\n");

            // Top 3 worst airports.
            count = 0;
            summary.append("Airports with longest average delays:\n");
            for (Map.Entry<Double, String> entry : sortedAirports.entrySet()) {
                summary.append("• ").append(entry.getValue())
                        .append(": ").append(String.format("%.1f", entry.getKey()))
                        .append(" minutes\n");
                if (++count >= 3) break;
            }

            summary.append("\nOverall average delay across all airports: ")
                    .append(String.format("%.1f", avg)).append(" minutes.");

            // Add common delay factors.
            summary.append("\n\nCommon factors that contribute to airport delays:");
            summary.append("\n• Poor weather");
            summary.append("\n• Congestion and capacity issues");
            summary.append("\n• Air traffic control problems");
            summary.append("\n• Airline operational issues");
        }

        summaryTextArea.setText(summary.toString());
        revalidate();
        repaint();
    }


    public void showTimeSeriesChart(Map<String, Double> data, String airportName) {
        currentChart = "timeseries";

        // Create time series for the chart.
        TimeSeries series = new TimeSeries("Average Delay");
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/yyyy");

        // Add each data point to the series.
        for (Map.Entry<String, Double> entry : data.entrySet()) {
            try {
                // Parse the date (MM/YYYY).
                Date date = dateFormat.parse(entry.getKey());
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);

                // Calendar months are 0-based (0=Jan).
                int month = cal.get(Calendar.MONTH) + 1;
                int year = cal.get(Calendar.YEAR);

                series.add(new Month(month, year), entry.getValue());
            } catch (ParseException e) {
                // Skip invalid dates.
                System.err.println("Bad date format: " + entry.getKey());
            }
        }

        // Add to dataset.
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(series);

        // Create the line chart.
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                null,
                "Date",
                "Average Delay (minutes)",
                dataset,
                false,
                true,
                false
        );

        // Style the chart.
        chart.setBackgroundPaint(bgColour);
        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlinePaint(null);
        plot.setDomainGridlinePaint(new Color(230, 230, 230));
        plot.setRangeGridlinePaint(new Color(230, 230, 230));

        // Style the line.
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesPaint(0, blueColour);
        renderer.setSeriesStroke(0, new BasicStroke(2.0f));
        renderer.setSeriesShapesVisible(0, true); // Show points
        plot.setRenderer(renderer);

        // Format x-axis as dates (MM/YYYY).
        DateAxis dateAxis = (DateAxis) plot.getDomainAxis();
        dateAxis.setDateFormatOverride(new SimpleDateFormat("MM/yyyy"));
        dateAxis.setTickLabelFont(normalFont);
        dateAxis.setLabelFont(headerFont);
        plot.getRangeAxis().setTickLabelFont(normalFont);
        plot.getRangeAxis().setLabelFont(headerFont);

        // Create and add chart panel.
        ChartPanel chartComponent = new ChartPanel(chart);
        chartComponent.setPreferredSize(new Dimension(600, 400));
        chartComponent.setBackground(bgColour);

        // Update UI.
        chartPanel.removeAll();
        chartTitleLabel.setText("Delay Trends for " + airportName);
        chartPanel.add(chartTitleLabel, BorderLayout.NORTH);
        chartPanel.add(chartComponent, BorderLayout.CENTER);

        // Create summary text.
        StringBuilder summary = new StringBuilder();
        summary.append("Delay Trends for ").append(airportName).append("\n\n");

        if (data.isEmpty() || data.size() < 2) {
            summary.append("Insufficient data to show trends for this airport.");
        } else {
            // Find min, max, avg.
            double total = 0;
            double max = -1;
            double min = Double.MAX_VALUE;
            String peakMonth = "";
            String lowMonth = "";

            for (Map.Entry<String, Double> entry : data.entrySet()) {
                double delay = entry.getValue();
                total += delay;

                // Track highest delay.
                if (delay > max) {
                    max = delay;
                    peakMonth = entry.getKey();
                }

                // Track lowest delay.
                if (delay < min) {
                    min = delay;
                    lowMonth = entry.getKey();
                }
            }

            double avg = total / data.size();

            summary.append("This chart shows how flight delays have changed over time ")
                    .append("for departures from ").append(airportName).append(".\n\n");

            // Key stats.
            summary.append("Key stats:\n");
            summary.append("• Avg delay: ").append(String.format("%.1f", avg)).append(" mins\n");
            summary.append("• Worst month: ").append(peakMonth)
                    .append(" (").append(String.format("%.1f", max)).append(" mins)\n");
            summary.append("• Best month: ").append(lowMonth)
                    .append(" (").append(String.format("%.1f", min)).append(" mins)\n\n");

            // Common factors.
            summary.append("Factors affecting seasonal delay patterns:");
            summary.append("\n• Weather (winter snow, summer storms)");
            summary.append("\n• Holiday periods (summer holidays, Christmas)");
            summary.append("\n• Maintenance schedules");
            summary.append("\n• Staffing and ATC variations");
        }

        summaryTextArea.setText(summary.toString());
        revalidate();
        repaint();
    }
}