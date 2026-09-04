package com.cloudopshub.auth.Repository;

import com.cloudopshub.auth.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String user);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    List<User> findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(String username, String email);

    List<User> findByEmailContainingIgnoreCase(String email);

}
