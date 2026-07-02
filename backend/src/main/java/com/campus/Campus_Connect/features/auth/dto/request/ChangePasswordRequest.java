package com.campus.Campus_Connect.features.auth.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChangePasswordRequest {

    private String currentPassword;
    private String newPassword;
}
