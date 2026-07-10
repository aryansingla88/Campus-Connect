package com.campus.Campus_Connect.features.metadata.interest.repo;

import com.campus.Campus_Connect.features.metadata.interest.entity.UserInterest;
import com.campus.Campus_Connect.features.metadata.interest.entity.UserInterestId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserInterestRepository extends JpaRepository<UserInterest, UserInterestId> {

    List<UserInterest> findAllByIdUserId(
            Integer userId
    );

    boolean existsByIdUserIdAndIdInterestId(
            Integer userId,
            Integer interestId
    );

    void deleteByIdUserIdAndIdInterestId(
            Integer userId,
            Integer interestId
    );
}