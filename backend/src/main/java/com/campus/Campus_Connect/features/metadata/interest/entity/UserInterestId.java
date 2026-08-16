package com.campus.Campus_Connect.features.metadata.interest.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class UserInterestId implements Serializable {

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "interest_id")
    private Integer interestId;
}