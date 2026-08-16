package com.campus.Campus_Connect.features.honor.repository;

import com.campus.Campus_Connect.features.honor.entity.HonorItem;
import com.campus.Campus_Connect.features.honor.entity.UserHonor;
import com.campus.Campus_Connect.features.honor.entity.UserHonorId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserHonorRepository
        extends JpaRepository<UserHonor, UserHonorId> {

    List<UserHonor> findAllByHonor_Id(
            Integer honorId
    );

    List<UserHonor> findAllByHonorIn(
            List<HonorItem> honors
    );

    boolean existsByUser_IdAndHonor_Event_Id(
            Integer userId,
            Integer eventId
    );

    boolean existsByUser_IdAndHonor_Id(
            Integer userId,
            Integer honorId
    );

    void deleteByHonor_Id(
            Integer honorId
    );

    long countByUser_Id(
            Integer userId
    );
}