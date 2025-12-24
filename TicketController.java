package com.moviebookingapp.controller;

import com.moviebookingapp.model.Ticket;
import com.moviebookingapp.repository.MovieRepository;
import com.moviebookingapp.repository.TicketRepository;
import com.moviebookingapp.service.TicketService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TicketController.class)
@AutoConfigureMockMvc(addFilters = false)
class TicketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TicketService ticketService;
    
    @MockBean
    private com.moviebookingapp.security.JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @WithMockUser(username = "john", roles = {"USER"})
    @DisplayName("POST /{moviename}/add books ticket")
    void bookTickets_user() throws Exception {
        Ticket t = new Ticket();
        t.setBookingReference("BR123");
        Mockito.when(ticketService.bookTicket(Mockito.any(), Mockito.anyString())).thenReturn(t);
        String body = "{\"movieName\":\"Avatar\",\"theatreName\":\"IMAX\",\"numberOfTickets\":2}";
        mockMvc.perform(post("/api/v1.0/moviebooking/Avatar/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.bookingReference").value("BR123"));
    }

    @Test
    @WithMockUser(username = "john", roles = {"USER"})
    @DisplayName("GET /tickets/user returns current user's tickets")
    void getUserTickets() throws Exception {
        Mockito.when(ticketService.getTicketsByUser("john")).thenReturn(List.of());
        mockMvc.perform(get("/api/v1.0/moviebooking/tickets/user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("GET /{moviename}/tickets returns movie tickets (admin)")
    void getMovieTickets_admin() throws Exception {
        Mockito.when(ticketService.getBookedTicketsByMovie("Avatar", "IMAX"))
                .thenReturn(List.of());
        mockMvc.perform(get("/api/v1.0/moviebooking/Avatar/tickets").param("theatreName", "IMAX"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "john", roles = {"USER"})
    @DisplayName("GET /tickets/{bookingReference} returns a ticket for owner")
    void getTicketByReference_userOwns() throws Exception {
        Ticket t = new Ticket();
        t.setBookingReference("BR123");
        t.setUserLoginId("john");
        Mockito.when(ticketService.getTicketByBookingReference("BR123")).thenReturn(t);
        mockMvc.perform(get("/api/v1.0/moviebooking/tickets/BR123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
                
    }

    @Test
    @WithMockUser(username = "john", roles = {"USER"})
    @DisplayName("DELETE /tickets/{bookingReference}/cancel cancels ticket")
    void cancelTicket_user() throws Exception {
        Ticket t = new Ticket();
        t.setBookingReference("BR123");
        Mockito.when(ticketService.cancelTicket("BR123", "john")).thenReturn(t);
        mockMvc.perform(delete("/api/v1.0/moviebooking/tickets/BR123/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("GET /tickets/all returns all tickets (admin)")
    void getAllTickets_admin() throws Exception {
        Mockito.when(ticketService.getAllTickets()).thenReturn(List.of());
        mockMvc.perform(get("/api/v1.0/moviebooking/tickets/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("GET /tickets/stats/movies returns movie stats")
    void getMovieBookingStats_admin() throws Exception {
        TicketRepository.MovieBookingStats stats = Mockito.mock(TicketRepository.MovieBookingStats.class);
        Mockito.when(ticketService.getBookingStatsByMovie()).thenReturn(List.of(stats));
        mockMvc.perform(get("/api/v1.0/moviebooking/tickets/stats/movies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("GET /tickets/stats/theatres returns theatre stats")
    void getTheatreBookingStats_admin() throws Exception {
        TicketRepository.TheatreBookingStats stats = Mockito.mock(TicketRepository.TheatreBookingStats.class); 
        Mockito.when(ticketService.getBookingStatsByTheatre()).thenReturn(List.of(stats));
        mockMvc.perform(get("/api/v1.0/moviebooking/tickets/stats/theatres"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("GET /{moviename}/tickets/count returns count")
    void countMovieTickets_admin() throws Exception {
        Mockito.when(ticketService.countBookedTickets("Avatar", "IMAX")).thenReturn(5L);
        mockMvc.perform(get("/api/v1.0/moviebooking/Avatar/tickets/count").param("theatreName", "IMAX"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(5));
    }

    @Test
    @WithMockUser(username = "john", roles = {"USER"})
    @DisplayName("GET /tickets/{username} returns tickets for same user")
    void getUserTicketsByUsername_userSelf() throws Exception {
        Mockito.when(ticketService.getTicketsByUser("john")).thenReturn(List.of());
        mockMvc.perform(get("/api/v1.0/moviebooking/tickets/john"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "john", roles = {"USER"})
    @DisplayName("DELETE /tickets/cancel/{bookingReference} cancels via alias")
    void cancelTicketAlias_user() throws Exception {
        Ticket t = new Ticket();
        t.setBookingReference("BR123");
        Mockito.when(ticketService.cancelTicket("BR123", "john")).thenReturn(t);
        mockMvc.perform(delete("/api/v1.0/moviebooking/tickets/cancel/BR123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
