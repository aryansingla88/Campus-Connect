package com.campus.Campus_Connect.features.post.dto.response;

import com.campus.Campus_Connect.features.post.entity.VoteType;
import com.fasterxml.jackson.annotation.JsonProperty;
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

    private boolean canDelete;

    private long commentCount;

    @JsonProperty("isEdited")
    private boolean isEdited;

    private String postType;

    private Integer eventId;

    private Integer eventCategoryId;

    private String eventCategoryName;

    private String eventOrganizerName;

    private String eventVenue;

    private Double eventLatitude;

    private Double eventLongitude;

    private java.time.Instant eventStartTime;
}
