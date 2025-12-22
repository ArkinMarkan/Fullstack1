package com.moviebookingapp.controller;

import com.moviebookingapp.dto.ApiResponseDto;
import com.moviebookingapp.dto.JwtResponseDto;
import com.moviebookingapp.dto.LoginRequestDto;
import com.moviebookingapp.dto.UserRegistrationDto;
import com.moviebookingapp.model.User;
import com.moviebookingapp.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for AuthController
 */
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(AuthController.class)
class AuthControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private UserService userService;
    
    @MockBean
    private com.moviebookingapp.util.JwtUtil jwtUtil;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private UserRegistrationDto validRegistrationDto;
    private User validUser;
    private JwtResponseDto jwtResponse;
    
    @BeforeEach
    void setUp() {
        validRegistrationDto = new UserRegistrationDto();
        validRegistrationDto.setFirstName("John");
        validRegistrationDto.setLastName("Doe");
        validRegistrationDto.setEmail("john.doe@example.com");
        validRegistrationDto.setLoginId("john_doe");
        validRegistrationDto.setPassword("password123");
        validRegistrationDto.setConfirmPassword("password123");
        validRegistrationDto.setContactNumber("9876543210");
        
        validUser = new User();
        validUser.setId(123L);
        validUser.setFirstName("John");
        validUser.setLastName("Doe");
        validUser.setEmail("john.doe@example.com");
        validUser.setLoginId("john_doe");
        validUser.setRole(User.Role.USER);
        
        jwtResponse = new JwtResponseDto(
            "access-token",
            "refresh-token",
            "john_doe",
            "John",
            "Doe",
            "john.doe@example.com",
            "USER"
        );
    }
    
    @Test
    void registerUser_WithValidData_ShouldReturnSuccess() throws Exception {
        // Arrange
        when(userService.registerUser(any(UserRegistrationDto.class))).thenReturn(validUser);
        
        // Act & Assert
        mockMvc.perform(post("/api/v1.0/moviebooking/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegistrationDto))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User registered successfully"))
                .andExpect(jsonPath("$.data.loginId").value("john_doe"))
                .andExpect(jsonPath("$.data.email").value("john.doe@example.com"));
    }
    
    @Test
    void registerUser_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        // Arrange
        validRegistrationDto.setFirstName(""); // Invalid empty name
        
        // Act & Assert
        mockMvc.perform(post("/api/v1.0/moviebooking/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegistrationDto))
                .with(csrf()))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void loginUser_WithValidCredentials_ShouldReturnJwtToken() throws Exception {
        // Arrange
        when(userService.authenticateUser(any(LoginRequestDto.class))).thenReturn(jwtResponse);
        
        // Act & Assert
        mockMvc.perform(get("/api/v1.0/moviebooking/login")
                .param("loginId", "john_doe")
                .param("password", "password123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User authenticated successfully"))
                .andExpect(jsonPath("$.data.token").value("access-token"))
                .andExpect(jsonPath("$.data.loginId").value("john_doe"));
    }
    
    @Test
    void loginUserPost_WithValidCredentials_ShouldReturnJwtToken() throws Exception {
        // Arrange
        LoginRequestDto loginRequest = new LoginRequestDto("john_doe", "password123");
        when(userService.authenticateUser(any(LoginRequestDto.class))).thenReturn(jwtResponse);
        
        // Act & Assert
        mockMvc.perform(post("/api/v1.0/moviebooking/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").value("access-token"));
    }
    
    @Test
    void forgotPassword_WithValidUsername_ShouldReturnTemporaryPassword() throws Exception {
        // Arrange
        String resetToken = "RESET-TOKEN-1234";
        when(userService.createPasswordResetToken(anyString())).thenReturn(resetToken);
        
        // Act & Assert
        mockMvc.perform(get("/api/v1.0/moviebooking/john_doe/forgot"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(resetToken));
    }
}
