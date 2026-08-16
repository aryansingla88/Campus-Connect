package com.campus.Campus_Connect.features.honor.service;

import com.campus.Campus_Connect.features.honor.entity.HonorItem;
import com.campus.Campus_Connect.features.honor.entity.UserHonor;
import com.campus.Campus_Connect.features.honor.entity.UserHonorId;
import com.campus.Campus_Connect.features.honor.enums.HonorType;
import com.campus.Campus_Connect.features.honor.enums.StatisticType;
import com.campus.Campus_Connect.features.honor.repository.HonorItemRepository;
import com.campus.Campus_Connect.features.honor.repository.UserHonorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BadgeEvaluatorService {

    private final HonorItemRepository honorItemRepository;
    private final UserHonorRepository userHonorRepository;

    public void evaluateBadges(
            Integer userId,
            StatisticType statisticType,
            int currentValue
    ) {
        
        List<HonorItem> badges =
                honorItemRepository.findByTypeAndStatisticTypeOrderByThresholdAsc(
                        HonorType.BADGE,
                        statisticType
                );

        for (HonorItem badge : badges) {

            if (badge.getThreshold() > currentValue) {
                break;
            }

            if (userHonorRepository.existsByUser_IdAndHonor_Id(
                    userId,
                    badge.getId()
            )) {
                continue;
            }

            UserHonor userHonor = new UserHonor();
            userHonor.setId(new UserHonorId(userId, badge.getId()));

            userHonorRepository.save(userHonor);
        }
    }
}