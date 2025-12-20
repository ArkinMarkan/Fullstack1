package com.moviebookingapp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.math.BigDecimal;

/**
 * Movie entity representing movies in the system with MySQL/JPA
 */
@Entity
@Table(name = "movies", 
       uniqueConstraints = @UniqueConstraint(
           name = "uk_movie_theatre", 
           columnNames = {"movie_name", "theatre_name"}
       ),
       indexes = {
           @Index(name = "idx_movie_name", columnList = "movie_name"),
           @Index(name = "idx_theatre_name", columnList = "theatre_name"),
           @Index(name = "idx_status", columnList = "status")
       })
@EntityListeners(AuditingEntityListener.class)
@IdClass(MovieCompositeKey.class)
public class Movie {
    
    @Id
    @Column(name = "movie_name", nullable = false, length = 100)
    @NotBlank(message = "Movie name is mandatory")
    private String movieName;
    
    @Id
    @Column(name = "theatre_name", nullable = false, length = 100)
    @NotBlank(message = "Theatre name is mandatory")
    private String theatreName;
    
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @NotNull(message = "Total tickets is mandatory")
    @Min(value = 1, message = "Total tickets must be at least 1")
    @Column(name = "total_tickets", nullable = false)
    private Integer totalTickets;
    
    @Min(value = 0, message = "Available tickets cannot be negative")
    @Column(name = "available_tickets")
    private Integer availableTickets;
    
    @Column(name = "status", length = 20)
    private String status = "BOOK_ASAP"; // BOOK_ASAP, SOLD_OUT
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "genre", length = 100)
    private String genre;
    
    @Column(name = "language", length = 50)
    private String language;
    
    @Column(name = "duration")
    private Integer duration; // in minutes
    
    // Removed precision/scale as Hibernate 6 disallows scale on floating types
    @Column(name = "rating", precision = 3, scale = 1)
    private BigDecimal rating; // Movie rating (e.g., 8.5)
    
    @Column(name = "release_date")
    private LocalDateTime releaseDate;
    
    // Replace List<String> with embeddable ShowTime mapping
    @ElementCollection
    @CollectionTable(name = "movie_show_times", 
                    joinColumns = {
                        @JoinColumn(name = "movie_name"),
                        @JoinColumn(name = "theatre_name")
                    })
    private List<ShowTime> showTimes;
    
    // Removed precision/scale as Hibernate 6 disallows scale on floating types
    @Column(name = "ticket_price", precision = 10, scale = 2)
    private BigDecimal ticketPrice;
    
    @Column(name = "poster_url")
    private String posterUrl;
    
    @CreatedDate
    @Column(name = "created_at", nullable = true, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public Movie() {}
    
    public Movie(String movieName, String theatreName, Integer totalTickets) {
        this.movieName = movieName;
        this.theatreName = theatreName;
        this.totalTickets = totalTickets;
        this.availableTickets = totalTickets;
        this.status = "BOOK_ASAP";
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
    
    public Integer getTotalTickets() {
        return totalTickets;
    }
    
    public void setTotalTickets(Integer totalTickets) {
        this.totalTickets = totalTickets;
        if (this.availableTickets == null) {
            this.availableTickets = totalTickets;
        }
    }
    
    public Integer getAvailableTickets() {
        return availableTickets;
    }
    
    public void setAvailableTickets(Integer availableTickets) {
        this.availableTickets = availableTickets;
        updateStatus();
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getGenre() {
        return genre;
    }
    
    public void setGenre(String genre) {
        this.genre = genre;
    }
    
    public String getLanguage() {
        return language;
    }
    
    public void setLanguage(String language) {
        this.language = language;
    }
    
    public Integer getDuration() {
        return duration;
    }
    
    public void setDuration(Integer duration) {
        this.duration = duration;
    }
    
    public String getPosterUrl() {
        return posterUrl;
    }
    
    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public BigDecimal getRating() {
        return rating;
    }
    
    public void setRating(BigDecimal rating) {
        this.rating = rating;
    }
    
    public LocalDateTime getReleaseDate() {
        return releaseDate;
    }
    
    public void setReleaseDate(LocalDateTime releaseDate) {
        this.releaseDate = releaseDate;
    }
    
    public List<ShowTime> getShowTimes() {
        return showTimes;
    }
    
    public void setShowTimes(List<ShowTime> showTimes) {
        this.showTimes = showTimes;
    }
    
    public BigDecimal getTicketPrice() {
        return ticketPrice;
    }
    
    public void setTicketPrice(BigDecimal ticketPrice) {
        this.ticketPrice = ticketPrice;
    }
    
    // Business methods
    public void updateStatus() {
        if (availableTickets != null) {
            this.status = availableTickets <= 0 ? "SOLD_OUT" : "BOOK_ASAP";
        }
    }
    
    public boolean isAvailable() {
        return availableTickets != null && availableTickets > 0;
    }
    
    public void bookTickets(int numberOfTickets) {
        if (availableTickets >= numberOfTickets) {
            availableTickets -= numberOfTickets;
            updateStatus();
        } else {
            throw new IllegalArgumentException("Not enough tickets available");
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Movie movie = (Movie) o;
        return Objects.equals(movieName, movie.movieName) && 
               Objects.equals(theatreName, movie.theatreName);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(movieName, theatreName);
    }
    
    @Override
    public String toString() {
        return "Movie{" +
                "id='" + id + '\'' +
                ", movieName='" + movieName + '\'' +
                ", theatreName='" + theatreName + '\'' +
                ", totalTickets=" + totalTickets +
                ", availableTickets=" + availableTickets +
                ", status='" + status + '\'' +
                '}';
    }
    
    // Builder Pattern
    public static MovieBuilder builder() {
        return new MovieBuilder();
    }
    
    public static class MovieBuilder {
        private String movieName;
        private String theatreName;
        private Integer totalTickets;
        private Integer availableTickets;
        private String status = "BOOK_ASAP";
        private String description;
        private String genre;
        private String language;
        private Integer duration;
        private BigDecimal rating;
        private LocalDateTime releaseDate;
        private List<ShowTime> showTimes;
        private BigDecimal ticketPrice;
        private String posterUrl;
        
        public MovieBuilder movieName(String movieName) {
            this.movieName = movieName;
            return this;
        }
        
        public MovieBuilder theatreName(String theatreName) {
            this.theatreName = theatreName;
            return this;
        }
        
        public MovieBuilder totalTickets(Integer totalTickets) {
            this.totalTickets = totalTickets;
            this.availableTickets = totalTickets;
            return this;
        }
        
        public MovieBuilder availableTickets(Integer availableTickets) {
            this.availableTickets = availableTickets;
            return this;
        }
        
        public MovieBuilder status(String status) {
            this.status = status;
            return this;
        }
        
        public MovieBuilder description(String description) {
            this.description = description;
            return this;
        }
        
        public MovieBuilder genre(String genre) {
            this.genre = genre;
            return this;
        }
        
        public MovieBuilder language(String language) {
            this.language = language;
            return this;
        }
        
        public MovieBuilder duration(Integer duration) {
            this.duration = duration;
            return this;
        }
        
        public MovieBuilder rating(BigDecimal rating) {
            this.rating = rating;
            return this;
        }
        
        public MovieBuilder releaseDate(LocalDateTime releaseDate) {
            this.releaseDate = releaseDate;
            return this;
        }
        
        public MovieBuilder showTimes(List<ShowTime> showTimes) {
            this.showTimes = showTimes;
            return this;
        }
        
        public MovieBuilder ticketPrice(BigDecimal ticketPrice) {
            this.ticketPrice = ticketPrice;
            return this;
        }
        
        public MovieBuilder posterUrl(String posterUrl) {
            this.posterUrl = posterUrl;
            return this;
        }
        
        public Movie build() {
            Movie movie = new Movie();
            movie.movieName = this.movieName;
            movie.theatreName = this.theatreName;
            movie.totalTickets = this.totalTickets;
            movie.availableTickets = this.availableTickets;
            movie.status = this.status;
            movie.description = this.description;
            movie.genre = this.genre;
            movie.language = this.language;
            movie.duration = this.duration;
            movie.rating = this.rating;
            movie.releaseDate = this.releaseDate;
            movie.showTimes = this.showTimes;
            movie.ticketPrice = this.ticketPrice;
            movie.posterUrl = this.posterUrl;
            return movie;
        }
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.updatedAt == null) {
            this.updatedAt = this.createdAt;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
