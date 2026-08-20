package com.campus.Campus_Connect.features.navigation.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "nav_edges")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class NavEdge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "from_node_id", nullable = false)
    private Long fromNodeId;

    @Column(name = "to_node_id", nullable = false)
    private Long toNodeId;

    @Column(name = "distance_meters", nullable = false)
    private Double distanceMeters;

    @Column(name = "is_two_way")
    private Boolean isTwoWay = true;
}