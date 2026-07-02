package com.campus.Campus_Connect.features.auth.dto.request;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {

    private String username;
    private String email;
    private String password;

    private String fullName;

    private Integer courseId;
    private String branch;     // id later (nullable)

    private Integer admissionYear;

    private String gender;
    private LocalDate dob;

    private String rollNumber;
}