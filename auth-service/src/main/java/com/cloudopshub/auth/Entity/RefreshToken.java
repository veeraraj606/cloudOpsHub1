package com.cloudopshub.auth.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_tokens")
@ToString
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(nullable=false, unique = true, length = 500)
    private String token;
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime created_at;
    @Column(name = "revoked", nullable = false)
    private boolean revoked = false;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public LocalDateTime getCreated_at() {
        return created_at;
    }

    public void setCreated_at(LocalDateTime created_at) {
        this.created_at = created_at;
    }

    public boolean isRevoked() {
        return revoked;
    }

    public void setRevoked(boolean revoked) {
        this.revoked = revoked;
    }

    /*@PostLoad
    private void checkRevokedAfterLoad(){
        System.out.println("*******RefreshToken*******");
        System.out.println("ID" + id);
        System.out.println("Token" + token);
        System.out.println("Revoked" + revoked);
    }*/
}
