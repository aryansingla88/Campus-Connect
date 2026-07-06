package com.campus.Campus_Connect.features.map.entity;

import com.campus.Campus_Connect.features.auth.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_presence")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPresence {

    @Id
    @Column(name = "user_id")
    private Integer userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @Column(name = "longitude", nullable = false)
    private Double longitude;

    @UpdateTimestamp
    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;
}