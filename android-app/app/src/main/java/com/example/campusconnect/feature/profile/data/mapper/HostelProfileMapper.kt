package com.example.campusconnect.feature.profile.data.mapper

import com.example.campusconnect.feature.metadata.hostels.Hostel
import com.example.campusconnect.feature.profile.model.PublicUserProfile

fun PublicUserProfile.withHostelSelection(hostel: Hostel?): PublicUserProfile = if (hostel == null) {
    copy(hostelId = null, clearHostel = true)
} else {
    copy(hostelId = hostel.id, clearHostel = false)
}
