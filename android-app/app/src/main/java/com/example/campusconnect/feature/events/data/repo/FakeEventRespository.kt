package com.example.campusconnect.feature.events.data.repo

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.campusconnect.feature.events.data.fake.FakeEventService
import com.example.campusconnect.feature.events.data.fake.FakeMedalService
import com.example.campusconnect.feature.events.data.fake.FakeParticipantsService
import com.example.campusconnect.feature.events.data.fake.FakeUserAccessService
import com.example.campusconnect.feature.events.model.Event
import com.example.campusconnect.feature.events.model.MedalAward
import com.example.campusconnect.feature.events.model.MedalType
import com.example.campusconnect.feature.events.registrations.data.FakeRegistrationService
import com.example.campusconnect.feature.events.registrations.model.Registration

class FakeEventRepository : EventRepository {

    private val eventService = FakeEventService()
    private val participantService = FakeParticipantsService()
    private val medalService = FakeMedalService

    // Events -------------------------------------------------------------

    override suspend fun getEvents() =
        Result.success(
            eventService.getEvents()
        )

    override suspend fun getEvent(
        eventId: Int
    ) = Result.success(
        eventService.getEvents()
            .first { it.id == eventId }
    )

    override suspend fun createEvent(
        event: Event
    ): Result<Event> {

        eventService.createEvent(event)

        return Result.success(event)
    }

    override suspend fun updateEvent(
        event: Event
    ): Result<Event> {

        eventService.updateEvent(event)

        return Result.success(event)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override suspend fun deleteEvent(
        eventId: Int
    ): Result<Unit> {

        eventService.deleteEvent(eventId)

        return Result.success(Unit)
    }


    // Registration -------------------------------------------------------

    override suspend fun getRegistration(
        eventId: Int
    ): Result<Registration> {

        return Result.success(
            FakeRegistrationService.registration
        )
    }


    // Participants -------------------------------------------------------

    override suspend fun getTeams(
        eventId: Int
    ) = Result.success(
        participantService.getTeams(eventId)
    )

    override suspend fun getSoloParticipants(
        eventId: Int
    ) = Result.success(
        participantService.getSoloParticipants(eventId)
    )

    override suspend fun getParticipantsCount(
        eventId: Int
    ) = Result.success(
        participantService.getTotalCount(eventId)
    )


    // Medals -------------------------------------------------------------

    override suspend fun getMedalsForEvent(
        eventId: Int
    ) = Result.success(
        medalService.getAwardsForEvent(eventId)
    )

    override suspend fun awardMedal(
        award: MedalAward
    ): Result<MedalAward> {

        medalService.awardMedal(award)

        return Result.success(award)
    }

    override suspend fun removeMedal(
        eventId: Int,
        medalType: MedalType
    ): Result<Unit> {

        medalService.removeAward(
            eventId,
            medalType
        )

        return Result.success(Unit)
    }


    // Access -------------------------------------------------------------

    override suspend fun getUsersWithAccess(
        eventId: Int
    ) = Result.success(
        FakeUserAccessService.defaultAccess
    )

    override suspend fun searchUsers(
        eventId: Int,
        query: String
    ) = Result.success(
        FakeUserAccessService.search(query)
    )
}