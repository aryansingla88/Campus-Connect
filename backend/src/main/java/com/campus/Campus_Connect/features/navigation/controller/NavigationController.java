package com.campus.Campus_Connect.features.navigation.controller;

import com.campus.Campus_Connect.common.response.ApiResponse;
import com.campus.Campus_Connect.features.navigation.dto.RouteRequest;
import com.campus.Campus_Connect.features.navigation.dto.RouteResponse;
import com.campus.Campus_Connect.features.navigation.service.NavigationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("navigation/route")
@RequiredArgsConstructor
public class NavigationController {

    private final NavigationService navigationService;

    @PostMapping
    public ApiResponse<RouteResponse> getRoute(@RequestBody RouteRequest request) {
        RouteResponse response = navigationService.calculateRoute(request);
        return ApiResponse.success(response, "Navigation route calculated successfully.");
    }
}