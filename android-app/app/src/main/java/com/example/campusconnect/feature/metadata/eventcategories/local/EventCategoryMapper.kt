package com.example.campusconnect.feature.metadata.eventcategories.local

import com.example.campusconnect.feature.metadata.eventcategories.EventCategory
import com.example.campusconnect.feature.metadata.eventcategories.remote.EventCategoryResponse

fun EventCategoryEntity.toEventCategory() =
    EventCategory(
        id = id,
        name = name
    )

fun EventCategory.toEntity() =
    EventCategoryEntity(
        id = id,
        name = name
    )

fun EventCategoryResponse.toEventCategory() =
    EventCategory(
        id = id,
        name = name
    )
