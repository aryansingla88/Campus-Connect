package com.campus.Campus_Connect.features.honor.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedalRecipientResponse {

    private Integer honorId;

    private String name;

    private String subtitle;

    private Boolean team;

}