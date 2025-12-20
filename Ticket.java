package com.moviebookingapp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.math.BigDecimal;
import jakarta.persistence.Convert;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Ticket entity representing booked tickets with MySQL/JPA
 */
@Entity
@Table(name = "tickets",
       indexes = {
           @Index(name = "idx_movie_name", columnList = "movie_name"),
           @Index(name = "idx_theatre_name", columnList = "theatre_name"),
           @Index(name = "idx_user_id", columnList = "user_id"),
           @Index(name = "idx_user_login_id", columnList = "user_login_id"),
           @Index(name = "idx_booking_date", columnList = "booking_date"),
           @Index(name = "idx_status", columnList = "status")
       })
@EntityListeners(AuditingEntityListener.class)
public class Ticket {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @NotBlank(message = "Movie name is mandatory")
    @Column(name = "movie_name", nullable = false, length = 100)
    private String movieName;
    
    @NotBlank(message = "Theatre name is mandatory")
    @Column(name = "theatre_name", nullable = false, length = 100)
    private String theatreName;
    
    @NotNull(message = "Number of tickets is mandatory")
    @Min(value = 1, message = "Number of tickets must be at least 1")
    @Max(value = 10, message = "Maximum 10 tickets can be booked at once")
    @Column(name = "number_of_tickets", nullable = false)
    private Integer numberOfTickets;

    // Store seat numbers as JSON in tickets.seat_numbers
    @NotNull(message = "Seat numbers are mandatory")
    @Column(name = "seat_numbers", columnDefinition = "JSON")
    @Convert(converter = SeatNumbersJsonConverter.class)
    private List<String> seatNumbers;
    
    @NotNull(message = "User ID is mandatory")
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @NotBlank(message = "User login ID is mandatory")
    @Column(name = "user_login_id", nullable = false, length = 50)
    private String userLoginId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private TicketStatus status = TicketStatus.CONFIRMED;
    
    @Column(name = "total_price", precision = 10, scale = 2)
    private BigDecimal totalPrice;
    
    @Column(name = "booking_reference", nullable = false, length = 20)
    private String bookingReference;

    // Avoid creating extra column not defined in SQL
    @Transient
    private LocalDateTime showDateTime;
    
    @CreatedDate
    @Column(name = "booking_date", nullable = false, updatable = false)
    private LocalDateTime bookedAt;

    // Add created_date to match SQL audit columns
    @CreatedDate
    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;
    
    @LastModifiedDate
    @Column(name = "modified_date")
    private LocalDateTime updatedAt;
    
    // Enum for ticket status
    public enum TicketStatus {
        CONFIRMED, CANCELLED, EXPIRED
    }
    
    // Constructors
    public Ticket() {}
    
    public Ticket(String movieName, String theatreName, Integer numberOfTickets, 
                  List<String> seatNumbers, Long userId, String userLoginId) {
        this.movieName = movieName;
        this.theatreName = theatreName;
        this.numberOfTickets = numberOfTickets;
        this.seatNumbers = seatNumbers;
        this.userId = userId;
        this.userLoginId = userLoginId;
        this.status = TicketStatus.CONFIRMED;
        this.bookingReference = generateBookingReference();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
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
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getUserLoginId() {
        return userLoginId;
    }
    
    public void setUserLoginId(String userLoginId) {
        this.userLoginId = userLoginId;
    }
    
    public TicketStatus getStatus() {
        return status;
    }
    
    public void setStatus(TicketStatus status) {
        this.status = status;
    }
    
    public BigDecimal getTotalPrice() {
        return totalPrice;
    }
    
    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }
    
    public String getBookingReference() {
        return bookingReference;
    }
    
    public void setBookingReference(String bookingReference) {
        this.bookingReference = bookingReference;
    }
    
    public LocalDateTime getShowDateTime() {
        return showDateTime;
    }
    
    public void setShowDateTime(LocalDateTime showDateTime) {
        this.showDateTime = showDateTime;
    }
    
    public LocalDateTime getBookedAt() {
        return bookedAt;
    }
    
    public void setBookedAt(LocalDateTime bookedAt) {
        this.bookedAt = bookedAt;
    }
    
    public LocalDateTime getCreatedDate() {
        return createdDate;
    }
    
    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    // Business methods
    private String generateBookingReference() {
        return "MB" + System.currentTimeMillis() + 
               (int)(Math.random() * 1000);
    }
    
    public boolean isActive() {
        return status == TicketStatus.CONFIRMED;
    }
    
    public void cancel() {
        this.status = TicketStatus.CANCELLED;
    }
    
    public String getMovieTheatreKey() {
        return movieName + "_" + theatreName;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ticket ticket = (Ticket) o;
        return Objects.equals(id, ticket.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "Ticket{" +
                "id='" + id + '\'' +
                ", movieName='" + movieName + '\'' +
                ", theatreName='" + theatreName + '\'' +
                ", numberOfTickets=" + numberOfTickets +
                ", userId='" + userId + '\'' +
                ", status=" + status +
                ", bookingReference='" + bookingReference + '\'' +
                '}';
    }

    // JSON Converter for List<String> <-> JSON stored in seat_numbers
    @Converter
    public static class SeatNumbersJsonConverter implements AttributeConverter<List<String>, String> {
        private static final ObjectMapper mapper = new ObjectMapper();
        @Override
        public String convertToDatabaseColumn(List<String> attribute) {
            try {
                return attribute == null ? null : mapper.writeValueAsString(attribute);
            } catch (Exception e) {
                throw new IllegalArgumentException("Failed to serialize seat numbers", e);
            }
        }
        @Override
        public List<String> convertToEntityAttribute(String dbData) {
            try {
                return dbData == null ? null : mapper.readValue(dbData, new TypeReference<List<String>>(){});
            } catch (Exception e) {
                throw new IllegalArgumentException("Failed to deserialize seat numbers", e);
            }
        }
    }
}
