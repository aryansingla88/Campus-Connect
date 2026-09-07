package com.campus.Campus_Connect.features.metadata.clubs.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClubMetadataResponse {

    private Integer clubId;
    private String name;
}
