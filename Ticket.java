package com.moviebookingapp.repository;

import com.moviebookingapp.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Ticket entity with JPA/MySQL
 */
@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    
    /**
     * Find tickets by user ID
     */
    List<Ticket> findByUserId(Long userId);
    
    /**
     * Find tickets by user login ID
     */
    List<Ticket> findByUserLoginId(String userLoginId);
    
    /**
     * Find tickets by movie name and theatre name
     */
    List<Ticket> findByMovieNameAndTheatreName(String movieName, String theatreName);
    
    /**
     * Find tickets by movie name
     */
    List<Ticket> findByMovieName(String movieName);
    
    /**
     * Find tickets by theatre name
     */
    List<Ticket> findByTheatreName(String theatreName);
    
    /**
     * Find tickets by status
     */
    List<Ticket> findByStatus(Ticket.TicketStatus status);
    
    /**
     * Find confirmed tickets
     */
    @Query("SELECT t FROM Ticket t WHERE t.status = 'CONFIRMED'")
    List<Ticket> findConfirmedTickets();
    
    /**
     * Find tickets by booking reference
     */
    Optional<Ticket> findByBookingReference(String bookingReference);
    
    /**
     * Count total tickets booked for a movie and theatre
     */
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.movieName = :movieName AND t.theatreName = :theatreName AND t.status = 'CONFIRMED'")
    long countBookedTicketsByMovieAndTheatre(@Param("movieName") String movieName, @Param("theatreName") String theatreName);
    
    /**
     * Sum number of tickets for a movie and theatre
     */
    @Query("SELECT COALESCE(SUM(t.numberOfTickets), 0) FROM Ticket t WHERE t.movieName = :movieName AND t.theatreName = :theatreName AND t.status = 'CONFIRMED'")
    Integer sumTicketsByMovieAndTheatre(@Param("movieName") String movieName, @Param("theatreName") String theatreName);
    
    /**
     * Find tickets booked within a date range
     */
    @Query("SELECT t FROM Ticket t WHERE t.bookedAt BETWEEN :startDate AND :endDate")
    List<Ticket> findByBookingDateBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    /**
     * Find tickets by user and status
     */
    List<Ticket> findByUserIdAndStatus(Long userId, Ticket.TicketStatus status);
    
    /**
     * Find tickets with specific seat numbers for a movie and theatre
     */
    @Query("SELECT t FROM Ticket t WHERE t.movieName = :movieName AND t.theatreName = :theatreName AND t.status = 'CONFIRMED' AND t.seatNumbers IS NOT NULL")
    List<Ticket> findByMovieTheatreWithSeatNumbers(@Param("movieName") String movieName, @Param("theatreName") String theatreName);
    
    /**
     * Get booking statistics by movie
     */
    @Query("""
        SELECT 
            t.movieName as movieName,
            COUNT(t) as totalBookings,
            SUM(t.numberOfTickets) as totalTickets,
            SUM(t.totalPrice) as totalRevenue
        FROM Ticket t 
        WHERE t.status = 'CONFIRMED' 
        GROUP BY t.movieName 
        ORDER BY SUM(t.numberOfTickets) DESC
        """)
    List<MovieBookingStats> getBookingStatsByMovie();
    
    /**
     * Get booking statistics by theatre
     */
    @Query("""
        SELECT 
            t.theatreName as theatreName,
            COUNT(t) as totalBookings,
            SUM(t.numberOfTickets) as totalTickets,
            SUM(t.totalPrice) as totalRevenue
        FROM Ticket t 
        WHERE t.status = 'CONFIRMED' 
        GROUP BY t.theatreName 
        ORDER BY SUM(t.numberOfTickets) DESC
        """)
    List<TheatreBookingStats> getBookingStatsByTheatre();
    
    /**
     * Get booking statistics by movie and theatre combination
     */
    @Query("""
        SELECT 
            t.movieName as movieName,
            t.theatreName as theatreName,
            COUNT(t) as totalBookings,
            SUM(t.numberOfTickets) as totalTickets,
            SUM(t.totalPrice) as totalRevenue,
            AVG(t.numberOfTickets) as avgTicketsPerBooking
        FROM Ticket t 
        WHERE t.status = 'CONFIRMED' 
        GROUP BY t.movieName, t.theatreName 
        ORDER BY SUM(t.totalPrice) DESC
        """)
    List<Object[]> getDetailedBookingStats();
    
    /**
     * Get user booking history with details
     */
    @Query("SELECT t FROM Ticket t WHERE t.userId = :userId ORDER BY t.bookedAt DESC")
    List<Ticket> findUserBookingHistory(@Param("userId") Long userId);
    
    /**
     * Get user booking summary
     */
    @Query("""
        SELECT 
            COUNT(t) as totalBookings,
            SUM(t.numberOfTickets) as totalTickets,
            SUM(CASE WHEN t.status = 'CONFIRMED' THEN t.totalPrice ELSE 0 END) as totalSpent,
            MAX(t.bookedAt) as lastBookingDate
        FROM Ticket t 
        WHERE t.userId = :userId
        """)
    Object[] getUserBookingSummary(@Param("userId") Long userId);
    
    /**
     * Find recent bookings
     */
    @Query("SELECT t FROM Ticket t WHERE t.bookedAt >= :since ORDER BY t.bookedAt DESC")
    List<Ticket> findRecentBookings(@Param("since") LocalDateTime since);
    
    /**
     * Check if seat numbers are already booked for a movie-theatre combination
     * Uses MySQL JSON_CONTAINS to search within JSON array stored in tickets.seat_numbers
     */
    @Query(value = "SELECT COUNT(*) > 0 FROM tickets t WHERE t.movie_name = :movieName AND t.theatre_name = :theatreName AND t.status = 'CONFIRMED' AND JSON_CONTAINS(t.seat_numbers, CONCAT('\"', :seatNumber, '\"'))", nativeQuery = true)
    boolean isSeatAlreadyBooked(@Param("movieName") String movieName, @Param("theatreName") String theatreName, @Param("seatNumber") String seatNumber);
    
    /**
     * Get monthly booking report
     */
    @Query("""
        SELECT 
            YEAR(t.bookedAt) as year,
            MONTH(t.bookedAt) as month,
            COUNT(t) as totalBookings,
            SUM(t.numberOfTickets) as totalTickets,
            SUM(t.totalPrice) as totalRevenue
        FROM Ticket t
        WHERE t.status = 'CONFIRMED' AND t.bookedAt >= :startDate
        GROUP BY YEAR(t.bookedAt), MONTH(t.bookedAt)
        ORDER BY YEAR(t.bookedAt) DESC, MONTH(t.bookedAt) DESC
        """)
    List<Object[]> getMonthlyBookingReport(@Param("startDate") LocalDateTime startDate);
    
    /**
     * Delete cancelled tickets older than specified date
     */
    @Query("DELETE FROM Ticket t WHERE t.status = 'CANCELLED' AND t.bookedAt < :cutoffDate")
    int deleteCancelledTicketsOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * Find top movies by booking count
     */
    @Query("""
        SELECT 
            t.movieName,
            t.theatreName,
            COUNT(t) as bookingCount
        FROM Ticket t 
        WHERE t.status = 'CONFIRMED' 
        GROUP BY t.movieName, t.theatreName 
        ORDER BY COUNT(t) DESC
        """)
    List<Object[]> findTopMoviesByBookings();
    
    /**
     * Check if user has already booked tickets for a specific movie-theatre
     */
    @Query("SELECT COUNT(t) > 0 FROM Ticket t WHERE t.userLoginId = :loginId AND t.movieName = :movieName AND t.theatreName = :theatreName AND t.status = 'CONFIRMED'")
    boolean hasUserBookedMovie(@Param("loginId") String loginId, @Param("movieName") String movieName, @Param("theatreName") String theatreName);
    
    /**
     * Interface for ticket summary projection
     */
    interface TicketSummary {
        Integer getTotalTickets();
        BigDecimal getTotalRevenue();
    }
    
    /**
     * Interface for movie booking statistics projection
     */
    interface MovieBookingStats {
        String getMovieName();
        Long getTotalBookings();
        Long getTotalTickets();
        BigDecimal getTotalRevenue();
    }
    
    /**
     * Interface for theatre booking statistics projection
     */
    interface TheatreBookingStats {
        String getTheatreName();
        Long getTotalBookings();
        Long getTotalTickets();
        BigDecimal getTotalRevenue();
    }
}
