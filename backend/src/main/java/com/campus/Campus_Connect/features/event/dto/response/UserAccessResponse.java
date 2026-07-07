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

    private String subtitle;

    private EventMemberRole role;

}
