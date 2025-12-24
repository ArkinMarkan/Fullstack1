package com.moviebookingapp.controller;

import com.moviebookingapp.model.Movie;
import com.moviebookingapp.repository.MovieRepository;
import com.moviebookingapp.service.MovieService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MovieController.class)
@AutoConfigureMockMvc(addFilters = false)
class MovieControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MovieService movieService;

    @Test
    @DisplayName("GET /all returns movies")
    void getAllMovies() throws Exception {
        Movie m = new Movie();
        m.setMovieName("Avatar");
        Mockito.when(movieService.getAllMovies()).thenReturn(List.of(m));

        mockMvc.perform(get("/api/v1.0/moviebooking/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].movieName").value("Avatar"));
    }

    @Test
    @DisplayName("GET /movies/search/{moviename} returns matches")
    void searchMoviesByName() throws Exception {
        Movie m = new Movie();
        m.setMovieName("Matrix");
        Mockito.when(movieService.searchMoviesByName("Matrix"))
                .thenReturn(List.of(m));

        mockMvc.perform(get("/api/v1.0/moviebooking/movies/search/Matrix"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].movieName").value("Matrix"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("PUT /{moviename}/update/{status} updates ticket status")
    void updateTicketStatus_admin() throws Exception {
        Movie m = new Movie();
        m.setMovieName("Avatar");
        Mockito.when(movieService.updateTicketStatus(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(m);

        mockMvc.perform(put("/api/v1.0/moviebooking/Avatar/update/BOOKED")
                        .param("theatreName", "IMAX"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("DELETE /{moviename}/delete/{theatreName} deletes movie")
    void deleteMovie_admin() throws Exception {
        mockMvc.perform(delete("/api/v1.0/moviebooking/Avatar/delete/IMAX"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /available returns available movies")
    void getAvailableMovies() throws Exception {
        Mockito.when(movieService.getAvailableMovies()).thenReturn(List.of());
        mockMvc.perform(get("/api/v1.0/moviebooking/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /search returns movies by query")
    void searchMovies() throws Exception {
        Mockito.when(movieService.searchMovies("hero")).thenReturn(List.of());
        mockMvc.perform(get("/api/v1.0/moviebooking/search").param("q", "hero"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("GET /statistics returns stats for admin")
    void getMovieStatistics_admin() throws Exception {
        MovieRepository.MovieStats stats = new MovieRepository.MovieStats("Avatar", 10L);
        Mockito.when(movieService.getMovieStatistics()).thenReturn(List.of(stats));
        mockMvc.perform(get("/api/v1.0/moviebooking/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("POST /{moviename}/recalculate returns updated movie")
    void recalc_admin() throws Exception {
        Movie m = new Movie();
        Mockito.when(movieService.recalculateTicketStatus(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(m);
        mockMvc.perform(post("/api/v1.0/moviebooking/Avatar/recalculate")
                        .param("theatreName", "IMAX"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("POST /admin/add adds a movie")
    void addMovie_admin() throws Exception {
        Movie saved = new Movie();
        saved.setMovieName("New Movie");
        Mockito.when(movieService.addMovie(Mockito.any(Movie.class))).thenReturn(saved);
        String body = "{\"movieName\":\"New Movie\",\"theatreName\":\"IMAX\"}";
        mockMvc.perform(post("/api/v1.0/moviebooking/admin/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.movieName").value("New Movie"));
    }
}
