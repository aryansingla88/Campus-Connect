package com.campus.Campus_Connect.features.honor.repository;

import com.campus.Campus_Connect.features.honor.entity.HonorItem;
import com.campus.Campus_Connect.features.honor.entity.UserHonor;
import com.campus.Campus_Connect.features.honor.entity.UserHonorId;
import com.campus.Campus_Connect.features.honor.enums.HonorType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

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

    long countByUser_IdAndHonor_Type(
            Integer userId,
            HonorType type
    );

    List<UserHonor> findByUser_IdAndHonor_TypeOrderByPriorityAsc(
            Integer userId,
            HonorType type
    );

    Optional<UserHonor> findByUser_IdAndHonor_Id(
            Integer userId,
            Integer honorId
    );


}