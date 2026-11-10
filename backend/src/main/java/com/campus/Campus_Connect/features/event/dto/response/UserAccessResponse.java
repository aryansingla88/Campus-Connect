package com.campus.Campus_Connect.features.event.dto.response;

import com.campus.Campus_Connect.features.event.entity.enums.EventMemberRole;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAccessResponse {

    private Integer id;

    private String name;

    private Integer courseId;

    private Integer admissionYear;

    private String avatarUrl;

    private EventMemberRole role;

    private Boolean hasAccess;
}
