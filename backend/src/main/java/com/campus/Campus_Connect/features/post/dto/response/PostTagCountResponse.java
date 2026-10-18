package com.campus.Campus_Connect.features.post.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostTagCountResponse {

    private Integer id;

    private String name;

    private Long postCount;
}
