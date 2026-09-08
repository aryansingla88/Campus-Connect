package com.campus.Campus_Connect.features.profile.dto.request;

import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserProfileRequest {

    private String bio;

    private String avatarUrl;

    private String hostel;

    private String hometown;

    private String phone;

    private String github;

    private String linkedin;

    private String instagram;
}