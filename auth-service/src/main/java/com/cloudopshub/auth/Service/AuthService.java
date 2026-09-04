package com.cloudopshub.auth.Service;

import com.cloudopshub.auth.Dto.LoginRequest;
import com.cloudopshub.auth.Dto.LoginResponse;
import com.cloudopshub.auth.Dto.RegisterRequest;
import com.cloudopshub.auth.Dto.UserResponse;
import com.cloudopshub.auth.Entity.RefreshToken;
import com.cloudopshub.auth.Entity.User;
import com.cloudopshub.auth.Repository.RefreshTokenRepository;
import com.cloudopshub.auth.Repository.UserRepository;
import com.cloudopshub.auth.Security.JwtService;
import com.cloudopshub.auth.exception.DuplicateResourceException;
import com.cloudopshub.auth.exception.InvalidCredentialsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;

    private final RefreshTokenService refreshTokenService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder, JwtService jwtService, RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    public UserResponse register(RegisterRequest request){
        if(userRepository.existsByUsername(request.getUsername())){
            throw new DuplicateResourceException("Username already exists");
        }
        if(userRepository.existsByEmail(request.getEmail())){
            throw new DuplicateResourceException("Email already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("User");
        user.setEnabled(true);
        User savedUser  = userRepository.save(user);


        return new UserResponse(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail(),
                savedUser.getRole(),
                savedUser.getEnabled(),
                savedUser.getCreatedAt(),
                savedUser.getUpdatedAt()

        );
    }

    public LoginResponse login(LoginRequest request){

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(()->new InvalidCredentialsException("Invalid username or password"));

        if(!user.getEnabled()){
            throw new InvalidCredentialsException("user account is disabled");
        }

        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())){
            throw new InvalidCredentialsException("Invalid username or password");
        }
        String token = jwtService.generateToken(user.getUsername(), user.getRole());

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);
        return new LoginResponse(token, user.getUsername(), refreshToken.getToken());
    }
}
