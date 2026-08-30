package com.campus.Campus_Connect.features.event.service.impl;

import com.campus.Campus_Connect.common.response.ApiResponse;
import com.campus.Campus_Connect.features.event.dto.response.EventCategoryResponse;
import com.campus.Campus_Connect.features.event.entity.EventCategory;
import com.campus.Campus_Connect.features.event.repository.EventCategoryRepository;
import com.campus.Campus_Connect.features.event.service.EventCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventCategoryServiceImpl implements EventCategoryService {

    private final EventCategoryRepository eventCategoryRepository;

    @Override
    public ApiResponse<List<EventCategoryResponse>> getAllCategories() {

        List<EventCategoryResponse> categories =
                eventCategoryRepository.findAll()
                        .stream()
                        .map(this::toResponse)
                        .toList();

        return ApiResponse.success(
                categories,
                "Event categories fetched successfully."
        );
    }

    private EventCategoryResponse toResponse(EventCategory category) {

        return new EventCategoryResponse(
                category.getId(),
                category.getName()
        );
    }
}