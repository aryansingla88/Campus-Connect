package com.campus.Campus_Connect.features.map.repository;

import com.campus.Campus_Connect.features.map.entity.Poi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PoiRepository extends JpaRepository<Poi, Integer> {

}