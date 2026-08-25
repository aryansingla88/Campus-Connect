package com.campus.Campus_Connect.features.navigation.repository;

import com.campus.Campus_Connect.features.navigation.entity.DestinationNavMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DestinationNavMappingRepository extends JpaRepository<DestinationNavMapping, Long> {
    Optional<DestinationNavMapping> findByEntityTypeAndEntityId(String entityType, Long entityId);
}