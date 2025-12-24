package com.moviebookingapp.controller;

import com.moviebookingapp.dto.JwtResponseDto;
import com.moviebookingapp.dto.LoginRequestDto;
import com.moviebookingapp.dto.UserRegistrationDto;
import com.moviebookingapp.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    @DisplayName("POST /register returns 200 and user payload")
    void registerUser_success() throws Exception {
        Mockito.when(userService.registerUser(Mockito.any(UserRegistrationDto.class)))
                .thenAnswer(inv -> {
                    com.moviebookingapp.model.User u = new com.moviebookingapp.model.User();
                    u.setLoginId("john");
                    return u;
                });

        String body = "{\"loginId\":\"john\",\"password\":\"Pass@123\",\"email\":\"john@example.com\"}";
        mockMvc.perform(post("/api/v1.0/moviebooking/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.loginId").value("john"));
    }

    @Test
    @DisplayName("GET /login authenticates and returns JWT")
    void loginUser_get_success() throws Exception {
        Mockito.when(userService.authenticateUser(Mockito.any(LoginRequestDto.class)))
                .thenReturn(new JwtResponseDto("token", "john", "USER"));

        mockMvc.perform(get("/api/v1.0/moviebooking/login")
                        .param("loginId", "john")
                        .param("password", "Pass@123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").value("token"))
                .andExpect(jsonPath("$.data.loginId").value("john"));
    }

    @Test
    @DisplayName("POST /login authenticates and returns JWT")
    void loginUser_post_success() throws Exception {
        Mockito.when(userService.authenticateUser(Mockito.any(LoginRequestDto.class)))
                .thenReturn(new JwtResponseDto("token", "john", "USER"));

        String body = "{\"loginId\":\"john\",\"password\":\"Pass@123\"}";
        mockMvc.perform(post("/api/v1.0/moviebooking/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").value("token"));
    }

    @Test
    @DisplayName("POST /forgot-password returns token in success wrapper")
    void requestPasswordReset_success() throws Exception {
        Mockito.when(userService.createPasswordResetToken(Mockito.anyString()))
                .thenReturn("reset-token");

        String body = "{\"usernameOrEmail\":\"john\"}";
        mockMvc.perform(post("/api/v1.0/moviebooking/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("reset-token"));
    }

    @Test
    @DisplayName("POST /reset-password validates and returns 200")
    void performPasswordReset_success() throws Exception {
        String body = "{\"token\":\"abc\",\"newPassword\":\"New@123\",\"confirmPassword\":\"New@123\"}";
        mockMvc.perform(post("/api/v1.0/moviebooking/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /reset-password bad request when passwords mismatch")
    void performPasswordReset_passwordMismatch() throws Exception {
        String body = "{\"token\":\"abc\",\"newPassword\":\"New@123\",\"confirmPassword\":\"Mismatch\"}";
        mockMvc.perform(post("/api/v1.0/moviebooking/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}
