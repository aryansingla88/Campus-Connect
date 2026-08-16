package com.campus.Campus_Connect.features.honor.entity;

import com.campus.Campus_Connect.features.event.entity.Event;
import com.campus.Campus_Connect.features.honor.enums.HonorType;
import com.campus.Campus_Connect.features.honor.enums.StatisticType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "honor_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class HonorItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HonorType type;

    @Column(nullable = false)
    private String title;

    private String subtitle;

    private String iconUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event;

    @Enumerated(EnumType.STRING)
    private StatisticType statisticType;

    private Integer threshold;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

}
