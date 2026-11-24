package com.example.campusconnect.feature.metadata.hostels.local

import com.example.campusconnect.feature.metadata.hostels.Hostel
import com.example.campusconnect.feature.metadata.hostels.remote.HostelResponse

fun HostelEntity.toHostel() = Hostel(id = id, hostel = hostel, name = name, active = active)
fun Hostel.toEntity() = HostelEntity(id = id, name = name, hostel = hostel, active = active)
fun HostelResponse.toHostel() = Hostel(id = id, hostel = hostel, name = name, active = active)
