package com.campus.Campus_Connect.features.settings.entity;

import com.campus.Campus_Connect.features.auth.entity.User;
import com.campus.Campus_Connect.features.settings.enums.ShowPresence;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_preferences")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPreference {

    @Id
    @Column(name = "user_id")
    private Integer userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "show_phone", nullable = false)
    @Builder.Default
    private Boolean showPhone = false;

    @Column(name = "show_socials", nullable = false)
    @Builder.Default
    private Boolean showSocials = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "show_presence", nullable = false)
    @Builder.Default
    private ShowPresence showPresence = ShowPresence.CONNECTIONS;

    @Column(name = "notify_connections", nullable = false)
    @Builder.Default
    private Boolean notifyConnections = true;

    @Column(name = "notify_events", nullable = false)
    @Builder.Default
    private Boolean notifyEvents = true;

    @Column(name = "notify_posts", nullable = false)
    @Builder.Default
    private Boolean notifyPosts = true;

    @Column(nullable = false)
    @Builder.Default
    private String theme = "system";

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}