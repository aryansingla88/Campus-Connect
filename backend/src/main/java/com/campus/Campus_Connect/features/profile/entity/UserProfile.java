package com.campus.Campus_Connect.features.profile.entity;

import com.campus.Campus_Connect.features.auth.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_profile")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfile {

    @Id
    @Column(name = "user_id")
    private Integer userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "full_name")
    private String fullName;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "course_id")
    private Integer courseId;

    private String branch;       // id later (nullable)

    @Column(name = "admission_year")
    private Integer admissionYear;

    @Column(name = "roll_number")
    private String rollNumber;

    private String hostel;

    private String hometown;

    private String gender;

    private LocalDate dob;

    private String phone;

    private String github;

    private String linkedin;

    private String instagram;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}