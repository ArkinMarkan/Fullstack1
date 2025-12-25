package com.moviebookingapp.controller;
package com.moviebookingapp.repository;

import com.moviebookingapp.model.Movie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class MovieRepositoryTest {

    @Autowired
    private MovieRepository movieRepository;

    private Movie m1;
    private Movie m2;
    private Movie m3;

    @BeforeEach
    void setup() {
        movieRepository.deleteAll();

        m1 = new Movie();
        m1.setMovieName("Avatar");
        m1.setTheatreName("Regal");
        m1.setStatus("BOOK_ASAP");
        m1.setTotalTickets(100);
        m1.setAvailableTickets(60);
        m1.setGenre("Sci-Fi");
        m1.setLanguage("English");
        movieRepository.save(m1);

        m2 = new Movie();
        m2.setMovieName("Avatar");
        m2.setTheatreName("AMC");
        m2.setStatus("SOLD_OUT");
        m2.setTotalTickets(120);
        m2.setAvailableTickets(0);
        m2.setGenre("Sci-Fi");
        m2.setLanguage("English");
        movieRepository.save(m2);

        m3 = new Movie();
        m3.setMovieName("Oppenheimer");
        m3.setTheatreName("Regal");
        m3.setStatus("BOOK_ASAP");
        m3.setTotalTickets(90);
        m3.setAvailableTickets(30);
        m3.setGenre("Drama");
        m3.setLanguage("English");
        movieRepository.save(m3);
    }

    @Test
    @DisplayName("findByMovieNameAndTheatreName should return composite key match")
    void findByMovieNameAndTheatreName() {
        Optional<Movie> found = movieRepository.findByMovieNameAndTheatreName("Avatar", "Regal");
        assertThat(found).isPresent();
        assertThat(found.get().getAvailableTickets()).isEqualTo(60);
    }

    @Test
    @DisplayName("findAvailableMovies and findSoldOutMovies should filter by status")
    void statusFilters() {
        assertThat(movieRepository.findAvailableMovies())
                .extracting(Movie::getMovieName)
                .contains("Avatar", "Oppenheimer");
        assertThat(movieRepository.findSoldOutMovies())
                .extracting(Movie::getTheatreName)
                .containsExactly("AMC");
    }

    @Test
    @DisplayName("search methods should be case-insensitive partial matches")
    void caseInsensitiveSearch() {
        List<Movie> nameMatches = movieRepository.findByMovieNameContainingIgnoreCase("ava");
        assertThat(nameMatches).hasSize(2);
        List<Movie> theatreMatches = movieRepository.findByTheatreNameContainingIgnoreCase("reg");
        assertThat(theatreMatches).hasSize(2);
    }

    @Test
    @DisplayName("findMoviesWithAvailableTickets should filter by threshold")
    void findMoviesWithAvailableTickets() {
        List<Movie> res = movieRepository.findMoviesWithAvailableTickets(50);
        assertThat(res).extracting(Movie::getTheatreName).contains("Regal");
        assertThat(res).extracting(Movie::getAvailableTickets).allMatch(t -> t > 50);
    }

    @Test
    @DisplayName("getMovieStatistics should return aggregated projection")
    void getMovieStatistics() {
        List<MovieRepository.MovieStats> stats = movieRepository.getMovieStatistics();
        assertThat(stats).isNotEmpty();
        MovieRepository.MovieStats avatarStats = stats.stream().filter(s -> s.getId().equals("Avatar")).findFirst().orElse(null);
        assertThat(avatarStats).isNotNull();
        assertThat(avatarStats.getTotalTheatres()).isEqualTo(2L);
        assertThat(avatarStats.getTotalTickets()).isEqualTo(220L);
        assertThat(avatarStats.getAvailableTickets()).isEqualTo(60L);
    }

    @Test
    @DisplayName("searchMovies should search across name, theatre, genre, language")
    void searchMovies() {
        List<Movie> res = movieRepository.searchMovies("drama");
        assertThat(res).extracting(Movie::getMovieName).contains("Oppenheimer");
        assertThat(movieRepository.searchMovies("amc")).extracting(Movie::getTheatreName).contains("AMC");
    }

    @Test
    @DisplayName("findDistinct names should return unique values")
    void distinctValues() {
        assertThat(movieRepository.findDistinctMovieNames()).contains("Avatar", "Oppenheimer");
        assertThat(movieRepository.findDistinctTheatreNames()).contains("Regal", "AMC");
    }
}
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
