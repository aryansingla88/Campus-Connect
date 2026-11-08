package com.example.campusconnect.feature.registrations

import androidx.lifecycle.SavedStateHandle
import com.example.campusconnect.feature.registrations.domain.repository.RegistrationRepository
import com.example.campusconnect.feature.registrations.model.*
import com.example.campusconnect.feature.registrations.viewmodel.CreateState
import com.example.campusconnect.feature.registrations.viewmodel.ParticipantRegistrationViewModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ParticipantRegistrationViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    @Before fun setUp() { Dispatchers.setMain(dispatcher) }
    @After fun tearDown() { Dispatchers.resetMain() }

    @Test fun initializesAndKeepsOneSessionPerDraft() = runTest(dispatcher) {
        val repo = FakeRegistrationRepository()
        val vm = ParticipantRegistrationViewModel(repo)
        vm.start(11)
        advanceUntilIdle()
        assertEquals(1, repo.initializations)
        val session = vm.state.value.session!!
        assertEquals(11, session.eventId)
        assertEquals(111, session.registrationId)
        vm.start(11)
        advanceUntilIdle()
        assertEquals(1, repo.initializations)
    }

    @Test fun oldInitializationCannotOverwriteSwitchedEvent() = runTest(dispatcher) {
        val repo = FakeRegistrationRepository()
        val a = CompletableDeferred<Result<RegistrationDraft>>()
        repo.results[1] = a
        val vm = ParticipantRegistrationViewModel(repo)
        vm.start(1)
        advanceUntilIdle()
        vm.start(2)
        advanceUntilIdle()
        a.complete(Result.success(RegistrationDraft(1001, emptyList())))
        advanceTimeBy(500)
        advanceUntilIdle()
        assertEquals(2, vm.state.value.session?.eventId)
        assertEquals(102, vm.state.value.session?.registrationId)
    }

    @Test fun validatesAndPersistsIndividualAnswersBeforeAdvancing() = runTest(dispatcher) {
        val repo = FakeRegistrationRepository().apply {
            draft = RegistrationDraft(101, listOf(RegistrationQuestion(backendId = 7, label = "Name", isRequired = true)), allowSolo = true, allowTeam = true)
        }
        val vm = ParticipantRegistrationViewModel(repo)
        vm.start(1); advanceUntilIdle()
        vm.submitDetails(); advanceUntilIdle()
        assertTrue(vm.state.value.showErrors)
        vm.onAnswerChange(7, "Ari")
        vm.submitDetails(); advanceUntilIdle()
        assertEquals(mapOf(7 to "Ari"), repo.savedAnswers)
        assertEquals(UserRegistrationStep.PARTICIPATION, vm.state.value.session?.progress)
    }

    @Test fun restoresSmallSessionIdentifiersButReloadsDraft() = runTest(dispatcher) {
        val handle = SavedStateHandle(mapOf(
            "registration_event_id" to 3,
            "registration_id" to 103,
            "registration_mode" to ParticipationMode.JOIN_TEAM.name,
            "registration_team_id" to 12,
            "registration_progress" to UserRegistrationStep.TEAM_PORTAL.name,
        ))
        val repo = FakeRegistrationRepository().apply { draft = RegistrationDraft(103, emptyList()) }
        val vm = ParticipantRegistrationViewModel(repo, handle)
        vm.resumeSubmission()
        vm.start(3); advanceUntilIdle()
        assertEquals(ParticipationMode.JOIN_TEAM, vm.state.value.session?.mode)
        assertEquals(12, vm.state.value.session?.teamId)
        assertEquals(UserRegistrationStep.TEAM_PORTAL, vm.state.value.step)
    }

    private class FakeRegistrationRepository : RegistrationRepository {
        var draft = RegistrationDraft(101, emptyList())
        var initializations = 0
        var savedAnswers: Map<Int, String> = emptyMap()
        val results = mutableMapOf<Int, CompletableDeferred<Result<RegistrationDraft>>>()
        override suspend fun createRegistration(eventId: Int): Result<RegistrationDraft> {
            initializations++
            return results[eventId]?.await() ?: Result.success(draft.copy(registrationId = 100 + eventId))
        }
        override suspend fun loadSettings(eventId: Int) = RegistrationConfigLoadResult.Missing
        override suspend fun saveSettings(eventId: Int, settings: RegistrationSettings, exists: Boolean) = Result.success(Unit)
        override suspend fun saveQuickFields(eventId: Int, mask: Long) = Result.success(RegistrationSettings())
        override suspend fun loadQuestions(eventId: Int, scope: QuestionScope?) = Result.success(emptyList<RegistrationQuestion>())
        override suspend fun saveQuestions(eventId: Int, questions: List<RegistrationQuestion>) = Result.success(questions)
        override suspend fun getRegistration(eventId: Int) = Result.success(RegistrationSnapshot(eventId, null, null, null, null, false))
        override suspend fun saveIndividualAnswers(registrationId: Int, answers: Map<Int, String>): Result<Unit> { savedAnswers = answers; return Result.success(Unit) }
        override suspend fun cancelRegistration(eventId: Int) = Result.success(Unit)
    }
}
