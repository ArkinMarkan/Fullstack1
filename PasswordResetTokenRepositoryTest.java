package com.moviebookingapp.repository;

import com.moviebookingapp.model.PasswordResetToken;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class PasswordResetTokenRepositoryTest {

    @Autowired
    private PasswordResetTokenRepository repository;

    @Test
    @DisplayName("findByToken should return existing token")
    void findByToken() {
        PasswordResetToken token = new PasswordResetToken();
        token.setToken("reset-123");
        token.setUserId(1L);
        token.setExpiresAt(LocalDateTime.now().plusHours(1));
        repository.save(token);

        Optional<PasswordResetToken> found = repository.findByToken("reset-123");
        assertThat(found).isPresent();
        assertThat(found.get().getUserId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("findByToken should return empty for missing token")
    void findByToken_missing() {
        Optional<PasswordResetToken> missing = repository.findByToken("missing");
        assertThat(missing).isNotPresent();
    }
}
