package com.campus.Campus_Connect.features.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class VerifyPasswordResetOtpResponse {

    private String resetToken;
}