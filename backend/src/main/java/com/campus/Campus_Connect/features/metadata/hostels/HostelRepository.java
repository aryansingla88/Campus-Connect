package com.campus.Campus_Connect.features.metadata.hostels;

import com.campus.Campus_Connect.features.metadata.hostels.entity.Hostel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HostelRepository extends JpaRepository<Hostel, Integer> {
    List<Hostel> findAllByActiveTrueOrderByNameAsc();

    Optional<Hostel> findByIdAndActiveTrue(Integer id);
}
