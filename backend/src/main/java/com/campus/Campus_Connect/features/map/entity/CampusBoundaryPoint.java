package com.campus.Campus_Connect.features.map.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.*;

@Entity
@Table(name = "campus_boundary_points")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CampusBoundaryPoint {

    @Id
    @Column(name = "point_order")
    private Integer pointOrder;

    @Column(name = "latitude", nullable = false, precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(name = "longitude", nullable =false, precision =10, scale =7)
    private BigDecimal longitude;
}