package com.campus.Campus_Connect.features.auth.dto.response;

import com.campus.Campus_Connect.features.auth.entity.User;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private Integer id;
    private String username;
    private String email;
    private User.Role role;
}