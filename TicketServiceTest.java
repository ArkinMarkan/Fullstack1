package com.moviebookingapp.service;

import com.moviebookingapp.dto.TicketBookingDto;
import com.moviebookingapp.exception.TicketBookingException;
import com.moviebookingapp.exception.ValidationException;
import com.moviebookingapp.model.Movie;
import com.moviebookingapp.model.Ticket;
import com.moviebookingapp.model.User;
import com.moviebookingapp.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private MovieService movieService;

    @Mock
    private UserService userService;

    @InjectMocks
    private TicketService ticketService;

    private TicketBookingDto bookingDto;
    private User user;
    private Movie movie;

    @BeforeEach
    void setup() {
        bookingDto = new TicketBookingDto();
        bookingDto.setMovieName("Avengers Endgame");
        bookingDto.setTheatreName("PVR Cinemas");
        bookingDto.setNumberOfTickets(2);
        bookingDto.setSeatNumbers(Arrays.asList("A1", "A2"));

        user = new User();
        user.setId(1L);
        user.setLoginId("john_doe");

        movie = new Movie("Avengers Endgame", "PVR Cinemas", 100);
        movie.setAvailableTickets(50);
        movie.setStatus("BOOK_ASAP");
    }

    @Test
    void bookTicket_Success() {
        when(userService.findByLoginId("john_doe")).thenReturn(user);
        when(movieService.getMovieByNameAndTheatre("Avengers Endgame", "PVR Cinemas")).thenReturn(movie);
        when(ticketRepository.findByMovieTheatreWithSeatNumbers(anyString(), anyString())).thenReturn(Collections.emptyList());
        ArgumentCaptor<Ticket> ticketCaptor = ArgumentCaptor.forClass(Ticket.class);
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> {
            Ticket t = invocation.getArgument(0);
            // simulate booking reference assignment by repository
            t.setBookingReference("BR-12345");
            t.setStatus(Ticket.TicketStatus.CONFIRMED);
            return t;
        });

        Ticket result = ticketService.bookTicket(bookingDto, "john_doe");

        assertNotNull(result);
        assertEquals("BR-12345", result.getBookingReference());
        verify(ticketRepository).save(ticketCaptor.capture());
        Ticket saved = ticketCaptor.getValue();
        assertEquals("Avengers Endgame", saved.getMovieName());
        assertEquals("PVR Cinemas", saved.getTheatreName());
        verify(movieService).updateAvailableTickets("Avengers Endgame", "PVR Cinemas", 2);
    }

    @Test
    void bookTicket_InvalidSeatCount_ThrowsValidationException() {
        bookingDto.setSeatNumbers(Arrays.asList("A1"));
        assertThrows(ValidationException.class, () -> ticketService.bookTicket(bookingDto, "john_doe"));
        verify(ticketRepository, never()).save(any());
    }

    @Test
    void bookTicket_MoreThanMaxTickets_ThrowsValidationException() {
        bookingDto.setNumberOfTickets(11);
        bookingDto.setSeatNumbers(Arrays.asList("A1","A2","A3","A4","A5","A6","A7","A8","A9","A10","A11"));
        assertThrows(ValidationException.class, () -> ticketService.bookTicket(bookingDto, "john_doe"));
        verify(ticketRepository, never()).save(any());
    }

    @Test
    void bookTicket_DuplicateSeats_ThrowsValidationException() {
        bookingDto.setSeatNumbers(Arrays.asList("A1","A1"));
        assertThrows(ValidationException.class, () -> ticketService.bookTicket(bookingDto, "john_doe"));
        verify(ticketRepository, never()).save(any());
    }

    @Test
    void bookTicket_MovieNotBookable_ThrowsTicketBookingException() {
        movie.setStatus("SOLD_OUT");
        when(userService.findByLoginId("john_doe")).thenReturn(user);
        when(movieService.getMovieByNameAndTheatre("Avengers Endgame", "PVR Cinemas")).thenReturn(movie);
        assertThrows(TicketBookingException.class, () -> ticketService.bookTicket(bookingDto, "john_doe"));
        verify(ticketRepository, never()).save(any());
    }

    @Test
    void bookTicket_NotEnoughTickets_ThrowsTicketBookingException() {
        movie.setAvailableTickets(1);
        when(userService.findByLoginId("john_doe")).thenReturn(user);
        when(movieService.getMovieByNameAndTheatre("Avengers Endgame", "PVR Cinemas")).thenReturn(movie);
        when(ticketRepository.findByMovieTheatreWithSeatNumbers(anyString(), anyString())).thenReturn(Collections.emptyList());
        assertThrows(TicketBookingException.class, () -> ticketService.bookTicket(bookingDto, "john_doe"));
        verify(ticketRepository, never()).save(any());
    }

    @Test
    void bookTicket_SeatAlreadyBooked_ThrowsTicketBookingException() {
        when(userService.findByLoginId("john_doe")).thenReturn(user);
        when(movieService.getMovieByNameAndTheatre("Avengers Endgame", "PVR Cinemas")).thenReturn(movie);
        Ticket existing = new Ticket("Avengers Endgame","PVR Cinemas",1, Collections.singletonList("A1"), 2L, "jane");
        when(ticketRepository.findByMovieTheatreWithSeatNumbers("Avengers Endgame","PVR Cinemas"))
                .thenReturn(Collections.singletonList(existing));
        assertThrows(TicketBookingException.class, () -> ticketService.bookTicket(bookingDto, "john_doe"));
        verify(ticketRepository, never()).save(any());
    }

    @Test
    void bookTicket_MovieUpdateFails_RollsBackTicket() {
        when(userService.findByLoginId("john_doe")).thenReturn(user);
        when(movieService.getMovieByNameAndTheatre("Avengers Endgame", "PVR Cinemas")).thenReturn(movie);
        when(ticketRepository.findByMovieTheatreWithSeatNumbers(anyString(), anyString())).thenReturn(Collections.emptyList());
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> {
            Ticket t = invocation.getArgument(0);
            t.setBookingReference("BR-FAIL");
            t.setStatus(Ticket.TicketStatus.CONFIRMED);
            return t;
        });
        doThrow(new RuntimeException("DB error")).when(movieService).updateAvailableTickets(anyString(), anyString(), anyInt());

        assertThrows(TicketBookingException.class, () -> ticketService.bookTicket(bookingDto, "john_doe"));
        verify(ticketRepository).delete(argThat(t -> "BR-FAIL".equals(t.getBookingReference())));
    }

    @Test
    void getTicketsByUser_ReturnsTickets() {
        when(ticketRepository.findByUserLoginId("john_doe")).thenReturn(Arrays.asList(
                new Ticket("M","T",1, Collections.singletonList("A1"), 1L, "john_doe")
        ));
        List<Ticket> tickets = ticketService.getTicketsByUser("john_doe");
        assertEquals(1, tickets.size());
        verify(ticketRepository).findByUserLoginId("john_doe");
    }

    @Test
    void getBookedTicketsByMovie_FiltersConfirmed() {
        Ticket confirmed = new Ticket("M","T",1, Collections.singletonList("A1"), 1L, "john");
        confirmed.setStatus(Ticket.TicketStatus.CONFIRMED);
        Ticket cancelled = new Ticket("M","T",1, Collections.singletonList("A2"), 1L, "john");
        cancelled.setStatus(Ticket.TicketStatus.CANCELLED);
        when(ticketRepository.findByMovieNameAndTheatreName("M","T"))
                .thenReturn(Arrays.asList(confirmed, cancelled));
        List<Ticket> booked = ticketService.getBookedTicketsByMovie("M","T");
        assertEquals(1, booked.size());
    }

    @Test
    void getTicketByBookingReference_Found() {
        Ticket t = new Ticket("M","T",1, Collections.singletonList("A1"), 1L, "john");
        t.setBookingReference("BR-1");
        when(ticketRepository.findByBookingReference("BR-1")).thenReturn(Optional.of(t));
        Ticket found = ticketService.getTicketByBookingReference("BR-1");
        assertEquals("BR-1", found.getBookingReference());
    }

    @Test
    void getTicketByBookingReference_NotFound_Throws() {
        when(ticketRepository.findByBookingReference("NA")).thenReturn(Optional.empty());
        assertThrows(TicketBookingException.class, () -> ticketService.getTicketByBookingReference("NA"));
    }

    @Test
    void cancelTicket_Success() {
        Ticket t = new Ticket("M","T",1, Collections.singletonList("A1"), 1L, "john");
        t.setBookingReference("BR-1");
        t.setStatus(Ticket.TicketStatus.CONFIRMED);
        when(ticketRepository.findByBookingReference("BR-1")).thenReturn(Optional.of(t));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));

        Ticket cancelled = ticketService.cancelTicket("BR-1", "john");
        assertEquals(Ticket.TicketStatus.CANCELLED, cancelled.getStatus());
        verify(movieService).recalculateTicketStatus("M", "T");
    }

    @Test
    void cancelTicket_OtherUser_Throws() {
        Ticket t = new Ticket("M","T",1, Collections.singletonList("A1"), 1L, "jane");
        t.setBookingReference("BR-1");
        t.setStatus(Ticket.TicketStatus.CONFIRMED);
        when(ticketRepository.findByBookingReference("BR-1")).thenReturn(Optional.of(t));
        assertThrows(TicketBookingException.class, () -> ticketService.cancelTicket("BR-1", "john"));
    }

    @Test
    void cancelTicket_AlreadyCancelled_Throws() {
        Ticket t = new Ticket("M","T",1, Collections.singletonList("A1"), 1L, "john");
        t.setBookingReference("BR-1");
        t.setStatus(Ticket.TicketStatus.CANCELLED);
        when(ticketRepository.findByBookingReference("BR-1")).thenReturn(Optional.of(t));
        assertThrows(TicketBookingException.class, () -> ticketService.cancelTicket("BR-1", "john"));
    }

    @Test
    void getBookingStatsByMovie_Delegates() {
        ticketService.getBookingStatsByMovie();
        verify(ticketRepository).getBookingStatsByMovie();
    }

    @Test
    void getBookingStatsByTheatre_Delegates() {
        ticketService.getBookingStatsByTheatre();
        verify(ticketRepository).getBookingStatsByTheatre();
    }

    @Test
    void getUserBookingHistory_ByNumericId() {
        when(ticketRepository.findUserBookingHistory(1L)).thenReturn(Collections.emptyList());
        List<Ticket> history = ticketService.getUserBookingHistory("1");
        assertNotNull(history);
        verify(ticketRepository).findUserBookingHistory(1L);
    }

    @Test
    void getUserBookingHistory_ByLoginId() {
        when(userService.findByLoginId("john")).thenReturn(user);
        when(ticketRepository.findUserBookingHistory(1L)).thenReturn(Collections.emptyList());
        List<Ticket> history = ticketService.getUserBookingHistory("john");
        assertNotNull(history);
        verify(ticketRepository).findUserBookingHistory(1L);
    }

    @Test
    void countBookedTickets_Delegates() {
        ticketService.countBookedTickets("M","T");
        verify(ticketRepository).countBookedTicketsByMovieAndTheatre("M","T");
    }

    @Test
    void getAllTickets_Delegates() {
        ticketService.getAllTickets();
        verify(ticketRepository).findAll();
    }

    @Test
    void getConfirmedTickets_Delegates() {
        ticketService.getConfirmedTickets();
        verify(ticketRepository).findConfirmedTickets();
    }
}
