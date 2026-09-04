package com.cloudopshub.auth.Service;

import com.cloudopshub.auth.Entity.RefreshToken;
import com.cloudopshub.auth.Entity.User;
import com.cloudopshub.auth.Repository.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("veera1");
    }

    // ---------- createRefreshToken() ----------

    @Test
    void createRefreshToken_savesTokenWithSevenDayExpiry() {
        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RefreshToken result = refreshTokenService.createRefreshToken(user);

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(captor.capture());

        RefreshToken saved = captor.getValue();
        assertThat(saved.getUser()).isEqualTo(user);
        assertThat(saved.getToken()).isNotBlank();
        assertThat(saved.getExpiresAt()).isAfter(LocalDateTime.now().plusDays(6));
        assertThat(saved.getExpiresAt()).isBefore(LocalDateTime.now().plusDays(8));
        assertThat(result).isSameAs(saved);
    }

    // ---------- isExpired() ----------

    @Test
    void isExpired_pastExpiry_returnsTrue() {
        RefreshToken token = new RefreshToken();
        token.setExpiresAt(LocalDateTime.now().minusMinutes(1));

        assertThat(refreshTokenService.isExpired(token)).isTrue();
    }

    @Test
    void isExpired_futureExpiry_returnsFalse() {
        RefreshToken token = new RefreshToken();
        token.setExpiresAt(LocalDateTime.now().plusDays(1));

        assertThat(refreshTokenService.isExpired(token)).isFalse();
    }

    // ---------- isRevoked() ----------

    @Test
    void isRevoked_reflectsEntityFlag() {
        RefreshToken revoked = new RefreshToken();
        revoked.setRevoked(true);

        RefreshToken active = new RefreshToken();
        active.setRevoked(false);

        assertThat(refreshTokenService.isRevoked(revoked)).isTrue();
        assertThat(refreshTokenService.isRevoked(active)).isFalse();
    }

    // ---------- revokeToken() ----------

    @Test
    void revokeToken_existingToken_marksRevokedAndSaves() {
        RefreshToken token = new RefreshToken();
        token.setToken("existing-token");
        token.setRevoked(false);

        when(refreshTokenRepository.findByToken("existing-token"))
                .thenReturn(Optional.of(token));

        refreshTokenService.revokeToken("existing-token");

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(captor.capture());
        assertThat(captor.getValue().isRevoked()).isTrue();
    }

    @Test
    void revokeToken_unknownToken_doesNothingSilently() {
        when(refreshTokenRepository.findByToken("missing-token"))
                .thenReturn(Optional.empty());

        refreshTokenService.revokeToken("missing-token");

        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
    }

    // ---------- rotateRefreshToken() ----------

    @Test
    void rotateRefreshToken_revokesOldAndIssuesNewTokenForSameUser() {
        RefreshToken oldToken = new RefreshToken();
        oldToken.setToken("old-token-value");
        oldToken.setUser(user);
        oldToken.setRevoked(false);

        when(refreshTokenRepository.findByToken("old-token-value"))
                .thenReturn(Optional.of(oldToken));
        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RefreshToken newToken = refreshTokenService.rotateRefreshToken(oldToken);

        // old token must be revoked
        assertThat(oldToken.isRevoked()).isTrue();

        // a genuinely new token must be issued for the same user
        assertThat(newToken).isNotNull();
        assertThat(newToken.getUser()).isEqualTo(user);
        assertThat(newToken.getToken()).isNotEqualTo("old-token-value");

        verify(refreshTokenRepository, times(2)).save(any(RefreshToken.class)); // one for revoke, one for the new token
    }
}