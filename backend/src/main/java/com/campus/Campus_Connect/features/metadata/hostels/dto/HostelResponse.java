package com.campus.Campus_Connect.features.metadata.hostels.dto;

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
public class HostelResponse {
    private Integer id;
    private String hostel;
    private String name;
    private boolean active;
}
