package com.campus.Campus_Connect.features.event.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "event_categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String name;
}