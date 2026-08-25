package com.campus.Campus_Connect.features.registration.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "event_registration_field_options")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrationFieldOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "field_id", nullable = false)
    private RegistrationField field;

    @Column(name = "option_value", nullable = false)
    private String optionValue;

    @Column(name = "option_order", nullable = false)
    private Integer optionOrder;
}