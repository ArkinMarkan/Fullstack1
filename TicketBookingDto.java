package com.moviebookingapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for ticket booking request
 */
public class TicketBookingDto {
    
    @NotBlank(message = "Movie name is mandatory")
    @JsonProperty("movie_name")
    private String movieName;
    
    @NotBlank(message = "Theatre name is mandatory")
    @JsonProperty("theatre_name")
    private String theatreName;
    
    @NotNull(message = "Number of tickets is mandatory")
    @Min(value = 1, message = "Number of tickets must be at least 1")
    @Max(value = 10, message = "Maximum 10 tickets can be booked at once")
    @JsonProperty("number_of_tickets")
    private Integer numberOfTickets;
    
    @NotNull(message = "Seat numbers are mandatory")
    @JsonProperty("seat_numbers")
    private List<String> seatNumbers;
    
    // Constructors
    public TicketBookingDto() {}
    
    public TicketBookingDto(String movieName, String theatreName, 
                           Integer numberOfTickets, List<String> seatNumbers) {
        this.movieName = movieName;
        this.theatreName = theatreName;
        this.numberOfTickets = numberOfTickets;
        this.seatNumbers = seatNumbers;
    }
    
    // Getters and Setters
    public String getMovieName() {
        return movieName;
    }
    
    public void setMovieName(String movieName) {
        this.movieName = movieName;
    }
    
    public String getTheatreName() {
        return theatreName;
    }
    
    public void setTheatreName(String theatreName) {
        this.theatreName = theatreName;
    }
    
    public Integer getNumberOfTickets() {
        return numberOfTickets;
    }
    
    public void setNumberOfTickets(Integer numberOfTickets) {
        this.numberOfTickets = numberOfTickets;
    }
    
    public List<String> getSeatNumbers() {
        return seatNumbers;
    }
    
    public void setSeatNumbers(List<String> seatNumbers) {
        this.seatNumbers = seatNumbers;
    }
    
    // Validation methods
    public boolean isValidSeatCount() {
        return seatNumbers != null && numberOfTickets != null && seatNumbers.size() == numberOfTickets;
    }
    
    @Override
    public String toString() {
        return "TicketBookingDto{" +
                "movieName='" + movieName + '\'' +
                ", theatreName='" + theatreName + '\'' +
                ", numberOfTickets=" + numberOfTickets +
                ", seatNumbers=" + seatNumbers +
                '}';
    }
}
