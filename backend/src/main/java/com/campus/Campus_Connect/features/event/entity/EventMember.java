package com.campus.Campus_Connect.features.event.entity;

import com.campus.Campus_Connect.features.auth.entity.User;
import com.campus.Campus_Connect.features.event.entity.enums.EventMemberRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "event_members")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventMember {

    @EmbeddedId
    private EventMemberId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("eventId")
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventMemberRole role;

//    @ManyToOne(fetch = FetchType.LAZY)             -- later after teams
//    @JoinColumn(name = "team_id")
//    private EventTeam team;

    @Column(name = "team_id")
    private Integer teamId;

    @Column(name = "joined_at", nullable = false, updatable = false)
    private LocalDateTime joinedAt;
}