package com.campus.Campus_Connect.features.honor.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "honor_points")
public class HonorPoints {

    @Id
    private Integer userId;

    private Integer points;

    private Instant updatedAt;
}