package com.campus.Campus_Connect.features.post.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "posts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "creator_id", nullable = false)
    private Integer creatorId;

    @Column(name = "post_type", nullable = false)
    private String postType;

    @Column(name = "event_id")
    private Integer eventId;

    @Column(name = "club_id")
    private Integer clubId;

    @Column(name = "poi_id")
    private Integer poiId;

    @Column(nullable = false)
    private String title;

    @Column(name = "content_raw", nullable = false, columnDefinition = "TEXT")
    private String contentRaw;

    @Column(name = "content_rendered", columnDefinition = "TEXT")
    private String contentRendered;

    @Column(name = "visibility_type")
    private String visibilityType;

    @Column(name = "visibility_value")
    private String visibilityValue;

    @Column(name = "allow_comments", nullable = false)
    private Boolean allowComments;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<PostImage> images = new HashSet<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Comment> comments = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "post_tag_map",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @Builder.Default
    private Set<PostTag> tags = new HashSet<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<PostVote> votes = new HashSet<>();
}