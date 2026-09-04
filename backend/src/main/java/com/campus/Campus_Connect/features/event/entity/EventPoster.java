package com.campus.Campus_Connect.features.event.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "event_posters")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventPoster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(name = "poster_url", nullable = false, length = 500)
    private String posterUrl;

    @Column(name = "uploaded_at", nullable = false)
    private Instant uploadedAt;
}