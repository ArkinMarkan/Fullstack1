package com.moviebookingapp.controller;

import com.moviebookingapp.dto.ApiResponseDto;
import com.moviebookingapp.dto.TicketBookingDto;
import com.moviebookingapp.model.Ticket;
import com.moviebookingapp.repository.TicketRepository;
import com.moviebookingapp.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for ticket booking operations
 */
@RestController
@RequestMapping("/api/v1.0/moviebooking")
@Tag(name = "Tickets", description = "Ticket booking and management APIs")
@CrossOrigin(origins = "*", maxAge = 3600)
public class TicketController {
    
    private static final Logger logger = LoggerFactory.getLogger(TicketController.class);
    
    @Autowired
    private TicketService ticketService;

    // Helper methods to safely get current user info without assuming principal type
    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (authentication != null) ? authentication.getName() : null;
    }

    private boolean isCurrentUserAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) return false;
        for (GrantedAuthority auth : authentication.getAuthorities()) {
            if ("ROLE_ADMIN".equals(auth.getAuthority())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Book tickets for a movie
     * POST /api/v1.0/moviebooking/{moviename}/add
     */
    @PostMapping("/{moviename}/add")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "Book movie tickets", description = "Book tickets for a specific movie")
    public ResponseEntity<ApiResponseDto<Ticket>> bookTickets(
            @PathVariable String moviename,
            @Valid @RequestBody TicketBookingDto bookingDto) {
        
        String userLoginId = getCurrentUsername();
        if (userLoginId == null) {
            throw new com.moviebookingapp.exception.AuthorizationException("User is not authenticated");
        }
        
        logger.info("Ticket booking request received for movie: {} by user: {} | payload: {}",
            moviename, userLoginId, bookingDto);
        
        // Ensure movie name matches path parameter
        bookingDto.setMovieName(moviename);
        
        Ticket bookedTicket = ticketService.bookTicket(bookingDto, userLoginId);
        
        ApiResponseDto<Ticket> response = ApiResponseDto.success(
            "Tickets booked successfully", bookedTicket);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get user's booked tickets
     * GET /api/v1.0/moviebooking/tickets/user
     */
    @GetMapping("/tickets/user")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "Get user tickets", description = "Get all tickets booked by the current user")
    public ResponseEntity<ApiResponseDto<List<Ticket>>> getUserTickets() {
        
        String userLoginId = getCurrentUsername();
        logger.info("Request received to fetch tickets for user: {}", userLoginId);
        
        List<Ticket> tickets = ticketService.getTicketsByUser(userLoginId);
        
        ApiResponseDto<List<Ticket>> response = ApiResponseDto.success(
            "User tickets fetched successfully", tickets);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get booked tickets for a movie (Admin only)
     * GET /api/v1.0/moviebooking/{moviename}/tickets
     */
    @GetMapping("/{moviename}/tickets")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get movie tickets", description = "Get all booked tickets for a specific movie (Admin only)")
    public ResponseEntity<ApiResponseDto<List<Ticket>>> getMovieTickets(
            @PathVariable String moviename,
            @RequestParam String theatreName) {
        
        logger.info("Request received to fetch tickets for movie: {}, theatre: {}", 
            moviename, theatreName);
        
        List<Ticket> tickets = ticketService.getBookedTicketsByMovie(moviename, theatreName);
        
        ApiResponseDto<List<Ticket>> response = ApiResponseDto.success(
            "Movie tickets fetched successfully", tickets);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get ticket by booking reference
     * GET /api/v1.0/moviebooking/tickets/{bookingReference}
     */
    @GetMapping("/tickets/{bookingReference}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "Get ticket by reference", description = "Get ticket details by booking reference")
    public ResponseEntity<ApiResponseDto<Ticket>> getTicketByReference(
            @PathVariable String bookingReference) {
        
        logger.info("Request received to fetch ticket with reference: {}", bookingReference);
        
        Ticket ticket = ticketService.getTicketByBookingReference(bookingReference);
        
        // Check if user has access to this ticket (users can only view their own tickets)
        String currentUsername = getCurrentUsername();
        boolean isAdmin = isCurrentUserAdmin();
        if (!isAdmin && ticket != null && ticket.getUserLoginId() != null && !ticket.getUserLoginId().equals(currentUsername)) {
            throw new com.moviebookingapp.exception.AuthorizationException(
                "You can only view your own tickets");
        }
        
        ApiResponseDto<Ticket> response = ApiResponseDto.success(
            "Ticket fetched successfully", ticket);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Cancel ticket
     * DELETE /api/v1.0/moviebooking/tickets/{bookingReference}/cancel
     */
    @DeleteMapping("/tickets/{bookingReference}/cancel")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "Cancel ticket", description = "Cancel a booked ticket")
    public ResponseEntity<ApiResponseDto<Ticket>> cancelTicket(
            @PathVariable String bookingReference) {
        
        String userLoginId = getCurrentUsername();
        logger.info("Ticket cancellation request received for reference: {} by user: {}", 
            bookingReference, userLoginId);
        
        Ticket cancelledTicket = ticketService.cancelTicket(bookingReference, userLoginId);
        
        ApiResponseDto<Ticket> response = ApiResponseDto.success(
            "Ticket cancelled successfully", cancelledTicket);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get all tickets (Admin only)
     * GET /api/v1.0/moviebooking/tickets/all
     */
    @GetMapping("/tickets/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all tickets", description = "Get all tickets in the system (Admin only)")
    public ResponseEntity<ApiResponseDto<List<Ticket>>> getAllTickets() {
        
        logger.info("Request received to fetch all tickets");
        
        List<Ticket> tickets = ticketService.getAllTickets();
        
        ApiResponseDto<List<Ticket>> response = ApiResponseDto.success(
            "All tickets fetched successfully", tickets);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get booking statistics by movie (Admin only)
     * GET /api/v1.0/moviebooking/tickets/stats/movies
     */
    @GetMapping("/tickets/stats/movies")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get movie booking statistics", description = "Get booking statistics by movie (Admin only)")
    public ResponseEntity<ApiResponseDto<List<TicketRepository.MovieBookingStats>>> getMovieBookingStats() {
        
        logger.info("Request received for movie booking statistics");
        
        List<TicketRepository.MovieBookingStats> stats = ticketService.getBookingStatsByMovie();
        
        ApiResponseDto<List<TicketRepository.MovieBookingStats>> response = ApiResponseDto.success(
            "Movie booking statistics fetched successfully", stats);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get booking statistics by theatre (Admin only)
     * GET /api/v1.0/moviebooking/tickets/stats/theatres
     */
    @GetMapping("/tickets/stats/theatres")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get theatre booking statistics", description = "Get booking statistics by theatre (Admin only)")
    public ResponseEntity<ApiResponseDto<List<TicketRepository.TheatreBookingStats>>> getTheatreBookingStats() {
        
        logger.info("Request received for theatre booking statistics");
        
        List<TicketRepository.TheatreBookingStats> stats = ticketService.getBookingStatsByTheatre();
        
        ApiResponseDto<List<TicketRepository.TheatreBookingStats>> response = ApiResponseDto.success(
            "Theatre booking statistics fetched successfully", stats);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Count booked tickets for a movie
     * GET /api/v1.0/moviebooking/{moviename}/tickets/count
     */
    @GetMapping("/{moviename}/tickets/count")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Count movie tickets", description = "Count total booked tickets for a movie (Admin only)")
    public ResponseEntity<ApiResponseDto<Long>> countMovieTickets(
            @PathVariable String moviename,
            @RequestParam String theatreName) {
        
        logger.info("Request received to count tickets for movie: {}, theatre: {}", 
            moviename, theatreName);
        
        long count = ticketService.countBookedTickets(moviename, theatreName);
        
        ApiResponseDto<Long> response = ApiResponseDto.success(
            "Ticket count fetched successfully", count);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get user's booked tickets by username (alias to match frontend)
     * GET /api/v1.0/moviebooking/tickets/{username}
     */
    @GetMapping("/tickets/{username}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "Get tickets by username", description = "Get all tickets booked by the specified user")
    public ResponseEntity<ApiResponseDto<List<Ticket>>> getUserTicketsByUsername(
            @PathVariable String username) {
        String currentUsername = getCurrentUsername();
        boolean isAdmin = isCurrentUserAdmin();
        
        // Non-admin users can only fetch their own tickets
        if (!isAdmin && !currentUsername.equalsIgnoreCase(username)) {
            throw new com.moviebookingapp.exception.AuthorizationException(
                "You can only view your own tickets");
        }
        
        logger.info("Request received to fetch tickets for username: {} by user: {}", username, currentUsername);
        List<Ticket> tickets = ticketService.getTicketsByUser(username);
        ApiResponseDto<List<Ticket>> response = ApiResponseDto.success(
            "User tickets fetched successfully", tickets);
        return ResponseEntity.ok(response);
    }

    /**
     * Cancel ticket (alias to match frontend path)
     * DELETE /api/v1.0/moviebooking/tickets/cancel/{bookingReference}
     */
    @DeleteMapping("/tickets/cancel/{bookingReference}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "Cancel ticket (alias)", description = "Cancel a booked ticket using alias path")
    public ResponseEntity<ApiResponseDto<Ticket>> cancelTicketAlias(
            @PathVariable String bookingReference) {
        String userLoginId = getCurrentUsername();
        logger.info("Alias cancel endpoint hit for reference: {} by user: {}", bookingReference, userLoginId);
        Ticket cancelledTicket = ticketService.cancelTicket(bookingReference, userLoginId);
        ApiResponseDto<Ticket> response = ApiResponseDto.success(
            "Ticket cancelled successfully", cancelledTicket);
        return ResponseEntity.ok(response);
    }
}
