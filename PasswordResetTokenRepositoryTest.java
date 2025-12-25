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
        // Persist a user to satisfy FK/not-null user_id
        User u = new User();
        u.setLoginId("user_for_token");
        u.setEmail("user.token@example.com");
        u.setFirstName("Reset");
        u.setLastName("User");
        u.setRole(User.Role.USER);
        u.setEnabled(true);
        userRepository.save(u);

        PasswordResetToken token = new PasswordResetToken();
        token.setToken("reset-123");
        token.setExpiresAt(LocalDateTime.now().plusHours(1));
        token.setCreatedAt(LocalDateTime.now());
        token.setUsed(false);
        // Associate user (ensures non-null user_id)
        // If entity has a 'user' relation, set it; otherwise ensure correct setter exists.
        token.setUser(u);
        repository.save(token);

        Optional<PasswordResetToken> found = repository.findByToken("reset-123");
        assertThat(found).isPresent();
        assertThat(found.get().getUser().getLoginId()).isEqualTo("user_for_token");
    }

    @Test
    @DisplayName("findByToken should return empty for missing token")
    void findByToken_missing() {
        Optional<PasswordResetToken> missing = repository.findByToken("missing");
        assertThat(missing).isNotPresent();
    }
}
