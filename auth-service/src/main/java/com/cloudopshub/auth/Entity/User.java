package com.cloudopshub.auth.Entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name="users",
        uniqueConstraints ={
                @UniqueConstraint(name = "uk_users_username", columnNames = "username"),
                @UniqueConstraint(name = "uk_users_email", columnNames = "email")
        } )
public class User {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(nullable = false, length = 100)
        private String username;

        @Column(nullable = false, length = 255)
        private String email;

        @Column(nullable = false, length = 255)
        private String password;

        @Column(nullable = false, length = 50)
        private String role;

        @Column(nullable = false)
        private Boolean enabled;

        @Column(name = "created_at", nullable = false, updatable = false)
        private LocalDateTime  createdAt;

        @Column(name = "updated_at", nullable = false)
        private LocalDateTime updatedAt;

        public Long getId() {
                return id;
        }

        public void setId(Long id) {
                this.id = id;
        }

        public String getUsername() {
                return username;
        }

        public void setUsername(String username) {
                this.username = username;
        }

        public String getEmail() {
                return email;
        }

        public void setEmail(String email) {
                this.email = email;
        }

        public String getPassword() {
                return password;
        }

        public void setPassword(String password) {
                this.password = password;
        }

        public String getRole() {
                return role;
        }

        public void setRole(String role) {
                this.role = role;
        }

        public Boolean getEnabled() {
                return enabled;
        }

        public void setEnabled(Boolean enabled) {
                this.enabled = enabled;
        }

        public LocalDateTime getCreatedAt() {
                return createdAt;
        }

        public void setCreatedAt(LocalDateTime createdAt) {
                this.createdAt = createdAt;
        }

        public LocalDateTime getUpdatedAt() {
                return updatedAt;
        }

        public void setUpdatedAt(LocalDateTime updatedAt) {
                this.updatedAt = updatedAt;
        }

        @PrePersist
        protected void onCreate() {
                LocalDateTime now = LocalDateTime.now();
                createdAt = now;
                updatedAt = now;
        }

        @PreUpdate
        protected void onUpdate() {
                updatedAt = LocalDateTime.now();
        }
}
