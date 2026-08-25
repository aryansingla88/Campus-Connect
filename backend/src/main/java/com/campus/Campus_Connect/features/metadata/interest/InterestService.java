package com.campus.Campus_Connect.features.metadata.interest;

import com.campus.Campus_Connect.common.exception.BadRequestException;
import com.campus.Campus_Connect.common.exception.ResourceNotFoundException;
import com.campus.Campus_Connect.common.response.ApiResponse;
import com.campus.Campus_Connect.common.security.SecurityUtils;
import com.campus.Campus_Connect.features.auth.entity.User;
import com.campus.Campus_Connect.features.auth.repository.UserRepository;
import com.campus.Campus_Connect.features.metadata.interest.entity.Interest;
import com.campus.Campus_Connect.features.metadata.interest.entity.UserInterest;
import com.campus.Campus_Connect.features.metadata.interest.entity.UserInterestId;
import com.campus.Campus_Connect.features.metadata.interest.repo.InterestRepository;
import com.campus.Campus_Connect.features.metadata.interest.repo.UserInterestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InterestService {

    private final InterestRepository interestRepository;
    private final UserInterestRepository userInterestRepository;
    private final UserRepository userRepository;

    //------------------------------------------------------
    @Transactional(readOnly = true)
    public ApiResponse<List<InterestResponse>> getInterests() {

        List<Interest> interests = interestRepository.findAll();

        List<InterestResponse> response = new ArrayList<>();

        for (Interest interest : interests) {
            response.add(buildInterestResponse(interest));
        }

        return ApiResponse.success(
                response,
                "Interests fetched successfully."
        );
    }

    //------------------------------------------------------
    @Transactional(readOnly = true)
    public ApiResponse<List<InterestResponse>> getMyInterests() {

        User currentUser = SecurityUtils.getCurrentUser();

        List<UserInterest> userInterests =
                userInterestRepository.findAllByIdUserId(currentUser.getId());

        List<InterestResponse> response = new ArrayList<>();

        for (UserInterest userInterest : userInterests) {
            response.add(
                    buildInterestResponse(userInterest.getInterest())
            );
        }

        return ApiResponse.success(
                response,
                "User interests fetched successfully."
        );
    }

    //------------------------------------------------------
    @Transactional(readOnly = true)
    public ApiResponse<List<InterestResponse>> getUserInterests(
            Integer userId
    ) {

        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found.");
        }

        List<UserInterest> userInterests =
                userInterestRepository.findAllByIdUserId(userId);

        List<InterestResponse> response = new ArrayList<>();

        for (UserInterest userInterest : userInterests) {
            response.add(
                    buildInterestResponse(userInterest.getInterest())
            );
        }

        return ApiResponse.success(
                response,
                "User interests fetched successfully."
        );
    }

    //------------------------------------------------------
    @Transactional
    public ApiResponse<Void> addInterest(Integer interestId) {

        User currentUser = SecurityUtils.getCurrentUser();

        Interest interest = interestRepository.findById(interestId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Interest not found."));

        if (userInterestRepository.existsByIdUserIdAndIdInterestId(
                currentUser.getId(),
                interestId
        )) {
            throw new BadRequestException(
                    "Interest already added."
            );
        }

        UserInterest userInterest = UserInterest.builder()
                .id(new UserInterestId(
                        currentUser.getId(),
                        interestId
                ))
                .user(currentUser)
                .interest(interest)
                .build();

        userInterestRepository.save(userInterest);

        return ApiResponse.success(
                null,
                "Interest added successfully."
        );
    }

    //------------------------------------------------------
    @Transactional
    public ApiResponse<Void> removeInterest(Integer interestId) {

        User currentUser = SecurityUtils.getCurrentUser();

        if (!userInterestRepository.existsByIdUserIdAndIdInterestId(
                currentUser.getId(),
                interestId
        )) {
            throw new BadRequestException(
                    "Interest not found in your profile."
            );
        }

        userInterestRepository.deleteByIdUserIdAndIdInterestId(
                currentUser.getId(),
                interestId
        );

        return ApiResponse.success(
                null,
                "Interest removed successfully."
        );
    }

    //------------------------------------------------------

    private InterestResponse buildInterestResponse(
            Interest interest
    ) {
        return InterestResponse.builder()
                .interestId(interest.getId())
                .label(interest.getLabel())
                .category(interest.getCategory())
                .build();
    }
}