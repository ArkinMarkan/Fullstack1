package com.moviebookingapp.repository;

import com.moviebookingapp.model.PasswordResetToken;
import com.moviebookingapp.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestDatabase;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@EntityScan(basePackages = "com.moviebookingapp.model")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class PasswordResetTokenRepositoryTest {

    @Autowired
    private PasswordResetTokenRepository repository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("findByToken should return existing token")
    void findByToken() {
        // Persist a user to satisfy NOT NULL user_id
        User u = new User();
        u.setLoginId("user_for_token");
        u.setEmail("user.token@example.com");
        u.setFirstName("Reset");
        u.setLastName("User");
        u.setRole(User.Role.USER);
        u.setEnabled(true);
        u = userRepository.save(u); // ensure ID is generated

        PasswordResetToken token = new PasswordResetToken();
        token.setToken("reset-123");
        token.setExpiresAt(LocalDateTime.now().plusHours(1));
        token.setCreatedAt(LocalDateTime.now());
        token.setUsed(false);
        // If entity uses primitive foreign key column, set it directly
        token.setUserId(u.getId());
        repository.save(token);

        Optional<PasswordResetToken> found = repository.findByToken("reset-123");
        assertThat(found).isPresent();
        assertThat(found.get().getUserId()).isEqualTo(u.getId());
    }

    @Test
    @DisplayName("findByToken should return empty for missing token")
    void findByToken_missing() {
        Optional<PasswordResetToken> missing = repository.findByToken("missing");
        assertThat(missing).isNotPresent();
    }
}
