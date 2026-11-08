package com.example.campusconnect.feature.registrations

import com.example.campusconnect.feature.registrations.domain.repository.RegistrationRepository
import com.example.campusconnect.feature.registrations.model.*
import com.example.campusconnect.feature.registrations.viewmodel.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OrganizerRegistrationViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    @Before fun setUp() { Dispatchers.setMain(dispatcher) }
    @After fun tearDown() { Dispatchers.resetMain() }

    @Test fun missingAndExistingEmptyConfigurationRemainDistinct() = runTest(dispatcher) {
        val repository = FakeRepository()
        val viewModel = RegistrationConfigViewModel(repository)
        viewModel.load(1); advanceUntilIdle()
        assertEquals(RegistrationConfigurationStatus.NOT_CREATED, viewModel.state.value.configurationStatus)
        repository.settingsResult = RegistrationConfigLoadResult.Existing(RegistrationSettings())
        viewModel.load(2); advanceUntilIdle()
        assertEquals(RegistrationConfigurationStatus.CREATED_EMPTY, viewModel.state.value.configurationStatus)
    }

    @Test fun saveCreatesMissingConfigAndCleanReloadKeepsSavedValues() = runTest(dispatcher) {
        val repository = FakeRepository()
        val viewModel = RegistrationConfigViewModel(repository)
        viewModel.load(1); advanceUntilIdle()
        viewModel.updateSettings(RegistrationSettings(minMembers = 1, maxMembers = 3))
        viewModel.saveAll(); advanceUntilIdle()
        assertTrue(repository.configExists)
        assertFalse(viewModel.state.value.dirty)
        assertEquals(1, repository.settingsSaves)
    }

    @Test fun questionsKeepLocalIdsAndUnknownTypesAcrossScopeMovesAndSave() = runTest(dispatcher) {
        val repository = FakeRepository()
        val original = RegistrationQuestion(backendId = 18, localId = "local-question", label = "Q",
            type = QuestionType.Unknown("NEW_KIND"), scope = QuestionScope.INDIVIDUAL)
        repository.questionsResult = listOf(original)
        val viewModel = RegistrationFormViewModel(repository)
        viewModel.load(4); advanceUntilIdle()
        viewModel.moveToTeam(listOf(original.localId))
        viewModel.save(4); advanceUntilIdle()
        val saved = repository.savedQuestions.single()
        assertEquals(18, saved.backendId)
        assertEquals("local-question", saved.localId)
        assertEquals(QuestionType.Unknown("NEW_KIND"), saved.type)
        assertEquals(QuestionScope.TEAM, saved.scope)
        assertFalse(viewModel.builderState.value.dirty)
    }

    @Test fun saveFailureRetainsQuestionEditsAndRetryDoesNotReloadOverThem() = runTest(dispatcher) {
        val repository = FakeRepository()
        val viewModel = RegistrationFormViewModel(repository)
        viewModel.load(5); advanceUntilIdle()
        viewModel.setIndividualFields(listOf(RegistrationQuestion(localId = "draft", label = "Draft")))
        repository.questionSaveFailure = true
        viewModel.save(5); advanceUntilIdle()
        assertTrue(viewModel.builderState.value.dirty)
        assertEquals("draft", viewModel.builderState.value.questions.single().localId)
        repository.questionSaveFailure = false
        viewModel.retry(5); advanceUntilIdle()
        assertFalse(viewModel.builderState.value.dirty)
        assertEquals(2, repository.questionSaves)
    }

    @Test fun concurrentQuestionSaveIsIgnoredWhileRequestIsInFlight() = runTest(dispatcher) {
        val repository = FakeRepository()
        val pending = CompletableDeferred<Result<List<RegistrationQuestion>>>()
        repository.pendingSave = pending
        val viewModel = RegistrationFormViewModel(repository)
        viewModel.load(6); advanceUntilIdle()
        viewModel.setIndividualFields(listOf(RegistrationQuestion(localId = "one", label = "Question")))
        viewModel.save(6); advanceUntilIdle()
        viewModel.save(6); advanceUntilIdle()
        assertEquals(1, repository.questionSaves)
        pending.complete(Result.success(listOf(RegistrationQuestion(backendId = 5, localId = "server", label = "Question"))))
        advanceUntilIdle()
        assertFalse(viewModel.builderState.value.dirty)
    }

    @Test fun successfulSettingsRemainPersistedWhenQuickFieldsFailAndRetryDoesNotRepeatSettings() = runTest(dispatcher) {
        val repository = FakeRepository().apply { quickFieldsFailure = true }
        val viewModel = RegistrationConfigViewModel(repository)
        viewModel.load(7); advanceUntilIdle()
        viewModel.updateSettings(RegistrationSettings(minMembers = 1, maxMembers = 4, quickFieldMask = 1L))
        viewModel.saveAll(); advanceUntilIdle()
        assertEquals(1, repository.settingsSaves)
        assertTrue(viewModel.state.value.configExists)
        assertTrue(viewModel.state.value.dirty)
        repository.quickFieldsFailure = false
        viewModel.saveAll(); advanceUntilIdle()
        assertEquals(1, repository.settingsSaves)
        assertFalse(viewModel.state.value.dirty)
        assertEquals(2, repository.quickFieldSaves)
    }

    @Test fun editingQuestionsDuringSaveKeepsNewerEditDirtyAndKeepsAssignedBackendId() = runTest(dispatcher) {
        val repository = FakeRepository()
        val pending = CompletableDeferred<Result<List<RegistrationQuestion>>>()
        repository.pendingSave = pending
        val viewModel = RegistrationFormViewModel(repository)
        viewModel.load(8); advanceUntilIdle()
        val submitted = RegistrationQuestion(localId = "stable-local-id", label = "Before save")
        viewModel.setIndividualFields(listOf(submitted))
        var saveReportedSuccess: Boolean? = null
        viewModel.save(8) { saveReportedSuccess = it }; advanceUntilIdle()
        viewModel.setIndividualFields(listOf(submitted.copy(label = "Edited while saving")))
        pending.complete(Result.success(listOf(submitted.copy(backendId = 81))))
        advanceUntilIdle()

        val latest = viewModel.builderState.value
        assertEquals(false, saveReportedSuccess)
        assertTrue(latest.dirty)
        assertEquals("Edited while saving", latest.questions.single().label)
        assertEquals("stable-local-id", latest.questions.single().localId)
        assertEquals(81, latest.questions.single().backendId)
    }

    private class FakeRepository : RegistrationRepository {
        var settingsResult: RegistrationConfigLoadResult = RegistrationConfigLoadResult.Missing
        var configExists = false
        var settingsSaves = 0
        var questionsResult = emptyList<RegistrationQuestion>()
        var savedQuestions = emptyList<RegistrationQuestion>()
        var questionSaves = 0
        var questionSaveFailure = false
        var quickFieldsFailure = false
        var quickFieldSaves = 0
        var pendingSave: CompletableDeferred<Result<List<RegistrationQuestion>>>? = null
        override suspend fun loadSettings(eventId: Int) = settingsResult
        override suspend fun saveSettings(eventId: Int, settings: RegistrationSettings, exists: Boolean): Result<Unit> {
            settingsSaves++
            configExists = true
            settingsResult = RegistrationConfigLoadResult.Existing(settings)
            return Result.success(Unit)
        }
        override suspend fun saveQuickFields(eventId: Int, mask: Long): Result<RegistrationSettings> {
            quickFieldSaves++
            return if (quickFieldsFailure) Result.failure(IllegalStateException("quick fields"))
            else Result.success(RegistrationSettings(minMembers = 1, maxMembers = 4, quickFieldMask = mask))
        }
        override suspend fun loadQuestions(eventId: Int, scope: QuestionScope?): Result<List<RegistrationQuestion>> = Result.success(questionsResult)
        override suspend fun saveQuestions(eventId: Int, questions: List<RegistrationQuestion>): Result<List<RegistrationQuestion>> {
            questionSaves++
            pendingSave?.let { return it.await() }
            if (questionSaveFailure) return Result.failure(IllegalStateException("network"))
            savedQuestions = questions
            return Result.success(questions)
        }
        override suspend fun createRegistration(eventId: Int) = Result.success(RegistrationDraft(1, emptyList()))
        override suspend fun getRegistration(eventId: Int) = Result.success(RegistrationSnapshot(eventId, null, null, null, null, false))
        override suspend fun saveIndividualAnswers(registrationId: Int, answers: Map<Int, String>) = Result.success(Unit)
        override suspend fun cancelRegistration(eventId: Int) = Result.success(Unit)
    }
}
