package com.campus.Campus_Connect.features.honor.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedalsResponse {

    private MedalResponse gold;

    private MedalResponse silver;

    private MedalResponse bronze;

    private Integer awardedCount;

}