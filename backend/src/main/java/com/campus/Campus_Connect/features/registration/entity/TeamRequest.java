package com.campus.Campus_Connect.features.registration.entity;

import com.campus.Campus_Connect.features.event.entity.EventTeam;
import com.campus.Campus_Connect.features.registration.entity.enums.TeamRequestInitiator;
import com.campus.Campus_Connect.features.registration.entity.enums.TeamRequestStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "event_team_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private EventTeam team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registration_id", nullable = false)
    private EventRegistration registration;

    @Enumerated(EnumType.STRING)
    @Column(name = "initiated_by", nullable = false, length = 20)
    private TeamRequestInitiator initiatedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TeamRequestStatus status = TeamRequestStatus.PENDING;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
