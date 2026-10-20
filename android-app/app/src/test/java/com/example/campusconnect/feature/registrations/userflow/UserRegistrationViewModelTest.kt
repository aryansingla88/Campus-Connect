package com.example.campusconnect.feature.registrations.userflow

import com.example.campusconnect.feature.registrations.userflow.data.FakeRegistrationStore
import com.example.campusconnect.feature.registrations.userflow.data.FakeUserRegistrationRepository
import com.example.campusconnect.feature.registrations.userflow.viewmodel.PortalState
import com.example.campusconnect.feature.registrations.userflow.viewmodel.UserRegistrationViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UserRegistrationViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before fun setUp() {
        Dispatchers.setMain(dispatcher)
        FakeRegistrationStore.resetForTests()
    }

    @After fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun participantCanHaveOnlyOnePendingJoinRequestInThePortal() = runTest(dispatcher) {
        val viewModel = UserRegistrationViewModel(FakeUserRegistrationRepository())
        viewModel.startPortal(eventId = 3, participantId = 3)
        advanceUntilIdle()
        val portal = (viewModel.state.value.portal as PortalState.Loaded).data
        val first = portal.teams.first { it.id == 2 }
        val second = portal.teams.first { it.id == 3 }

        viewModel.requestToJoin(first)
        viewModel.requestToJoin(second)
        advanceUntilIdle()

        assertEquals(setOf(first.id), viewModel.state.value.requestedTeamIds)
        assertEquals(setOf(first.id), FakeRegistrationStore.teamPortalData().pendingTeamIds)
    }
}
