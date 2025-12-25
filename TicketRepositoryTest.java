package com.moviebookingapp.repository;

import com.moviebookingapp.model.Ticket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class TicketRepositoryTest {

    @Autowired
    private TicketRepository ticketRepository;

    private Ticket t1;
    private Ticket t2;
    private Ticket t3;

    @BeforeEach
    void setup() {
        ticketRepository.deleteAll();

        t1 = new Ticket();
        t1.setUserId(1L);
        t1.setUserLoginId("admin1");
        t1.setMovieName("Avatar");
        t1.setTheatreName("Regal");
        t1.setNumberOfTickets(2);
        t1.setTotalPrice(new BigDecimal("20.00"));
        t1.setStatus(Ticket.TicketStatus.CONFIRMED);
        t1.setBookedAt(LocalDateTime.now().minusDays(5));
        ticketRepository.save(t1);

        t2 = new Ticket();
        t2.setUserId(2L);
        t2.setUserLoginId("user1");
        t2.setMovieName("Avatar");
        t2.setTheatreName("AMC");
        t2.setNumberOfTickets(3);
        t2.setTotalPrice(new BigDecimal("30.00"));
        t2.setStatus(Ticket.TicketStatus.CANCELLED);
        t2.setBookedAt(LocalDateTime.now().minusDays(2));
        ticketRepository.save(t2);

        t3 = new Ticket();
        t3.setUserId(1L);
        t3.setUserLoginId("admin1");
        t3.setMovieName("Oppenheimer");
        t3.setTheatreName("Regal");
        t3.setNumberOfTickets(4);
        t3.setTotalPrice(new BigDecimal("40.00"));
        t3.setStatus(Ticket.TicketStatus.CONFIRMED);
        t3.setBookedAt(LocalDateTime.now().minusDays(1));
        ticketRepository.save(t3);
    }

    @Test
    @DisplayName("findByUserId should return tickets for user")
    void findByUserId() {
        List<Ticket> adminTickets = ticketRepository.findByUserId(1L);
        assertThat(adminTickets).hasSize(2);
    }

    @Test
    @DisplayName("findByMovieNameAndTheatreName should match combination")
    void findByMovieNameAndTheatreName() {
        List<Ticket> res = ticketRepository.findByMovieNameAndTheatreName("Avatar", "Regal");
        assertThat(res).hasSize(1);
        assertThat(res.get(0).getNumberOfTickets()).isEqualTo(2);
    }

    @Test
    @DisplayName("findConfirmedTickets should return only confirmed")
    void findConfirmedTickets() {
        List<Ticket> confirmed = ticketRepository.findConfirmedTickets();
        assertThat(confirmed).extracting(Ticket::getStatus).allMatch(s -> s == Ticket.TicketStatus.CONFIRMED);
        assertThat(confirmed).hasSize(2);
    }

    @Test
    @DisplayName("countBookedTicketsByMovieAndTheatre should count confirmed")
    void countBookedTicketsByMovieAndTheatre() {
        long count = ticketRepository.countBookedTicketsByMovieAndTheatre("Oppenheimer", "Regal");
        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("sumTicketsByMovieAndTheatre should sum confirmed numbers")
    void sumTicketsByMovieAndTheatre() {
        Integer sum = ticketRepository.sumTicketsByMovieAndTheatre("Oppenheimer", "Regal");
        assertThat(sum).isEqualTo(4);
    }

    @Test
    @DisplayName("findByBookingDateBetween should filter date range")
    void findByBookingDateBetween() {
        LocalDateTime start = LocalDateTime.now().minusDays(3);
        LocalDateTime end = LocalDateTime.now();
        List<Ticket> res = ticketRepository.findByBookingDateBetween(start, end);
        assertThat(res).extracting(Ticket::getMovieName).contains("Oppenheimer");
    }

    @Test
    @DisplayName("findByUserIdAndStatus should return filtered list")
    void findByUserIdAndStatus() {
        List<Ticket> res = ticketRepository.findByUserIdAndStatus(1L, Ticket.TicketStatus.CONFIRMED);
        assertThat(res).hasSize(2);
    }

    @Test
    @DisplayName("findUserBookingHistory should order by bookedAt desc")
    void findUserBookingHistory() {
        List<Ticket> res = ticketRepository.findUserBookingHistory(1L);
        assertThat(res).hasSize(2);
        assertThat(res.get(0).getBookedAt()).isAfter(res.get(1).getBookedAt());
    }

    @Test
    @DisplayName("getUserBookingSummary should return aggregate values")
    void getUserBookingSummary() {
        Object[] summary = ticketRepository.getUserBookingSummary(1L);
        assertThat(summary).isNotNull();
        assertThat(((Long) summary[0])).isEqualTo(2L); // totalBookings
        assertThat(((Long) summary[1])).isEqualTo(6L); // totalTickets
        assertThat(summary[2]).isInstanceOf(BigDecimal.class); // totalSpent
    }

    @Test
    @DisplayName("findRecentBookings should filter by since date")
    void findRecentBookings() {
        List<Ticket> res = ticketRepository.findRecentBookings(LocalDateTime.now().minusDays(2));
        assertThat(res).hasSize(1);
        assertThat(res.get(0).getMovieName()).isEqualTo("Oppenheimer");
    }

    @Test
    @DisplayName("hasUserBookedMovie should check existence for combination")
    void hasUserBookedMovie() {
        boolean booked = ticketRepository.hasUserBookedMovie("admin1", "Oppenheimer", "Regal");
        assertThat(booked).isTrue();
        boolean notBooked = ticketRepository.hasUserBookedMovie("user1", "Avatar", "Regal");
        assertThat(notBooked).isFalse();
    }

    @Test
    @DisplayName("findTopMoviesByBookings should return grouped counts")
    void findTopMoviesByBookings() {
        List<Object[]> res = ticketRepository.findTopMoviesByBookings();
        assertThat(res).isNotEmpty();
        Object[] top = res.get(0);
        assertThat(top[0]).isIn("Oppenheimer", "Avatar");
        assertThat((Long) top[2]).isGreaterThanOrEqualTo(1L);
    }

    @Test
    @DisplayName("findByBookingReference should return Optional")
    void findByBookingReference() {
        Optional<Ticket> found = ticketRepository.findByBookingReference("BR-123");
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("deleteCancelledTicketsOlderThan should delete matching rows")
    void deleteCancelledTicketsOlderThan() {
        int deleted = ticketRepository.deleteCancelledTicketsOlderThan(LocalDateTime.now().minusDays(1));
        assertThat(deleted).isEqualTo(1);
    }
}
