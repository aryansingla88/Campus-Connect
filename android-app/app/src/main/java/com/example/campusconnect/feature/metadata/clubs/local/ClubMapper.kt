package com.example.campusconnect.feature.metadata.clubs.local

import com.example.campusconnect.feature.metadata.clubs.Club
import com.example.campusconnect.feature.metadata.clubs.remote.ClubResponse

fun ClubEntity.toClub(): Club {
    return Club(
        clubId = clubId,
        name = name
    )
}

fun Club.toEntity(): ClubEntity {
    return ClubEntity(
        clubId = clubId,
        name = name
    )
}

fun ClubResponse.toClub(): Club {
    return Club(
        clubId = clubId,
        name = name
    )
}