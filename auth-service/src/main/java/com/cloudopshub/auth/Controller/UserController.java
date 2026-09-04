package com.cloudopshub.auth.Controller;

import com.cloudopshub.auth.Dto.*;
import com.cloudopshub.auth.Entity.User;
import com.cloudopshub.auth.Repository.UserRepository;
import com.cloudopshub.auth.exception.InvalidCredentialsException;
import com.cloudopshub.auth.exception.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.hibernate.Hibernate.map;

@RestController
@RequestMapping("/api/users")
@SecurityRequirement(name="bearerAuth")
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/me")
    public UserProfileResponse getCurrentUser(Authentication authentication){
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return new UserProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole()
        );
    }

    @GetMapping("/admin/dashboard")
    @PreAuthorize("hasRole('Admin')")
    public String adminOnly(){
        return "Welcome to Admin Dashboard!";
    }

    @PutMapping("/me")
    public UserProfileResponse updateProfile(
        Authentication authentication,@Valid @RequestBody UpdateProfileRequest request){
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        User updatedUser= userRepository.save(user);

        return new UserProfileResponse(
                updatedUser.getId(),
                updatedUser.getUsername(),
                updatedUser.getEmail(),
                updatedUser.getRole()
        );
    }
    @PutMapping("/password")
    public ResponseEntity<String> changePassword(Authentication authentication,
   @Valid @RequestBody ChangePasswordRequest request){
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("user not found"));

        if(!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        return ResponseEntity.ok("password changed successfully");

    }

    @GetMapping
    @PreAuthorize("hasRole('Admin')")
    public Page<UserResponse> getAllUsers(Pageable pageable){

        Page<User> users = userRepository.findAll(pageable);

        return users.map(user -> new UserResponse(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getRole(),
                        user.getEnabled(),
                        user.getCreatedAt(),
                        user.getUpdatedAt()
                ));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('Admin')")
    public UserResponse updateUserStatus(@PathVariable Long id,
            @Valid @RequestBody UpdateUserStatusRequest request){
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setEnabled(request.getEnabled());

        User updatedUser = userRepository.save(user);
        return new UserResponse(
                updatedUser.getId(),
                updatedUser.getUsername(),
                updatedUser.getEmail(),
                updatedUser.getRole(),
                updatedUser.getEnabled(),
                updatedUser.getCreatedAt(),
                updatedUser.getUpdatedAt()
        );
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('Admin')")
    public List<UserResponse> searchUser(@RequestParam String keyword){

        List<User> users = userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(keyword, keyword);
        return users.stream()
                .map(user -> new UserResponse(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getRole(),
                        user.getEnabled(),
                        user.getCreatedAt(),
                        user.getUpdatedAt()

                )).toList();
    }
}
