package com.campus.Campus_Connect.features.event.entity;

import com.campus.Campus_Connect.features.auth.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "event_teams")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventTeam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(name = "team_name", nullable = false)
    private String teamName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leader_id", nullable = false)
    private User leader;

    @CreationTimestamp
    private Instant createdAt;

    @OneToMany(mappedBy = "team")
    private List<EventRegistration> registrations;
}