package com.campus.Campus_Connect.features.post.dto.response;

import lombok.Builder;
import lombok.Value;
import com.campus.Campus_Connect.features.post.entity.VoteType;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

@Value
@Builder
public class EventPostResponse {
    Integer id;
    Integer postId;
    String title;
    String hostName;
    String organizerName;
    Integer categoryId;
    String categoryName;
    String description;
    String posterUrl;
    Instant startTime;
    String venue;
    Double latitude;
    Double longitude;
    long commentCount;
    @JsonProperty("isEdited")
    boolean isEdited;
    int upvotes;
    int downvotes;
    VoteType userVote;
    boolean canDelete;
}
