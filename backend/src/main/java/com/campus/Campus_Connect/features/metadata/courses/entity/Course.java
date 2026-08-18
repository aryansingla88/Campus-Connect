package com.campus.Campus_Connect.features.metadata.courses.entity;

import jakarta.persistence.*;
import lombok.*;

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

    @Column(name = "program", nullable = false)
    private String program;

    @Column(name = "degree", nullable = false)
    private String degree;

    @Column(name = "course_code")
    private String courseCode;

    @Column(name = "degree_level", nullable = false)
    private String degreeLevel;

    @Column(name = "duration_years", nullable = false)
    private Integer durationYears;
}