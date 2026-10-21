package com.example.campusconnect.feature.registrations.userflow

import com.example.campusconnect.feature.registrations.userflow.data.FakeRegistrationStore
import com.example.campusconnect.feature.registrations.userflow.data.FakeUserRegistrationRepository
import com.example.campusconnect.feature.registrations.userflow.model.ParticipationMode
import com.example.campusconnect.feature.registrations.userflow.model.UserRegistrationStep
import com.example.campusconnect.feature.registrations.userflow.viewmodel.UserRegistrationViewModel
import com.example.campusconnect.feature.registrations.userflow.data.UserRegistrationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UserRegistrationTeamNameTest {
    private val dispatcher = StandardTestDispatcher()

    @Before fun setUp() {
        Dispatchers.setMain(dispatcher)
        FakeRegistrationStore.resetForTests()
    }

    @After fun tearDown() { Dispatchers.resetMain() }

    @Test fun checkingNameOnlyConfirmsAvailabilityAndNextRemainsSeparate() = runTest(dispatcher) {
        val viewModel = UserRegistrationViewModel(FakeUserRegistrationRepository())
        viewModel.start(eventId = 1, participantId = 2)
        advanceUntilIdle()
        assertFalse(viewModel.state.value.teamNamePromptOpen)
        viewModel.submitDetails()
        assertEquals(UserRegistrationStep.PARTICIPATION, viewModel.state.value.step)
        viewModel.selectMode(ParticipationMode.CREATE_TEAM)
        assertTrue(viewModel.state.value.teamNamePromptOpen)

        viewModel.validateTeamName("xy")
        assertEquals("Use at least 3 characters.", viewModel.state.value.teamNameError)
        assertEquals("Use at least 3 characters.", viewModel.state.value.teamNameResultMessage)
        assertFalse(viewModel.state.value.teamNameConfirmed)

        viewModel.validateTeamName("Code Crafters")
        advanceUntilIdle()
        assertEquals("That team name is already in use.", viewModel.state.value.teamNameError)
        assertEquals("That team name is already in use.", viewModel.state.value.teamNameResultMessage)

        viewModel.validateTeamName("Bright Sparks")
        advanceUntilIdle()
        assertTrue(viewModel.state.value.teamNameConfirmed)
        assertEquals("That team name is available.", viewModel.state.value.teamNameResultMessage)
        assertNull(viewModel.state.value.teamNameError)
        assertFalse(viewModel.state.value.teamNamePromptOpen)
        assertEquals(UserRegistrationStep.PARTICIPATION, viewModel.state.value.step)
        assertNull(FakeRegistrationStore.teamPortalData().myTeam)
        viewModel.dismissTeamNameResult()
        assertFalse(viewModel.state.value.teamNamePromptOpen)
        assertTrue(viewModel.state.value.teamNameConfirmed)
        assertNull(FakeRegistrationStore.teamPortalData().myTeam)
    }

    @Test fun unavailableNameCannotBeConfirmedOrAdvanced() = runTest(dispatcher) {
        val delegate = FakeUserRegistrationRepository()
        val repository = object : UserRegistrationRepository by delegate {
            override suspend fun isTeamNameAvailable(eventId: Int, name: String): Result<Boolean> = Result.success(false)
        }
        val viewModel = UserRegistrationViewModel(repository)
        viewModel.start(eventId = 1, participantId = 2)
        advanceUntilIdle()
        viewModel.selectMode(ParticipationMode.CREATE_TEAM)
        viewModel.validateTeamName("Already Used")
        advanceUntilIdle()

        assertFalse(viewModel.state.value.teamNameConfirmed)
        assertEquals("That team name is already in use.", viewModel.state.value.teamNameResultMessage)
        viewModel.dismissTeamNameResult()
        assertTrue(viewModel.state.value.teamNamePromptOpen)
        assertFalse(viewModel.state.value.teamNameConfirmed)
        assertNull(FakeRegistrationStore.teamPortalData().myTeam)
    }
}
