package com.campus.Campus_Connect.features.metadata.interest;

import com.campus.Campus_Connect.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class InterestController {

    private final InterestService interestService;

    //------------------------------------------------------

    @GetMapping("/interests")
    public ApiResponse<List<InterestResponse>> getInterests() {
        return interestService.getInterests();
    }

    //------------------------------------------------------

    @GetMapping("/users/me/interests")
    public ApiResponse<List<InterestResponse>> getMyInterests() {
        return interestService.getMyInterests();
    }

    //------------------------------------------------------

    @GetMapping("/users/{userId}/interests")
    public ApiResponse<List<InterestResponse>> getUserInterests(
            @PathVariable Integer userId
    ) {
        return interestService.getUserInterests(userId);
    }

    //------------------------------------------------------

    @PostMapping("/users/me/interests/{interestId}")
    public ApiResponse<Void> addInterest(
            @PathVariable Integer interestId
    ) {
        return interestService.addInterest(interestId);
    }

    //------------------------------------------------------

    @DeleteMapping("/users/me/interests/{interestId}")
    public ApiResponse<Void> removeInterest(
            @PathVariable Integer interestId
    ) {
        return interestService.removeInterest(interestId);
    }
}