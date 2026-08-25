package com.campus.Campus_Connect.features.navigation.repository;

import com.campus.Campus_Connect.features.navigation.entity.NavEdge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NavEdgeRepository extends JpaRepository<NavEdge, Long> {
}