package com.cloudopshub.auth.Repository;

import com.cloudopshub.auth.Entity.RefreshToken;
import com.cloudopshub.auth.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Repository;

import java.sql.Ref;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken,Long> {

   Optional<RefreshToken> findByToken(String token);

    void deleteByUser(User user);

    @Query(value = """
        SELECT *
        FROM refresh_tokens
        WHERE token = :token
          AND revoked = false
        """,
            nativeQuery = true)
    Optional<RefreshToken> findValidToken(@Param("token") String token);
}
