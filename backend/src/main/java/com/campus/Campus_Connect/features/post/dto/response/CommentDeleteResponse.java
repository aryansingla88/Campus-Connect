package com.campus.Campus_Connect.features.post.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentDeleteResponse {

    private Integer commentId;

    private boolean retainedAsTombstone;

    private CommentResponse comment;

    @Builder.Default
    private List<Integer> removedCommentIds = new ArrayList<>();
}
