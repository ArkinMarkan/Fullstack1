package com.moviebookingapp.service;

import com.moviebookingapp.dto.UserRegistrationDto;
import com.moviebookingapp.dto.LoginRequestDto;
import com.moviebookingapp.dto.JwtResponseDto;
import com.moviebookingapp.exception.DuplicateResourceException;
import com.moviebookingapp.exception.UserNotFoundException;
import com.moviebookingapp.exception.ValidationException;
import com.moviebookingapp.model.User;
import com.moviebookingapp.model.PasswordResetToken;
import com.moviebookingapp.repository.UserRepository;
import com.moviebookingapp.repository.PasswordResetTokenRepository;
import com.moviebookingapp.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service class for user management operations
 */
@Service
@Transactional
public class UserService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;
    
    /**
     * Register new user
     */
    @CacheEvict(value = "users", allEntries = true)
    public User registerUser(UserRegistrationDto registrationDto) {
        logger.info("Registering new user with login ID: {}", registrationDto.getLoginId());
        
        // Validate password match
        if (!registrationDto.isPasswordMatching()) {
            throw new ValidationException("Password and confirm password do not match");
        }
        
        // Check for existing user by login ID
        if (userRepository.existsByLoginId(registrationDto.getLoginId())) {
            throw new DuplicateResourceException("User with login ID already exists: " + registrationDto.getLoginId());
        }
        
        // Check for existing user by email
        if (userRepository.existsByEmail(registrationDto.getEmail())) {
            throw new DuplicateResourceException("User with email already exists: " + registrationDto.getEmail());
        }
        
        // Create new user
        User user = new User();
        user.setFirstName(registrationDto.getFirstName());
        user.setLastName(registrationDto.getLastName());
        user.setEmail(registrationDto.getEmail());
        user.setLoginId(registrationDto.getLoginId());
        user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
        user.setContactNumber(registrationDto.getContactNumber());
        user.setRole(User.Role.USER);
        
        User savedUser = userRepository.save(user);
        logger.info("User registered successfully: {}", savedUser.getLoginId());
        
        return savedUser;
    }
    
    /**
     * Authenticate user and generate JWT token
     */
    public JwtResponseDto authenticateUser(LoginRequestDto loginRequest) {
        logger.info("Authenticating user: {}", loginRequest.getLoginId());
        
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getLoginId(), 
                    loginRequest.getPassword()
                )
            );
            
            User user = (User) authentication.getPrincipal();
            
            // Generate tokens
            String token = jwtUtil.generateToken(authentication);
            String refreshToken = jwtUtil.generateRefreshToken(user.getLoginId());
            
            logger.info("User authenticated successfully: {}", user.getLoginId());
            
            return new JwtResponseDto(
                token,
                refreshToken,
                user.getLoginId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getRole().name()
            );
            
        } catch (AuthenticationException e) {
            logger.error("Authentication failed for user: {}", loginRequest.getLoginId());
            throw new com.moviebookingapp.exception.AuthenticationException("Invalid login credentials");
        }
    }
    
    /**
     * Find user by login ID
     */
    @Cacheable(value = "users", key = "#loginId")
    public User findByLoginId(String loginId) {
        return userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new UserNotFoundException("User not found with login ID: " + loginId));
    }
    
    /**
     * Find user by email
     */
    @Cacheable(value = "users", key = "#email")
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
    }
    
    /**
     * Find user by ID
     */
    @Cacheable(value = "users", key = "#userId")
    public User findById(String userId) {
        try {
            Long userIdLong = Long.parseLong(userId);
            return userRepository.findById(userIdLong)
                    .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
        } catch (NumberFormatException e) {
            throw new UserNotFoundException("Invalid user ID format: " + userId);
        }
    }
    
    /**
     * Reset user password
     */
    @CacheEvict(value = "users", key = "#loginId")
    public String resetPassword(String loginId) {
        logger.info("Resetting password for user: {}", loginId);
        
        User user = findByLoginId(loginId);
        
        // Generate temporary password
        String tempPassword = generateTemporaryPassword();
        user.setPassword(passwordEncoder.encode(tempPassword));
        
        userRepository.save(user);
        
        logger.info("Password reset successfully for user: {}", loginId);
        
        // In real application, send this via email
        return tempPassword;
    }
    
    /**
     * Update user password
     */
    @CacheEvict(value = "users", key = "#loginId")
    public void updatePassword(String loginId, String currentPassword, String newPassword) {
        logger.info("Updating password for user: {}", loginId);
        
        User user = findByLoginId(loginId);
        
        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new ValidationException("Current password is incorrect");
        }
        
        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        logger.info("Password updated successfully for user: {}", loginId);
    }
    
    /**
     * Get all users (Admin only)
     */
    @Cacheable(value = "users", key = "'all'")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    /**
     * Search users
     */
    public List<User> searchUsers(String searchTerm) {
        return userRepository.findBySearchTerm(searchTerm);
    }
    
    /**
     * Get users by role
     */
    @Cacheable(value = "users", key = "#role")
    public List<User> getUsersByRole(User.Role role) {
        return userRepository.findByRole(role);
    }
    
    /**
     * Update user profile
     */
    @CacheEvict(value = "users", allEntries = true)
    public User updateUserProfile(String loginId, UserRegistrationDto updateDto) {
        logger.info("Updating profile for user: {}", loginId);
        
        User user = findByLoginId(loginId);
        
        // Update allowed fields (username cannot be changed as per requirement)
        user.setFirstName(updateDto.getFirstName());
        user.setLastName(updateDto.getLastName());
        user.setContactNumber(updateDto.getContactNumber());
        
        // Check if email is being changed and if it's already taken
        if (!user.getEmail().equals(updateDto.getEmail()) && 
            userRepository.existsByEmail(updateDto.getEmail())) {
            throw new DuplicateResourceException("Email already exists: " + updateDto.getEmail());
        }
        user.setEmail(updateDto.getEmail());
        
        User savedUser = userRepository.save(user);
        logger.info("Profile updated successfully for user: {}", loginId);
        
        return savedUser;
    }
    
    /**
     * Check if user exists by login ID
     */
    public boolean existsByLoginId(String loginId) {
        return userRepository.existsByLoginId(loginId);
    }
    
    /**
     * Check if user exists by email
     */
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
    
    /**
     * Generate temporary password
     */
    private String generateTemporaryPassword() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    /**
     * Generate and persist a password reset token for the given username/email
     */
    public String createPasswordResetToken(String usernameOrEmail) {
        logger.info("Creating password reset token for: {}", usernameOrEmail);
        User user = findByLoginIdOrEmail(usernameOrEmail);
        String token = UUID.randomUUID().toString();
        PasswordResetToken prt = new PasswordResetToken(token, user, LocalDateTime.now().plusMinutes(15));
        passwordResetTokenRepository.save(prt);
        return token;
    }

    /**
     * Reset password using a valid token
     */
    @CacheEvict(value = "users", allEntries = true)
    public void resetPasswordWithToken(String token, String newPassword) {
        logger.info("Resetting password using token");
        PasswordResetToken prt = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new ValidationException("Invalid or expired token"));
        if (prt.isUsed() || prt.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Invalid or expired token");
        }
        User user = prt.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        prt.setUsed(true);
        passwordResetTokenRepository.save(prt);
        logger.info("Password reset successful for user: {}", user.getLoginId());
    }

    /**
     * Helper: find by loginId or email
     */
    public User findByLoginIdOrEmail(String usernameOrEmail) {
        // Try loginId first, then email
        return userRepository.findByLoginId(usernameOrEmail)
                .orElseGet(() -> userRepository.findByEmail(usernameOrEmail)
                        .orElseThrow(() -> new UserNotFoundException("User not found: " + usernameOrEmail)));
    }
    
    /**
     * Generate password reset token
     */
    public String generatePasswordResetToken(String username) {
        logger.info("Generating password reset token for user: {}", username);
        User user = findByLoginId(username);
        String resetToken = java.util.UUID.randomUUID().toString();
        logger.info("Password reset token generated for user: {}", username);
        return resetToken;
    }
}
