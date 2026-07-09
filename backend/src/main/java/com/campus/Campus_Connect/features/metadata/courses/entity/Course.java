package com.campus.Campus_Connect.features.metadata.courses.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "courses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "degree", nullable = false)
    private String degree;

    @Column(name = "program_name", nullable = false, unique = true)
    private String programName;

    @Column(name = "course_code", unique = true)
    private String courseCode;

    @Column(name = "degree_level", nullable = false)
    private String degreeLevel;

    @Column(name = "duration_years", nullable = false)
    private Integer durationYears;

    @Column(name = "has_branch", nullable = false)
    private Boolean hasBranch;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}