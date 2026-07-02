package com.campus.Campus_Connect.features.auth.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest {

    private String identifier;                           //email and username both
    private String password;
}


