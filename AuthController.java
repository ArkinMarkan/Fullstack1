package com.moviebookingapp.controller;

import com.moviebookingapp.dto.ApiResponseDto;
import com.moviebookingapp.dto.JwtResponseDto;
import com.moviebookingapp.dto.LoginRequestDto;
import com.moviebookingapp.dto.UserRegistrationDto;
import com.moviebookingapp.model.User;
import com.moviebookingapp.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.moviebookingapp.dto.ForgotPasswordRequestDto;
import com.moviebookingapp.dto.ResetPasswordRequestDto;

/**
 * Controller for authentication operations
 */
@RestController
@RequestMapping("/api/v1.0/moviebooking")
@Tag(name = "Authentication", description = "User authentication and registration APIs")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    @Autowired
    private UserService userService;
    
    /**
     * Register new user
     * POST /api/v1.0/moviebooking/register
     */
    @PostMapping("/register")
    @Operation(summary = "Register new user", description = "Register a new user in the system")
    public ResponseEntity<ApiResponseDto<User>> registerUser(
            @Valid @RequestBody UserRegistrationDto registrationDto) {
        
        logger.info("Registration request received for user: {}", registrationDto.getLoginId());
        
        User user = userService.registerUser(registrationDto);
        
        ApiResponseDto<User> response = ApiResponseDto.success(
            "User registered successfully", user);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * User login
     * GET /api/v1.0/moviebooking/login
     */
    @GetMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and get JWT token")
    public ResponseEntity<ApiResponseDto<JwtResponseDto>> loginUser(
            @RequestParam String loginId,
            @RequestParam String password) {
        
        logger.info("Login request received for user: {}", loginId);
        
        LoginRequestDto loginRequest = new LoginRequestDto(loginId, password);
        JwtResponseDto jwtResponse = userService.authenticateUser(loginRequest);
        
        ApiResponseDto<JwtResponseDto> response = ApiResponseDto.success(
            "User authenticated successfully", jwtResponse);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Alternative POST login endpoint
     * POST /api/v1.0/moviebooking/login
     */
    @PostMapping("/login")
    @Operation(summary = "User login (POST)", description = "Authenticate user and get JWT token via POST")
    public ResponseEntity<ApiResponseDto<JwtResponseDto>> loginUserPost(
            @Valid @RequestBody LoginRequestDto loginRequest) {
        
        logger.info("POST Login request received for user: {}", loginRequest.getLoginId());
        
        JwtResponseDto jwtResponse = userService.authenticateUser(loginRequest);
        
        ApiResponseDto<JwtResponseDto> response = ApiResponseDto.success(
            "User authenticated successfully", jwtResponse);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Request password reset token via username or email
     * POST /api/v1.0/moviebooking/forgot-password
     */
    @PostMapping("/forgot-password")
    @Operation(summary = "Request password reset", description = "Generate password reset token and send instructions")
    public ResponseEntity<ApiResponseDto<String>> requestPasswordReset(
            @Valid @RequestBody ForgotPasswordRequestDto request) {
        logger.info("Forgot password token request for: {}", request.getUsernameOrEmail());
        String token = userService.createPasswordResetToken(request.getUsernameOrEmail());
        return ResponseEntity.ok(ApiResponseDto.success(
                "Password reset instructions sent to your email",
                token // In production, don't return token; email it instead.
        ));
    }

    /**
     * Reset password using token
     * POST /api/v1.0/moviebooking/reset-password
     */
    @PostMapping("/reset-password")
    @Operation(summary = "Reset password", description = "Reset password using a valid token")
    public ResponseEntity<ApiResponseDto<String>> performPasswordReset(
            @Valid @RequestBody ResetPasswordRequestDto request) {
        if (!request.isPasswordMatching()) {
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Passwords do not match"));
        }
        userService.resetPasswordWithToken(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(ApiResponseDto.success("Password reset successful"));
    }
    
    /**
     * Forgot password
     * GET /api/v1.0/moviebooking/{username}/forgot
     */
    @GetMapping("/{username}/forgot")
    @Operation(summary = "Forgot password", description = "Reset password for a user")
    public ResponseEntity<ApiResponseDto<String>> forgotPassword(
            @PathVariable String username) {
        logger.info("Forgot password request received for user: {}", username);
        String resetToken = userService.createPasswordResetToken(username);
        ApiResponseDto<String> response = ApiResponseDto.success(
            "Password reset instructions sent to your email",
            resetToken // In production, this would be sent via email
        );
        return ResponseEntity.ok(response);
    }
}
