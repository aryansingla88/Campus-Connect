package com.campus.Campus_Connect.features.auth.entity;

import com.campus.Campus_Connect.features.profile.entity.UserProfile;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @JsonIgnore
    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    private UserProfile profile;

    private String username;

    private String email;

    @Column(name = "password_hash")
    private String passwordHash;

    public enum Role {
        STUDENT,
        ADMIN,
        MODERATOR
    }

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private Role role = Role.STUDENT;

    @Builder.Default
    @Column(name = "is_banned")
    private Boolean isBanned = false;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}