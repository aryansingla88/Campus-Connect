package com.campus.Campus_Connect.features.navigation.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "destination_nav_mappings")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class DestinationNavMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "entity_type", nullable = false)
    private String entityType;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Column(name = "nav_node_id", nullable = false)
    private Long navNodeId;
}