package com.campus.Campus_Connect.features.auth.dto.response;

import com.campus.Campus_Connect.features.auth.entity.User;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token;

    private User.Role role;

    @Builder.Default
    private String tokenType = "Bearer";
}
