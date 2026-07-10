package com.campus.Campus_Connect.features.honor.dto.request;

import com.campus.Campus_Connect.features.honor.enums.MedalType;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AwardMedalRequest {

    @NotNull
    private Integer registrationId;

    @NotNull
    private MedalType medalType;

}