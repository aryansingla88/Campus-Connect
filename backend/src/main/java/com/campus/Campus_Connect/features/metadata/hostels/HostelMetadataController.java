package com.campus.Campus_Connect.features.metadata.hostels;

import com.campus.Campus_Connect.common.response.ApiResponse;
import com.campus.Campus_Connect.features.metadata.hostels.dto.HostelResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/metadata/hostels")
@RequiredArgsConstructor
public class HostelMetadataController {

    private final HostelMetadataService hostelMetadataService;

    @GetMapping
    public ApiResponse<List<HostelResponse>> getHostels() {
        return ApiResponse.success(hostelMetadataService.getHostels(), "Hostel metadata fetched successfully.");
    }
}
