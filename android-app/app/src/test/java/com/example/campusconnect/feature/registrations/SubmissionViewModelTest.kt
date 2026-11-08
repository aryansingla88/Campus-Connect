package com.example.campusconnect.feature.registrations

import com.example.campusconnect.feature.registrations.domain.repository.SubmissionRepository
import com.example.campusconnect.feature.registrations.model.ParticipationMode
import com.example.campusconnect.feature.registrations.model.SubmissionContext
import com.example.campusconnect.feature.registrations.model.SubmissionReceipt
import com.example.campusconnect.feature.registrations.model.SubmissionRequest
import com.example.campusconnect.feature.registrations.model.QuestionScope
import com.example.campusconnect.feature.registrations.model.QuestionType
import com.example.campusconnect.feature.registrations.model.RegistrationQuestion
import com.example.campusconnect.feature.registrations.model.TeamQuestionSet
import com.example.campusconnect.feature.registrations.viewmodel.SubmissionViewModel
import com.example.campusconnect.feature.registrations.viewmodel.SubmitPhase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SubmissionViewModelTest {
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
        val viewModel = SubmissionViewModel(repository)
        val participantAnswers = mapOf(55 to "Aarav")

        viewModel.start(
            SubmissionContext(
                eventId = 3,
                registrationId = 20,
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
                RegistrationQuestion(backendId = 10, label = "Idea", type = QuestionType.Known.TEXT, isRequired = true, scope = QuestionScope.TEAM),
            ),
        )
        val viewModel = SubmissionViewModel(repository)
        val participantAnswers = mapOf(55 to "Aarav")

        viewModel.start(
            SubmissionContext(
                eventId = 3,
                registrationId = 20,
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
        val viewModel = SubmissionViewModel(repository)

        viewModel.start(
            SubmissionContext(
                eventId = 3,
                registrationId = 20,
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

    @Test
    fun delayedOldSubmissionCannotReplaceNewSessionAndRepeatedSubmitIsIgnored() = runTest(dispatcher) {
        val repository = RecordingRepository()
        val delayed = CompletableDeferred<Result<SubmissionReceipt>>()
        repository.delayedSubmission = delayed
        repository.delayedSubmissionRegistrationId = 20
        val viewModel = SubmissionViewModel(repository)
        viewModel.start(SubmissionContext(3, 20, "One", ParticipationMode.SOLO))
        viewModel.submit(); viewModel.submit()
        advanceUntilIdle()
        assertEquals(1, repository.submitCalls)
        viewModel.start(SubmissionContext(4, 21, "Two", ParticipationMode.SOLO))
        advanceUntilIdle()
        delayed.complete(Result.success(SubmissionReceipt("OLD", "Completed")))
        advanceUntilIdle()
        val success = viewModel.state.value.phase as SubmitPhase.Success
        assertEquals("#TEST", success.receipt.registrationCode)
        assertEquals(2, repository.submitCalls)
    }

    private class RecordingRepository(
        private val questions: List<RegistrationQuestion> = emptyList(),
    ) : SubmissionRepository {
        var questionLoads: Int = 0
        var submission: SubmissionRequest? = null
        var submitCalls: Int = 0
        var delayedSubmission: CompletableDeferred<Result<SubmissionReceipt>>? = null
        var delayedSubmissionRegistrationId: Int? = null

        override suspend fun getTeamQuestions(eventId: Int, registrationId: Int): Result<TeamQuestionSet> {
            questionLoads += 1
            return Result.success(TeamQuestionSet(questions))
        }

        override suspend fun submit(request: SubmissionRequest): Result<SubmissionReceipt> {
            submitCalls++
            submission = request
            return if (request.context.registrationId == delayedSubmissionRegistrationId) {
                delayedSubmission?.await() ?: Result.success(SubmissionReceipt("#TEST", "Completed"))
            } else {
                Result.success(SubmissionReceipt("#TEST", "Completed"))
            }
        }
    }
}

