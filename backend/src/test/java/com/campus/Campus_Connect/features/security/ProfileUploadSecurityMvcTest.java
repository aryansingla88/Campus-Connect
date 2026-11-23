package com.campus.Campus_Connect.features.security;

import com.campus.Campus_Connect.config.SecurityConfig;
import com.campus.Campus_Connect.config.WebConfig;
import com.campus.Campus_Connect.features.auth.repository.UserRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ProfileUploadSecurityMvcTest.SecurityProbeController.class)
@Import({SecurityConfig.class, WebConfig.class, JwtAuthenticationFilter.class})
@TestPropertySource(properties = "app.storage.local.root=target/profile-upload-security-test")
class ProfileUploadSecurityMvcTest {

    private static final byte[] IMAGE_BYTES = new byte[]{1, 2, 3, 4};
    private static final Path IMAGE_PATH = Paths.get(
            "target", "profile-upload-security-test", "profiles", "security-test.jpg"
    );

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserRepository userRepository;

    @BeforeAll
    static void createProfileImage() throws IOException {
        Files.createDirectories(IMAGE_PATH.getParent());
        Files.write(IMAGE_PATH, IMAGE_BYTES);
    }

    @AfterAll
    static void removeProfileImage() throws IOException {
        Files.deleteIfExists(IMAGE_PATH);
        Files.deleteIfExists(IMAGE_PATH.getParent());
        Files.deleteIfExists(IMAGE_PATH.getParent().getParent());
    }

    @Test
    void profileImageIsServedWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/uploads/profiles/security-test.jpg"))
                .andExpect(status().isOk())
                .andExpect(content().bytes(IMAGE_BYTES));
    }

    @Test
    void profileApiStillRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginRouteIsPublicAndJwtAuthenticatesProtectedProfileRoute() throws Exception {
        mockMvc.perform(post("/auth/login"))
                .andExpect(status().isOk());

        var user = com.campus.Campus_Connect.features.auth.entity.User.builder()
                .id(42)
                .role(com.campus.Campus_Connect.features.auth.entity.User.Role.STUDENT)
                .isBanned(false)
                .build();
        when(jwtService.extractUserId("test-token")).thenReturn(42);
        when(userRepository.findById(42)).thenReturn(Optional.of(user));

        mockMvc.perform(get("/users/me").header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(content().string("protected"));
    }

    @RestController
    static class SecurityProbeController {
        @org.springframework.web.bind.annotation.PostMapping("/auth/login")
        String login() {
            return "login";
        }

        @GetMapping("/users/me")
        String protectedProfile() {
            return "protected";
        }
    }
}
