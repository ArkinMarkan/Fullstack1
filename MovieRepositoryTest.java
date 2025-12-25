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
        List<Movie> nameMatches = movieRepository.findByMovieNameContainingIgnoreCase("Ant");
        assertThat(nameMatches).hasSize(2);
        List<Movie> theatreMatches = movieRepository.findByTheatreNameContainingIgnoreCase("Cine");
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
        assertThat(res).extracting(Movie::getMovieName).contains("RRR");
        assertThat(movieRepository.searchMovies("RRR")).extracting(Movie::getTheatreName).contains("Cinepolis");
    }

    @Test
    @DisplayName("findDistinct names should return unique values")
    void distinctValues() {
        assertThat(movieRepository.findDistinctMovieNames()).contains("AntMan", "Avengers Endgame");
        assertThat(movieRepository.findDistinctTheatreNames()).contains("INOX", "INOX Multiplex");
    }
}
