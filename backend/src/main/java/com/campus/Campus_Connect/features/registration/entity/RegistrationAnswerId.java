package com.campus.Campus_Connect.features.registration.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class RegistrationAnswerId implements Serializable {

    @Column(name = "registration_id")
    private Integer registrationId;

    @Column(name = "field_id")
    private Integer fieldId;
}