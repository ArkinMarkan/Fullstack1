package com.moviebookingapp.integration;

import com.moviebookingapp.dto.UserRegistrationDto;
import com.moviebookingapp.dto.LoginRequestDto;
import com.moviebookingapp.dto.TicketBookingDto;
import com.moviebookingapp.model.Movie;
import com.moviebookingapp.model.User;
import com.moviebookingapp.model.Ticket;
import com.moviebookingapp.model.ShowTime;
import com.moviebookingapp.repository.MovieRepository;
import com.moviebookingapp.repository.UserRepository;
import com.moviebookingapp.repository.TicketRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

/**
 * Integration tests for Movie Booking Application
 * Tests all user stories and requirements
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebMvc
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.liquibase.enabled=false",
    "logging.level.com.moviebookingapp=DEBUG"
})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MovieBookingIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply(springSecurity())
            .build();
            
        // Clean up test data
        ticketRepository.deleteAll();
        movieRepository.deleteAll();
        userRepository.deleteAll();
        
        // Initialize test data
        initializeTestMovies();
    }

    private void initializeTestMovies() {
        // Create sample movies as per requirement
        Movie movie1Theatre1 = Movie.builder()
            .movieName("Avengers: Endgame")
            .theatreName("PVR Cinemas")
            .totalTickets(100)
            .availableTickets(100)
            .status("BOOK_ASAP")
            .description("Epic superhero movie")
            .genre("Action")
            .language("English")
            .duration(181)
            .rating(BigDecimal.valueOf(8.4))
            .ticketPrice(BigDecimal.valueOf(250.0))
            .showTimes(toShowTimeList(Arrays.asList("10:00 AM", "2:00 PM", "6:00 PM")))
            .build();

        Movie movie1Theatre2 = Movie.builder()
            .movieName("Avengers: Endgame")
            .theatreName("INOX Multiplex")
            .totalTickets(120)
            .availableTickets(120)
            .status("BOOK_ASAP")
            .description("Epic superhero movie")
            .genre("Action")
            .language("English")
            .duration(181)
            .rating(BigDecimal.valueOf(8.4))
            .ticketPrice(BigDecimal.valueOf(280.0))
            .showTimes(toShowTimeList(Arrays.asList("11:00 AM", "3:00 PM", "7:00 PM")))
            .build();

        Movie movie2Theatre1 = Movie.builder()
            .movieName("RRR")
            .theatreName("PVR Cinemas")
            .totalTickets(80)
            .availableTickets(80)
            .status("BOOK_ASAP")
            .description("Historical action drama")
            .genre("Action, Drama")
            .language("Telugu")
            .duration(187)
            .rating(BigDecimal.valueOf(8.8))
            .ticketPrice(BigDecimal.valueOf(220.0))
            .showTimes(toShowTimeList(Arrays.asList("9:30 AM", "1:30 PM", "5:30 PM")))
            .build();

        Movie movie2Theatre2 = Movie.builder()
            .movieName("RRR")
            .theatreName("INOX Multiplex")
            .totalTickets(90)
            .availableTickets(90)
            .status("BOOK_ASAP")
            .description("Historical action drama")
            .genre("Action, Drama")
            .language("Telugu")
            .duration(187)
            .rating(BigDecimal.valueOf(8.8))
            .ticketPrice(BigDecimal.valueOf(240.0))
            .showTimes(toShowTimeList(Arrays.asList("10:30 AM", "2:30 PM", "6:30 PM")))
            .build();

        movieRepository.saveAll(Arrays.asList(movie1Theatre1, movie1Theatre2, movie2Theatre1, movie2Theatre2));
    }

    private List<ShowTime> toShowTimeList(List<String> times) {
        LocalDate date = LocalDate.now(); // or set a specific date if needed
        List<ShowTime> showTimes = new ArrayList<>();
        for (String time : times) {
            showTimes.add(new ShowTime(time, date));
        }
        return showTimes;
    }

    // US_01: Registration and Login Tests
    @Test
    void testUserRegistration_Success() throws Exception {
        UserRegistrationDto registrationDto = new UserRegistrationDto();
        registrationDto.setFirstName("John");
        registrationDto.setLastName("Doe");
        registrationDto.setEmail("john.doe@example.com");
        registrationDto.setLoginId("johndoe");
        registrationDto.setPassword("password123");
        registrationDto.setConfirmPassword("password123");
        registrationDto.setContactNumber("+919999999999");

        mockMvc.perform(post("/api/v1.0/moviebooking/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User registered successfully"))
                .andExpect(jsonPath("$.data.firstName").value("John"))
                .andExpect(jsonPath("$.data.email").value("john.doe@example.com"));
    }

    @Test
    void testUserRegistration_DuplicateLoginId() throws Exception {
        // First registration
        UserRegistrationDto registrationDto1 = new UserRegistrationDto();
        registrationDto1.setFirstName("John");
        registrationDto1.setLastName("Doe");
        registrationDto1.setEmail("john.doe@example.com");
        registrationDto1.setLoginId("johndoe");
        registrationDto1.setPassword("password123");
        registrationDto1.setConfirmPassword("password123");
        registrationDto1.setContactNumber("+919999999999");

        mockMvc.perform(post("/api/v1.0/moviebooking/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationDto1)))
                .andExpect(status().isOk());

        // Second registration with same loginId
        UserRegistrationDto registrationDto2 = new UserRegistrationDto();
        registrationDto2.setFirstName("Jane");
        registrationDto2.setLastName("Smith");
        registrationDto2.setEmail("jane.smith@example.com");
        registrationDto2.setLoginId("johndoe"); // Same loginId
        registrationDto2.setPassword("password456");
        registrationDto2.setConfirmPassword("password456");
        registrationDto2.setContactNumber("+918888888888");

        mockMvc.perform(post("/api/v1.0/moviebooking/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationDto2)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void testUserLogin_Success() throws Exception {
        // Register user first
        UserRegistrationDto registrationDto = new UserRegistrationDto();
        registrationDto.setFirstName("John");
        registrationDto.setLastName("Doe");
        registrationDto.setEmail("john.doe@example.com");
        registrationDto.setLoginId("johndoe");
        registrationDto.setPassword("password123");
        registrationDto.setConfirmPassword("password123");
        registrationDto.setContactNumber("+919999999999");

        mockMvc.perform(post("/api/v1.0/moviebooking/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationDto)))
                .andExpect(status().isOk());

        // Test login
        mockMvc.perform(get("/api/v1.0/moviebooking/login")
                .param("loginId", "johndoe")
                .param("password", "password123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").exists())
                .andExpect(jsonPath("$.data.user.loginId").value("johndoe"));
    }

    // US_02: View & Search Movies Tests
    @Test
    void testGetAllMovies() throws Exception {
        mockMvc.perform(get("/api/v1.0/moviebooking/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(4)) // 2 movies x 2 theatres
                .andExpect(jsonPath("$.data[0].movieName").exists())
                .andExpect(jsonPath("$.data[0].theatreName").exists());
    }

    @Test
    void testSearchMoviesByName_FullName() throws Exception {
        mockMvc.perform(get("/api/v1.0/moviebooking/movies/search/Avengers: Endgame"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2)) // Both theatres
                .andExpect(jsonPath("$.data[0].movieName").value("Avengers: Endgame"));
    }

    @Test
    void testSearchMoviesByName_PartialName() throws Exception {
        mockMvc.perform(get("/api/v1.0/moviebooking/movies/search/Avengers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].movieName").value("Avengers: Endgame"));
    }

    @Test
    void testSearchMoviesByName_NotFound() throws Exception {
        mockMvc.perform(get("/api/v1.0/moviebooking/movies/search/NonExistentMovie"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    // US_03: Book Tickets Tests
    @Test
    @WithMockUser(username = "johndoe", roles = "USER")
    void testBookTickets_Success() throws Exception {
        // Register user first
        registerTestUser();

        TicketBookingDto bookingDto = new TicketBookingDto();
        bookingDto.setMovieName("Avengers: Endgame");
        bookingDto.setTheatreName("PVR Cinemas");
        bookingDto.setNumberOfTickets(2);
        bookingDto.setSeatNumbers(Arrays.asList("A1", "A2"));

        mockMvc.perform(post("/api/v1.0/moviebooking/{moviename}/add", "Avengers: Endgame")
                .param("theatreName", "PVR Cinemas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.numberOfTickets").value(2))
                .andExpect(jsonPath("$.data.seatNumbers").isArray());
    }

    // US_04: Admin Operations Tests
    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void testUpdateTicketStatus_Admin() throws Exception {
        mockMvc.perform(put("/api/v1.0/moviebooking/Avengers: Endgame/update/SOLD_OUT")
                .param("theatreName", "PVR Cinemas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("SOLD_OUT"));
    }

    @Test
    @WithMockUser(username = "johndoe", roles = "USER")
    void testUpdateTicketStatus_Forbidden() throws Exception {
        registerTestUser();
        
        mockMvc.perform(put("/api/v1.0/moviebooking/Avengers: Endgame/update/SOLD_OUT")
                .param("theatreName", "PVR Cinemas"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void testDeleteMovie_Admin() throws Exception {
        Movie movie = movieRepository.findAll().get(0);
        mockMvc.perform(delete("/api/v1.0/moviebooking/{moviename}/delete/{theatreName}", movie.getMovieName(), movie.getTheatreName()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // Performance Tests
    @Test
    void testApplicationResponseTime() throws Exception {
        long startTime = System.currentTimeMillis();
        
        mockMvc.perform(get("/api/v1.0/moviebooking/all"))
                .andExpect(status().isOk());
                
        long endTime = System.currentTimeMillis();
        long responseTime = endTime - startTime;
        
        // Should respond within 30 seconds as per requirement
        assert responseTime < 30000 : "Response time exceeded 30 seconds: " + responseTime + "ms";
    }

    private void registerTestUser() throws Exception {
        UserRegistrationDto registrationDto = new UserRegistrationDto();
        registrationDto.setFirstName("John");
        registrationDto.setLastName("Doe");
        registrationDto.setEmail("john.doe@example.com");
        registrationDto.setLoginId("johndoe");
        registrationDto.setPassword("password123");
        registrationDto.setConfirmPassword("password123");
        registrationDto.setContactNumber("+919999999999");

        mockMvc.perform(post("/api/v1.0/moviebooking/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationDto)));
    }
}
