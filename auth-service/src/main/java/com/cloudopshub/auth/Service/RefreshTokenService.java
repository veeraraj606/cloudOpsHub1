package com.cloudopshub.auth.Service;

import com.cloudopshub.auth.Entity.RefreshToken;
import com.cloudopshub.auth.Entity.User;
import com.cloudopshub.auth.Repository.RefreshTokenRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public RefreshToken createRefreshToken(User user){
        RefreshToken refreshToken  = new RefreshToken();

        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setUser(user);
        refreshToken.setCreated_at(LocalDateTime.now());
        refreshToken.setExpiresAt(LocalDateTime.now().plusDays(7));

        return refreshTokenRepository.save(refreshToken);

    }

    public Optional<RefreshToken> findByToken(String token){
        return refreshTokenRepository.findValidToken(token);
    }

    public boolean isExpired(RefreshToken refreshToken){
        return refreshToken.getExpiresAt().isBefore(LocalDateTime.now());
    }

    public boolean isRevoked(RefreshToken refreshToken){
        return refreshToken.isRevoked();
    }

    public  void revokeToken(String token){

        refreshTokenRepository.findByToken(token)
                .ifPresent(refreshToken -> {
                    refreshToken.setRevoked(true);
                    refreshTokenRepository.save(refreshToken);
                });
    }
    public RefreshToken rotateRefreshToken(RefreshToken oldToken) {
        revokeToken(oldToken.getToken());
        return createRefreshToken(oldToken.getUser());
    }
}
