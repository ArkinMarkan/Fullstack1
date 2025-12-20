package com.moviebookingapp.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Embeddable value object representing a movie show time (time + date + optional screen).
 */
@Embeddable
public class ShowTime {

    @Column(name = "show_time", nullable = false)
    private String time; // e.g., "14:00:00" (HH:mm:ss)

    @Column(name = "show_date", nullable = false)
    private LocalDate date; // e.g., 2025-12-20

    @Column(name = "screen_number")
    private String screenNumber; // optional

    public ShowTime() {}

    public ShowTime(String time, LocalDate date) {
        this.time = time;
        this.date = date;
    }

    public ShowTime(String time, LocalDate date, String screenNumber) {
        this.time = time;
        this.date = date;
        this.screenNumber = screenNumber;
    }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getScreenNumber() { return screenNumber; }
    public void setScreenNumber(String screenNumber) { this.screenNumber = screenNumber; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ShowTime showTime = (ShowTime) o;
        return Objects.equals(time, showTime.time) &&
               Objects.equals(date, showTime.date) &&
               Objects.equals(screenNumber, showTime.screenNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(time, date, screenNumber);
    }

    @Override
    public String toString() {
        return "ShowTime{" +
                "time='" + time + '\'' +
                ", date=" + date +
                ", screenNumber='" + screenNumber + '\'' +
                '}';
    }
}
