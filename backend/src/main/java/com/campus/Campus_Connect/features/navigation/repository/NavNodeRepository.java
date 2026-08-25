package com.campus.Campus_Connect.features.navigation.repository;

import com.campus.Campus_Connect.features.navigation.entity.NavNode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NavNodeRepository extends JpaRepository<NavNode, Long> {

    @Query(value = "SELECT * FROM nav_nodes " +
            "ORDER BY location <-> ST_SetSRID(ST_MakePoint(:lng, :lat), 4326) " +
            "LIMIT 1", nativeQuery = true)
    Optional<NavNode> findNearestNode(@Param("lat") double lat, @Param("lng") double lng);
}