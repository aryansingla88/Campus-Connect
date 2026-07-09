package com.campus.Campus_Connect.features.post.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "post_votes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostVote {

    @EmbeddedId
    private PostVoteId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("postId")
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(name = "vote_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private VoteType voteType;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}