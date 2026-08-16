package com.campus.Campus_Connect.features.registration.entity;

import com.campus.Campus_Connect.features.event.entity.Event;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "event_registration_fields")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrationField {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(name = "field_label", nullable = false)
    private String fieldLabel;

    @Column(name = "field_type", nullable = false, length = 30)
    private String fieldType;

    @Column(name = "is_required", nullable = false)
    private Boolean required;

    @Column(length = 255)
    private String placeholder;

    @Column(name = "field_order", nullable = false)
    private Integer fieldOrder;
}