package com.campus.Campus_Connect.features.metadata.interest.repo;

import com.campus.Campus_Connect.features.metadata.interest.entity.Interest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterestRepository extends JpaRepository<Interest, Integer> {
}