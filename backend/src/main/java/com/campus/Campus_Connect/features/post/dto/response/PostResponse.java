package com.campus.Campus_Connect.features.post.dto.response;

import com.campus.Campus_Connect.features.post.entity.VoteType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostResponse {

    private Integer id;

    private String username;

    private String title;

    private String body;

    private List<PostTagResponse> tags;

    private String imageUrl;

    private Integer upvotes;

    private Integer downvotes;

    private VoteType userVote;

    private LocalDateTime createdAt;
}