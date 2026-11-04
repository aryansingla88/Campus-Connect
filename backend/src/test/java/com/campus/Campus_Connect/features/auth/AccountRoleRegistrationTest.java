package com.campus.Campus_Connect.features.auth;

import com.campus.Campus_Connect.features.auth.dto.request.RegisterRequest;
import com.campus.Campus_Connect.features.auth.entity.User;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AccountRoleRegistrationTest {
    @Test
    void registrationIgnoresClientSuppliedRoleAndDefaultsToStudent() throws Exception {
        ObjectMapper mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        RegisterRequest request = mapper.readValue("""
                {"username":"new-user","email":"new@example.test","password":"secret",
                 "courseId":1,"fullName":"New User","admissionYear":2026,
                 "role":"DEV"}
                """, RegisterRequest.class);

        User created = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash("encoded")
                .build();

        assertEquals(User.Role.STUDENT, created.getRole());
    }

    @Test
    void legacyAccountRoleStringsRemainDeserializableForManualReview() {
        assertEquals(User.Role.ADMIN, User.Role.valueOf("ADMIN"));
        assertEquals(User.Role.MODERATOR, User.Role.valueOf("MODERATOR"));
    }
}
