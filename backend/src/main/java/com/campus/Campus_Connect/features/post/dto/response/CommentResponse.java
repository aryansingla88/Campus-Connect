package com.campus.Campus_Connect.features.post.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentResponse {

    private Integer id;

    private Integer postId;

    private Integer parentCommentId;

    private String username;

    private String body;

    private LocalDateTime createdAt;

    @Builder.Default
    private List<CommentResponse> replies = new ArrayList<>();

}