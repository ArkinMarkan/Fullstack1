package com.moviebookingapp.repository;

import com.moviebookingapp.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User admin;
    private User user;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();

        admin = new User();
        admin.setLoginId("admin1");
        admin.setEmail("admin@example.com");
        admin.setFirstName("Alice");
        admin.setLastName("Admin");
        admin.setContactNumber("1111111111");
        admin.setRole(User.Role.ADMIN);
        admin.setEnabled(true);
        admin.setCreatedAt(LocalDateTime.now().minusDays(10));
        userRepository.save(admin);

        user = new User();
        user.setLoginId("user1");
        user.setEmail("user@example.com");
        user.setFirstName("Bob");
        user.setLastName("User");
        user.setContactNumber("2222222222");
        user.setRole(User.Role.USER);
        user.setEnabled(false);
        user.setCreatedAt(LocalDateTime.now().minusDays(2));
        userRepository.save(user);
    }

    @Test
    @DisplayName("findByLoginId should return user by loginId")
    void findByLoginId() {
        Optional<User> found = userRepository.findByLoginId("admin1");
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("admin@example.com");
    }

    @Test
    @DisplayName("existsByEmail should detect existing email")
    void existsByEmail() {
        assertThat(userRepository.existsByEmail("user@example.com")).isTrue();
        assertThat(userRepository.existsByEmail("missing@example.com")).isFalse();
    }

    @Test
    @DisplayName("findByRole should return users by role")
    void findByRole() {
        List<User> admins = userRepository.findByRole(User.Role.ADMIN);
        assertThat(admins).hasSize(1);
        assertThat(admins.get(0).getLoginId()).isEqualTo("admin1");
    }

    @Test
    @DisplayName("findByFirstNameAndLastNameIgnoreCase should match case-insensitively")
    void findByFirstNameAndLastNameIgnoreCase() {
        List<User> matches = userRepository.findByFirstNameAndLastNameIgnoreCase("alice", "admin");
        assertThat(matches).hasSize(1);
        assertThat(matches.get(0).getLoginId()).isEqualTo("admin1");
    }

    @Test
    @DisplayName("findBySearchTerm should search across fields")
    void findBySearchTerm() {
        List<User> byEmail = userRepository.findBySearchTerm("user@example.com");
        assertThat(byEmail).hasSize(1);
        assertThat(byEmail.get(0).getLoginId()).isEqualTo("user1");

        List<User> byNamePart = userRepository.findBySearchTerm("ali");
        assertThat(byNamePart).hasSize(1);
        assertThat(byNamePart.get(0).getLoginId()).isEqualTo("admin1");
    }

    @Test
    @DisplayName("countByRole should count users per role")
    void countByRole() {
        long adminCount = userRepository.countByRole(User.Role.ADMIN);
        long userCount = userRepository.countByRole(User.Role.USER);
        assertThat(adminCount).isEqualTo(1);
        assertThat(userCount).isEqualTo(1);
    }

    @Test
    @DisplayName("findByRegistrationDateRange should constrain by createdAt")
    void findByRegistrationDateRange() {
        LocalDateTime start = LocalDateTime.now().minusDays(5);
        LocalDateTime end = LocalDateTime.now();
        List<User> recent = userRepository.findByRegistrationDateRange(start, end);
        assertThat(recent).extracting(User::getLoginId).containsExactly("user1");
    }

    @Test
    @DisplayName("findByEnabled should filter by enabled flag")
    void findByEnabled() {
        List<User> enabled = userRepository.findByEnabled(true);
        assertThat(enabled).extracting(User::getLoginId).contains("admin1");
        assertThat(enabled).allMatch(User::isEnabled);
    }
}
