package com.example.campusconnect.feature.registrations.userflow

import com.example.campusconnect.feature.registrations.userflow.data.RegistrationSubmitRepository
import com.example.campusconnect.feature.registrations.userflow.model.ParticipationMode
import com.example.campusconnect.feature.registrations.userflow.model.SubmissionContext
import com.example.campusconnect.feature.registrations.userflow.model.SubmissionReceipt
import com.example.campusconnect.feature.registrations.userflow.model.SubmissionRequest
import com.example.campusconnect.feature.registrations.ui.screens.FieldType
import com.example.campusconnect.feature.registrations.ui.screens.FormField
import com.example.campusconnect.feature.registrations.ui.screens.RegistrationQuestionContext
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
    fun soloRegistrationAnswersCommonQuestionsAndSubmitsParticipantAnswers() = runTest(dispatcher) {
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

        assertEquals(1, repository.questionLoads)
        assertTrue(viewModel.state.value.phase is SubmitPhase.Form)
        viewModel.onAnswerChange(10, "Explore the event")
        viewModel.onAnswerChange(11, "Education")
        viewModel.submit()
        advanceUntilIdle()
        assertTrue(viewModel.state.value.phase is SubmitPhase.Success)
        assertEquals(ParticipationMode.SOLO, repository.submission?.context?.mode)
        assertEquals(participantAnswers, repository.submission?.context?.participantAnswers)
        assertEquals("Explore the event", repository.submission?.teamAnswers?.get(10))
        assertEquals("Education", repository.submission?.teamAnswers?.get(11))
    }

    @Test
    fun restoresSavedSharedAnswersWhenSubmissionFlowLoads() = runTest(dispatcher) {
        val repository = RecordingRepository(savedTeamAnswers = mapOf(10 to "Previously saved"))
        val viewModel = RegistrationSubmitViewModel(repository)
        viewModel.start(SubmissionContext(eventId = 3, eventName = "Event #3", mode = ParticipationMode.SOLO))
        advanceUntilIdle()

        assertEquals("Previously saved", viewModel.state.value.answers[10])
    }

    @Test
    fun parentRetainedSharedAnswersSurviveSubmissionViewModelRecreation() = runTest(dispatcher) {
        val firstViewModel = RegistrationSubmitViewModel(RecordingRepository())
        val context = SubmissionContext(eventId = 3, eventName = "Event #3", mode = ParticipationMode.SOLO)
        firstViewModel.start(context)
        advanceUntilIdle()
        firstViewModel.onAnswerChange(10, "Unsaved answer")

        // The parent flow copies answers out on every edit and passes them back if the child UI/VM is recreated.
        val parentRetainedAnswers = firstViewModel.state.value.answers.toMap()
        val recreatedRepository = RecordingRepository(savedTeamAnswers = mapOf(10 to "Older server answer"))
        val recreatedViewModel = RegistrationSubmitViewModel(recreatedRepository)
        recreatedViewModel.start(context.copy(initialTeamAnswers = parentRetainedAnswers))
        advanceUntilIdle()
        assertEquals("Unsaved answer", recreatedViewModel.state.value.answers[10])

        recreatedViewModel.onAnswerChange(11, "Education")
        recreatedViewModel.submit()
        advanceUntilIdle()
        assertEquals("Unsaved answer", recreatedRepository.submission?.teamAnswers?.get(10))
    }

    @Test
    fun teamCreatorLoadsAndValidatesTeamQuestionsBeforeSubmitting() = runTest(dispatcher) {
        val repository = RecordingRepository(questions = listOf(
            FormField(id = 10, label = "Goals", fieldType = FieldType.TEXT, isRequired = true, individual = false, registrationContext = RegistrationQuestionContext.TEAM_COMMON),
            FormField(id = 11, label = "Domain", fieldType = FieldType.TEXT, isRequired = true, individual = false, registrationContext = RegistrationQuestionContext.TEAM_CREATION),
        ))
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
        assertTrue(viewModel.state.value.validationErrors.containsKey(10))

        viewModel.onAnswerChange(10, "Build an app")
        viewModel.onAnswerChange(11, "Education")
        viewModel.submit()
        advanceUntilIdle()

        assertTrue(viewModel.state.value.phase is SubmitPhase.Success)
        assertEquals(ParticipationMode.CREATE_TEAM, repository.submission?.context?.mode)
        assertEquals(participantAnswers, repository.submission?.context?.participantAnswers)
        assertEquals("Build an app", repository.submission?.teamAnswers?.get(10))
        assertEquals("Education", repository.submission?.teamAnswers?.get(11))
    }

    @Test
    fun acceptedTeamMemberDoesNotSeeLeaderSharedQuestions() = runTest(dispatcher) {
        val repository = RecordingRepository(questions = listOf(
            FormField(id = 10, label = "Goals", fieldType = FieldType.TEXT, isRequired = true, individual = false, registrationContext = RegistrationQuestionContext.TEAM_COMMON),
            FormField(id = 11, label = "Leader-only field", fieldType = FieldType.TEXT, isRequired = true, individual = false, registrationContext = RegistrationQuestionContext.TEAM_CREATION),
        ))
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

        assertEquals(1, repository.questionLoads)
        assertTrue(viewModel.state.value.questions.isEmpty())
        assertTrue(viewModel.state.value.phase is SubmitPhase.Form)
        viewModel.onAnswerChange(10, "Participate")
        viewModel.submit()
        advanceUntilIdle()
        assertTrue(viewModel.state.value.phase is SubmitPhase.Success)
        assertEquals(ParticipationMode.JOIN_TEAM, repository.submission?.context?.mode)
        assertEquals(6, repository.submission?.context?.teamId)
    }

    @Test
    fun emptyQuestionSetWaitsForExplicitSubmitAction() = runTest(dispatcher) {
        val repository = RecordingRepository(questions = emptyList())
        val viewModel = RegistrationSubmitViewModel(repository)
        viewModel.start(
            SubmissionContext(
                eventId = 3,
                eventName = "Event #3",
                mode = ParticipationMode.SOLO,
            ),
        )
        advanceUntilIdle()

        assertTrue(viewModel.state.value.phase is SubmitPhase.Form)
        assertEquals(null, repository.submission)

        viewModel.submit()
        advanceUntilIdle()
        assertTrue(viewModel.state.value.phase is SubmitPhase.Success)
        assertTrue(repository.submission != null)
    }

    private class RecordingRepository(
        private val questions: List<FormField> = listOf(
            FormField(id = 10, label = "Goals", fieldType = FieldType.TEXT, isRequired = true, individual = false, registrationContext = RegistrationQuestionContext.TEAM_COMMON),
            FormField(id = 11, label = "Domain", fieldType = FieldType.TEXT, isRequired = true, individual = false, registrationContext = RegistrationQuestionContext.TEAM_CREATION),
        ),
        private val savedTeamAnswers: Map<Int, String> = emptyMap(),
    ) : RegistrationSubmitRepository {
        var questionLoads: Int = 0
        var submission: SubmissionRequest? = null

        override suspend fun getTeamQuestions(eventId: Int): Result<List<FormField>> {
            questionLoads += 1
            return Result.success(questions)
        }

        override suspend fun getSavedTeamAnswers(eventId: Int): Result<Map<Int, String>> = Result.success(savedTeamAnswers)

        override suspend fun submit(request: SubmissionRequest): Result<SubmissionReceipt> {
            submission = request
            return Result.success(SubmissionReceipt("#TEST", "Completed"))
        }
    }
}
