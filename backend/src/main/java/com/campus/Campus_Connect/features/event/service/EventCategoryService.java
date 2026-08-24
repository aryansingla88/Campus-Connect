package com.campus.Campus_Connect.features.event.service;

import com.campus.Campus_Connect.common.response.ApiResponse;
import com.campus.Campus_Connect.features.event.dto.response.EventCategoryResponse;

import java.util.List;

public interface EventCategoryService {

    ApiResponse<List<EventCategoryResponse>> getAllCategories();
}