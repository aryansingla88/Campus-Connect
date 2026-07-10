package com.campus.Campus_Connect.features.metadata.interest;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterestResponse {
    private Integer interestId;

    private String label;

    private String category;
}
