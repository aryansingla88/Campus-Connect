package com.campus.Campus_Connect.features.event.entity;

import com.campus.Campus_Connect.features.auth.entity.User;
import com.campus.Campus_Connect.features.event.entity.enums.ApprovalStatus;
import com.campus.Campus_Connect.features.event.entity.enums.EventState;
import com.campus.Campus_Connect.features.event.entity.enums.RegistrationType;
import com.campus.Campus_Connect.features.event.entity.enums.VisibilityType;
import jakarta.persistence.*;
import lombok.*;
import org.antlr.v4.runtime.misc.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.Instant;

import java.time.LocalDateTime;

@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {

    // ------------------------------------------------------------------------
    // Primary Key
    // ------------------------------------------------------------------------

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;


    // ------------------------------------------------------------------------
    // Creator
    // ------------------------------------------------------------------------

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;


    // ------------------------------------------------------------------------
    // Club (Temporary)
    // Replace with @ManyToOne Club once Club entity is created
    // ------------------------------------------------------------------------

    @Column(name = "club_id")
    private Integer clubId;


    // ------------------------------------------------------------------------
    // Basic Details
    // ------------------------------------------------------------------------

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "host_name")
    private String hostName;

    private String venue;


    // ------------------------------------------------------------------------
    // Location
    // ------------------------------------------------------------------------

    @NotNull
    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;


    // ------------------------------------------------------------------------
    // Date & Time
    // ------------------------------------------------------------------------

    @Column(name = "start_time", nullable = false)
    private Instant startTime;

    @Column(name = "end_time")
    private Instant endTime;


    // ------------------------------------------------------------------------
    // Visibility
    // ------------------------------------------------------------------------

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility_type", nullable = false)
    private VisibilityType visibilityType;

    @Column(name = "visibility_value")
    private String visibilityValue;


    // ------------------------------------------------------------------------
    // Registration
    // ------------------------------------------------------------------------

    @Enumerated(EnumType.STRING)
    @Column(name = "registration_type", nullable = false)
    private RegistrationType registrationType;

    @Column(name = "registration_link")
    private String registrationLink;


    // ------------------------------------------------------------------------
    // Status
    // ------------------------------------------------------------------------

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", nullable = false)
    private ApprovalStatus approvalStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_state", nullable = false)
    private EventState eventState;


    // ------------------------------------------------------------------------
    // Priority
    // ------------------------------------------------------------------------

    @Column(name = "base_priority_level", nullable = false)
    @Builder.Default
    private Integer basePriorityLevel = 1;

    @Column(name = "priority_score", nullable = false)
    @Builder.Default
    private Double priorityScore = 0.0;

    @Column(name = "priority_level", nullable = false)
    @Builder.Default
    private Integer priorityLevel = 1;

    // ------------------------------------------------------------------------
    // Audit
    // ------------------------------------------------------------------------

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}



;




