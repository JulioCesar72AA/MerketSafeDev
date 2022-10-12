package mx.softel.cirwireless.utils;

public class ElapsedDate {

    private long elapsedDays;
    private long elapsedHours;
    private long elapsedMinutes;
    private long elapsedSeconds;


    public ElapsedDate(long elapsedDays, long elapsedHours, long elapsedMinutes, long elapsedSeconds) {
        this.elapsedDays = elapsedDays;
        this.elapsedHours = elapsedHours;
        this.elapsedMinutes = elapsedMinutes;
        this.elapsedSeconds = elapsedSeconds;
    }


    // Getters ---------------------------------------------------------------------------
    public long getElapsedDays() { return elapsedDays; }

    public long getElapsedHours() { return elapsedHours; }

    public long getElapsedMinutes() { return elapsedMinutes; }

    public long getElapsedSeconds() { return elapsedSeconds; }
    // --------------------------------------------------------------------------------------
}
