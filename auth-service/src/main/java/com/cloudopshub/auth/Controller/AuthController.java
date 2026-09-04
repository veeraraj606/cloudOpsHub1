package com.cloudopshub.auth.Controller;

import com.cloudopshub.auth.Dto.*;
import com.cloudopshub.auth.Entity.RefreshToken;
import com.cloudopshub.auth.Entity.User;
import com.cloudopshub.auth.Repository.RefreshTokenRepository;
import com.cloudopshub.auth.Security.JwtService;
import com.cloudopshub.auth.Service.AuthService;
import com.cloudopshub.auth.Service.RefreshTokenService;
import com.cloudopshub.auth.exception.InvalidCredentialsException;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    public AuthController(AuthService authService, RefreshTokenService refreshTokenService, JwtService jwtService, RefreshTokenRepository refreshTokenRepository) {
        this.authService = authService;
        this.refreshTokenService = refreshTokenService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request){
        UserResponse response = authService.register(request);
        return  ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {

        LoginResponse response = authService.login(request);

        logger.info("Login successful for user: {}", response.getUsername());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refreshToken(@RequestBody RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenService.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid refresh token"));

        if (refreshTokenService.isExpired(refreshToken)) {
            throw new InvalidCredentialsException("Refresh token expired");
        }

        RefreshToken newRefreshToken = refreshTokenService.rotateRefreshToken(refreshToken);
        String newAccessToken = jwtService.generateToken(
                refreshToken.getUser().getUsername(), refreshToken.getUser().getRole()
        );

        LoginResponse response = new LoginResponse(
                newAccessToken,
                refreshToken.getUser().getUsername(),
                newRefreshToken.getToken()
        );
        return ResponseEntity.ok(response);
    }
  /*  @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest request) {
        Optional<RefreshToken> token = Optional.of(new RefreshToken());
        token = refreshTokenService.findByToken(request.getRefreshToken());
        return ResponseEntity.ok(token);
    }*/
  @PostMapping("/logout")
  public ResponseEntity<Map<String, String>> logout(@RequestBody LogoutRequest request) {
      refreshTokenService.revokeToken(request.getRefreshToken());
      return ResponseEntity.ok(Map.of("message", "Logout successful"));
  }
}
