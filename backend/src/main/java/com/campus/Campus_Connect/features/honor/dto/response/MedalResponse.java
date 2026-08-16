package com.campus.Campus_Connect.features.honor.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedalResponse {

    private String title;

    private Boolean awarded;

    private MedalRecipientResponse recipient;

}