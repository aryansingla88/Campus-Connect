package com.campus.Campus_Connect.features.honor.repository;

import com.campus.Campus_Connect.features.honor.entity.HonorPoints;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HonorPointsRepository
        extends JpaRepository<HonorPoints, Integer> {

    @Query("""
        SELECT COUNT(h)
        FROM HonorPoints h
        WHERE h.points > (
            SELECT hp.points
            FROM HonorPoints hp
            WHERE hp.userId = :userId
        )
        """)
    long countUsersWithMorePoints(
            @Param("userId") Integer userId
    );
}