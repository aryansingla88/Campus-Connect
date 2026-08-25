package com.campus.Campus_Connect.features.navigation.entity;

import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Point;
import java.time.LocalDateTime;

@Entity
@Table(name = "nav_nodes")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class NavNode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(columnDefinition = "GEOGRAPHY(POINT, 4326)", nullable = false)
    private Point location;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}