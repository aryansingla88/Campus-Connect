package com.campus.Campus_Connect.features.registration.entity;

import com.campus.Campus_Connect.features.event.entity.Event;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "event_registration_config")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrationConfig {

    @Id
    @Column(name = "event_id")
    private Integer eventId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "event_id")
    private Event event;

    @Column(name = "min_team_members", nullable = false)
    private Integer minTeamMembers;

    @Column(name = "max_team_members", nullable = false)
    private Integer maxTeamMembers;

    @Column(name = "quick_field_mask", nullable = false)
    private Long quickFieldMask;

    @Column(name = "registration_created_at")
    private Instant registrationCreatedAt;

    @Column(name = "registration_end_at")
    private Instant registrationEndAt;
}
