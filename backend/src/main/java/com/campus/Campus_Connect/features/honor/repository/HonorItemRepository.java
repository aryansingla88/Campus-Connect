package com.campus.Campus_Connect.features.honor.repository;

import com.campus.Campus_Connect.features.honor.entity.HonorItem;
import com.campus.Campus_Connect.features.honor.enums.HonorType;
import com.campus.Campus_Connect.features.honor.enums.StatisticType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HonorItemRepository
        extends JpaRepository<HonorItem,Integer> {


    Optional<HonorItem> findByEvent_IdAndTypeAndTitle(
            Integer eventId,
            HonorType type,
            String title
    );

    List<HonorItem> findAllByEventIdAndType(
            Integer eventId,
            HonorType type
    );

    boolean existsByEventIdAndTypeAndTitle(
            Integer eventId,
            HonorType type,
            String title
    );

    Optional<HonorItem> findByIdAndEvent_Id(
            Integer honorId,
            Integer eventId
    );

    // ---------- Badges ----------

    List<HonorItem> findByType(HonorType type);

    List<HonorItem> findByTypeAndStatisticTypeOrderByThresholdAsc(
            HonorType type,
            StatisticType statisticType
    );

}
