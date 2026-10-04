package com.campus.Campus_Connect.features.registration.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrationParticipantResponse {

    private Integer id;
    private String username;
    private String email;
    private String fullName;
    private String avatarUrl;
    private Integer courseId;
    private Integer admissionYear;
    private String rollNumber;
    private String hostel;
    private String hometown;
    private String gender;
    private String dob;
    private String phone;
    private String github;
    private String linkedin;
}
