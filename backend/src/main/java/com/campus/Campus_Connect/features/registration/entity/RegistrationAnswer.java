package com.campus.Campus_Connect.features.registration.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "event_registration_answers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrationAnswer {

    @EmbeddedId
    private RegistrationAnswerId id;

    @MapsId("registrationId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registration_id", nullable = false)
    private EventRegistration registration;

    @MapsId("fieldId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "field_id", nullable = false)
    private RegistrationField field;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String answer;
}