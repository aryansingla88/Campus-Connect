package com.campus.Campus_Connect.features.profile.dto.response;

import com.campus.Campus_Connect.features.metadata.courses.dto.CourseResponse;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileResponse {

    // User
    private Integer userId;
    private String username;
    private String email;

    // User Profile
    private String fullName;
    private String bio;
    private String avatarUrl;

    private CourseResponse course;

    private Integer admissionYear;
    private String hostel;
    private String hometown;
    private String gender;
    private String dob;
    private String phone;

    private String github;
    private String linkedin;
    private String instagram;

    private String memberSince;

    // Preferences
    private Boolean showPhone;
    private Boolean showSocials;
}