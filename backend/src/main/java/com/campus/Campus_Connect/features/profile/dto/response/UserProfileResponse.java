package com.campus.Campus_Connect.features.profile.dto.response;

import com.campus.Campus_Connect.features.connection.dto.ConnectionRelationshipStatus;
import com.campus.Campus_Connect.features.metadata.courses.dto.CourseResponse;
import com.campus.Campus_Connect.features.honor.dto.response.HonorResponse;
import lombok.*;

import java.util.List;

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

    // User Profile --Header
    private String fullName;
    private String bio;
    private String avatarUrl;

    // User Profile --course
    private Integer courseId;
    private Integer admissionYear;

    //User Profile --others
    private Integer hostelId;
    private String hometown;
    private String gender;
    private String dob;
    private Integer age;
    private String phone;

    private String github;
    private String linkedin;
    private String instagram;

    private String memberSince;
    private ConnectionRelationshipStatus relationshipStatus;

    // Preferences
    private Boolean showPhone;
    private Boolean showSocials;
    private Boolean showHometown;
    private Boolean showGender;
    private Boolean showAge;
    private Boolean showHonor;

    private List<HonorResponse> headerBadges;

    private List<HonorResponse> headerMedals;
}
