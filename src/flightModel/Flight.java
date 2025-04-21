package flightModel;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Flight {

    private int flightId;
    private LocalDate date;
    private String airlineCode;
    private String airlineName;
    private int flightNumber;
    private String originCode;
    private String originCity;
    private String destCode;
    private String destCity;
    private int scheduledDeparture;
    private int actualDeparture;
    private int scheduledArrival;
    private int actualArrival;
    private boolean cancelled;
    private String cancellationCode;
    private boolean diverted;
    private List<Delay> delays = new ArrayList<>();

    public Flight() {
        // Our default constructor.
    }

    public String getFullFlightNumber() {
        return airlineCode + flightNumber;
    }

    public int getDelayMinutes() {
        if (cancelled || diverted || actualArrival == 0 || scheduledArrival == 0) {
            return 0;
        }

        // Convert times to minutes since midnight for easier comparison.
        int scheduledArrivalMinutes = timeToMinutes(scheduledArrival);
        int actualArrivalMinutes = timeToMinutes(actualArrival);

        // Calculate difference, considering midnight crossings.
        int diffMinutes = actualArrivalMinutes - scheduledArrivalMinutes;
        if (diffMinutes < -720) { // More than 12 hours negative -> must have crossed midnight
            diffMinutes += 1440; // Add 24 hours in minutes
        } else if (diffMinutes > 720) { // More than 12 hours positive -> scheduled must have crossed midnight
            diffMinutes -= 1440; // Subtract 24 hours in minutes
        }

        return Math.max(0, diffMinutes); // Negative means early arrival, count as 0 delay.
    }

    public String getStatus() {
        if (cancelled) {
            return "Cancelled" + (cancellationCode != null ? " (" + formatCancellationCode(cancellationCode) + ")" : "");
        } else if (diverted) {
            return "Diverted";
        } else if (getDelayMinutes() > 15) {
            return "Delayed (" + getDelayMinutes() + " min)";
        } else if (actualArrival > 0) {
            return "On Time";
        } else {
            return "Scheduled";
        }
    }
    private String formatCancellationCode(String code) {
        if (code == null) return "";

        switch (code.trim().toUpperCase()) {
            case "A": return "Carrier";
            case "B": return "Weather";
            case "C": return "National Air System";
            case "D": return "Security";
            default: return code;
        }
    }

    private int timeToMinutes(int time) {
        int hours = time / 100;
        int minutes = time % 100;
        return hours * 60 + minutes;
    }

    public static String formatTime(int time) {
        if (time == 0) {
            return "N/A";
        }
        int hours = time / 100;
        int minutes = time % 100;
        return String.format("%02d:%02d", hours, minutes);
    }

    public static class Delay {
        private final String reason;
        private final int minutes;

        public Delay(String reason, int minutes) {
            this.reason = reason;
            this.minutes = minutes;
        }

        public String getReason() {
            return reason;
        }

        public int getMinutes() {
            return minutes;
        }

        public String getFormattedReason() {
            switch (reason) {
                case "CARRIER": return "Airline";
                case "WEATHER": return "Weather";
                case "NAS": return "Air Traffic Control";
                case "SECURITY": return "Security";
                case "LATE_AIRCRAFT": return "Late Aircraft";
                default: return reason;
            }
        }
    }

    // Getters and setters
    public int getFlightId() {
        return flightId;
    }

    public void setFlightId(int flightId) {
        this.flightId = flightId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setDateFromString(String dateStr) {
        if (dateStr != null && dateStr.length() == 8) {
            try {
                this.date = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("ddMMyyyy"));
//                this.date = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyyMMyyyy"));
            } catch (Exception e) {
                System.err.println("Invalid date format: " + dateStr);
            }
        }
    }

    public String getFormattedDate() {
        return date != null ? date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "N/A";
//        return date != null ? date.format(DateTimeFormatter.ofPattern("yyyy/MM/dd")) : "N/A";
    }

    public String getAirlineCode() {
        return airlineCode;
    }

    public void setAirlineCode(String airlineCode) {
        this.airlineCode = airlineCode;
    }

    public String getAirlineName() {
        return airlineName;
    }

    public void setAirlineName(String airlineName) {
        this.airlineName = airlineName;
    }

    public int getFlightNumber() {
        return flightNumber;
    }

    public void setFlightNumber(int flightNumber) {
        this.flightNumber = flightNumber;
    }

    public String getOriginCode() {
        return originCode;
    }

    public void setOriginCode(String originCode) {
        this.originCode = originCode;
    }

    public String getOriginCity() {
        return originCity;
    }

    public void setOriginCity(String originCity) {
        this.originCity = originCity;
    }

    public String getDestCode() {
        return destCode;
    }

    public void setDestCode(String destCode) {
        this.destCode = destCode;
    }

    public String getDestCity() {
        return destCity;
    }

    public void setDestCity(String destCity) {
        this.destCity = destCity;
    }

    public int getScheduledDeparture() {
        return scheduledDeparture;
    }

    public void setScheduledDeparture(int scheduledDeparture) {
        this.scheduledDeparture = scheduledDeparture;
    }

    public int getActualDeparture() {
        return actualDeparture;
    }

    public void setActualDeparture(int actualDeparture) {
        this.actualDeparture = actualDeparture;
    }

    public int getScheduledArrival() {
        return scheduledArrival;
    }

    public void setScheduledArrival(int scheduledArrival) {
        this.scheduledArrival = scheduledArrival;
    }

    public int getActualArrival() {
        return actualArrival;
    }

    public void setActualArrival(int actualArrival) {
        this.actualArrival = actualArrival;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public String getCancellationCode() {
        return cancellationCode;
    }

    public void setCancellationCode(String cancellationCode) {
        this.cancellationCode = cancellationCode;
    }

    public boolean isDiverted() {
        return diverted;
    }

    public void setDiverted(boolean diverted) {
        this.diverted = diverted;
    }

    public List<Delay> getDelays() {
        return delays;
    }

    public void addDelay(Delay delay) {
        this.delays.add(delay);
    }

    public void setDelays(List<Delay> delays) {
        this.delays = delays;
    }

    public String getOriginDisplay() {
        return originCode + " - " + originCity;
    }

    public String getDestinationDisplay() {
        return destCode + " - " + destCity;
    }

    @Override
    public String toString() {
        return getFullFlightNumber() + " from " + originCode + " to " + destCode + " on " + getFormattedDate();
    }
}





