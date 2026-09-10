package com.campus.Campus_Connect.features.map.enums;

/**
 * Visual classification for Points of Interest.
 *
 * Currently mirrors PoiCategory so that backend data remains
 * consistent while the frontend maps each type to its visual marker.
 */
public enum PoiIconType {

    // Academic & Learning
    ACADEMIC,
    LIBRARY,
    RESEARCH,

    // Administration & Student Services
    ADMINISTRATION,
    STUDENT_FACILITY,

    // Accommodation
    HOSTEL,
    ACCOMMODATION,
    RESIDENTIAL,

    // Food & Commercial
    FOOD,
    SHOP,
    FINANCIAL,

    // Health & Wellness
    HEALTHCARE,
    FITNESS,

    // Sports & Recreation
    SPORTS,

    // Campus Infrastructure
    PARKING,
    TRANSPORT,
    ENTRANCE,

    // Outdoor & Navigation
    OPEN_SPACE,
    LANDMARK,

    OTHER
}