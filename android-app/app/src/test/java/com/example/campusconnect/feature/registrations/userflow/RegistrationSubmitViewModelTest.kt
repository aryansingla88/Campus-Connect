package com.example.campusconnect.feature.registrations.userflow

import com.example.campusconnect.feature.registrations.userflow.data.RegistrationSubmitRepository
import com.example.campusconnect.feature.registrations.userflow.model.ParticipationMode
import com.example.campusconnect.feature.registrations.userflow.model.SubmissionContext
import com.example.campusconnect.feature.registrations.userflow.model.SubmissionReceipt
import com.example.campusconnect.feature.registrations.userflow.model.SubmissionRequest
import com.example.campusconnect.feature.registrations.userflow.model.UserFieldKind
import com.example.campusconnect.feature.registrations.userflow.model.UserQuestion
import com.example.campusconnect.feature.registrations.userflow.viewmodel.RegistrationSubmitViewModel
import com.example.campusconnect.feature.registrations.userflow.viewmodel.SubmitPhase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RegistrationSubmitViewModelTest {
    private lateinit var dispatcher: TestDispatcher

    @Before
    fun setUp() {
        dispatcher = StandardTestDispatcher()
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun soloRegistrationSkipsTeamQuestionsAndSubmitsParticipantAnswers() = runTest(dispatcher) {
        val repository = RecordingRepository()
        val viewModel = RegistrationSubmitViewModel(repository)
        val participantAnswers = mapOf(55 to "Aarav")

        viewModel.start(
            SubmissionContext(
                eventId = 3,
                eventName = "Event #3",
                mode = ParticipationMode.SOLO,
                participantAnswers = participantAnswers,
            ),
        )
        advanceUntilIdle()

        assertEquals(0, repository.questionLoads)
        assertTrue(viewModel.state.value.phase is SubmitPhase.Success)
        assertEquals(ParticipationMode.SOLO, repository.submission?.context?.mode)
        assertEquals(participantAnswers, repository.submission?.context?.participantAnswers)
        assertTrue(repository.submission?.teamAnswers.isNullOrEmpty())
    }

    @Test
    fun teamCreatorLoadsAndValidatesTeamQuestionsBeforeSubmitting() = runTest(dispatcher) {
        val repository = RecordingRepository(
            questions = listOf(
                UserQuestion(id = 10, label = "Idea", kind = UserFieldKind.TEXT, required = true),
            ),
        )
        val viewModel = RegistrationSubmitViewModel(repository)
        val participantAnswers = mapOf(55 to "Aarav")

        viewModel.start(
            SubmissionContext(
                eventId = 3,
                eventName = "Event #3",
                mode = ParticipationMode.CREATE_TEAM,
                participantAnswers = participantAnswers,
            ),
        )
        advanceUntilIdle()

        assertEquals(1, repository.questionLoads)
        assertTrue(viewModel.state.value.phase is SubmitPhase.Form)
        viewModel.submit()
        assertTrue(viewModel.state.value.showErrors)

        viewModel.onAnswerChange(10, "Build an app")
        viewModel.submit()
        advanceUntilIdle()

        assertTrue(viewModel.state.value.phase is SubmitPhase.Success)
        assertEquals(ParticipationMode.CREATE_TEAM, repository.submission?.context?.mode)
        assertEquals(participantAnswers, repository.submission?.context?.participantAnswers)
        assertEquals("Build an app", repository.submission?.teamAnswers?.get(10))
    }

    @Test
    fun acceptedTeamMemberSkipsLeaderTeamQuestionsAndPreservesTeamId() = runTest(dispatcher) {
        val repository = RecordingRepository()
        val viewModel = RegistrationSubmitViewModel(repository)

        viewModel.start(
            SubmissionContext(
                eventId = 3,
                eventName = "Event #3",
                mode = ParticipationMode.JOIN_TEAM,
                teamId = 6,
                participantAnswers = mapOf(55 to "Aarav"),
            ),
        )
        advanceUntilIdle()

        assertEquals(0, repository.questionLoads)
        assertTrue(viewModel.state.value.phase is SubmitPhase.Success)
        assertEquals(ParticipationMode.JOIN_TEAM, repository.submission?.context?.mode)
        assertEquals(6, repository.submission?.context?.teamId)
    }

    private class RecordingRepository(
        private val questions: List<UserQuestion> = emptyList(),
    ) : RegistrationSubmitRepository {
        var questionLoads: Int = 0
        var submission: SubmissionRequest? = null

        override suspend fun getTeamQuestions(eventId: Int): Result<List<UserQuestion>> {
            questionLoads += 1
            return Result.success(questions)
        }

        override suspend fun submit(request: SubmissionRequest): Result<SubmissionReceipt> {
            submission = request
            return Result.success(SubmissionReceipt("#TEST", "Completed"))
        }
    }
}
