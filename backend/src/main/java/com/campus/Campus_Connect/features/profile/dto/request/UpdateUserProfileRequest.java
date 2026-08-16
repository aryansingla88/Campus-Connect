package com.campus.Campus_Connect.features.profile.dto.request;

import lombok.*;
import java.time.LocalDate;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserProfileRequest {

    private String fullName;

    private String bio;

    private String avatarUrl;

    private Integer courseId;

    private Integer admissionYear;

    private String hostel;

    private String hometown;

    private String gender;

    private LocalDate dob;

    private String phone;

    private String github;

    private String linkedin;

    private String instagram;
}