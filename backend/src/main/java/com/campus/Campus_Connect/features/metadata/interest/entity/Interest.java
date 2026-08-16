package com.campus.Campus_Connect.features.metadata.interest.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "interests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Interest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String label;

    @Column(nullable = false)
    private String category;
}