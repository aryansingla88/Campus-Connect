package com.campus.Campus_Connect.features.honor.entity;

import com.campus.Campus_Connect.features.auth.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "user_honor")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserHonor {

    @EmbeddedId
    private UserHonorId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("honorId")
    @JoinColumn(name = "honor_id")
    private HonorItem honor;

    private Integer priority;

    private Instant awardedAt;

}
