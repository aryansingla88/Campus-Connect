package com.campus.Campus_Connect.features.metadata.clubs;

import com.campus.Campus_Connect.common.response.ApiResponse;
import com.campus.Campus_Connect.features.metadata.clubs.dto.ClubMetadataResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/metadata/clubs")
@RequiredArgsConstructor
public class ClubMetadataController {

    private final ClubMetadataService clubMetadataService;

    @GetMapping
    public ApiResponse<List<ClubMetadataResponse>> getClubs() {
        return ApiResponse.success(
                clubMetadataService.getClubs(),
                "Club metadata fetched successfully."
        );
    }
}
