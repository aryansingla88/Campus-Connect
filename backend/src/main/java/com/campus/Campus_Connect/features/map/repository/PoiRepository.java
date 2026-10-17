package com.campus.Campus_Connect.features.map.repository;

import com.campus.Campus_Connect.features.map.entity.Poi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PoiRepository extends JpaRepository<Poi, Integer> {

    @Query(value = "SELECT p.* FROM poi p WHERE NOT EXISTS " +
            "(SELECT 1 FROM shops s WHERE s.poi_id = p.id)", nativeQuery = true)
    java.util.List<Poi> findAllBasePois();

}
