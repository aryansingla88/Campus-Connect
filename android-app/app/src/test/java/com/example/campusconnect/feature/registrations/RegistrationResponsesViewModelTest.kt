package com.example.campusconnect.feature.registrations

import com.example.campusconnect.feature.registrations.domain.repository.RegistrationResponseRepository
import com.example.campusconnect.feature.registrations.model.RegistrationDetailResponse
import com.example.campusconnect.feature.registrations.model.RegistrationResponseItem
import com.example.campusconnect.feature.registrations.model.RegistrationResponseStatus
import com.example.campusconnect.feature.registrations.model.RegistrationResponseType
import com.example.campusconnect.feature.registrations.model.ResponseAnswer
import com.example.campusconnect.feature.registrations.viewmodel.RegistrationDetailState
import com.example.campusconnect.feature.registrations.viewmodel.RegistrationResponsesViewModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RegistrationResponsesViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before fun setUp() { Dispatchers.setMain(dispatcher) }
    @After fun tearDown() { Dispatchers.resetMain() }

    @Test fun cachedDetailsAreReusedAcrossInlineAndFullDetailAndListRefresh() = runTest(dispatcher) {
        val repository = FakeResponseRepository()
        val viewModel = RegistrationResponsesViewModel(repository)
        viewModel.loadRegistrations(1); advanceUntilIdle()
        viewModel.toggleExpanded(11); advanceUntilIdle()
        assertEquals(1, repository.detailLoads)

        viewModel.loadDetails(1, 11); advanceUntilIdle()
        assertEquals(1, repository.detailLoads)

        repository.responses[1] = listOf(response(11, "Updated name"))
        viewModel.loadRegistrations(1, force = true); advanceUntilIdle()
        assertEquals("Updated name", viewModel.listState.value.registrations.single().name)
        assertTrue(viewModel.details.value[11] is RegistrationDetailState.Loaded)
        viewModel.loadDetails(1, 11); advanceUntilIdle()
        assertEquals(1, repository.detailLoads)
    }

    @Test fun missingDetailsAreFetchedAndRefreshAfterReviewUpdatesListStatus() = runTest(dispatcher) {
        val repository = FakeResponseRepository()
        val viewModel = RegistrationResponsesViewModel(repository)
        viewModel.loadRegistrations(1); advanceUntilIdle()
        viewModel.loadDetails(1, 11); advanceUntilIdle()
        assertEquals(1, repository.detailLoads)

        viewModel.reviewRegistration(11, approve = true); advanceUntilIdle()
        assertEquals(RegistrationResponseStatus.CONFIRMED, viewModel.listState.value.registrations.single().status)
        assertEquals(2, repository.listLoads)
        assertEquals(1, repository.detailLoads)
    }

    @Test fun lateListResponseCannotReplaceDataAfterSwitchingAwayAndBack() = runTest(dispatcher) {
        val repository = FakeResponseRepository()
        repository.responses[1] = listOf(response(10))
        val oldEventResponse = CompletableDeferred<Result<List<RegistrationResponseItem>>>()
        repository.pendingLists.getOrPut(1) { mutableListOf() }.add(oldEventResponse)
        val viewModel = RegistrationResponsesViewModel(repository)

        viewModel.loadRegistrations(1); runCurrent()
        viewModel.loadRegistrations(2); advanceUntilIdle()
        viewModel.loadRegistrations(1); advanceUntilIdle()
        assertEquals(10, viewModel.listState.value.registrations.single().id)

        oldEventResponse.complete(Result.success(listOf(response(99, "stale event 1"))))
        advanceUntilIdle()
        assertEquals(10, viewModel.listState.value.registrations.single().id)
    }

    @Test fun lateDetailAndReviewResponsesCannotPopulateAnotherEventSession() = runTest(dispatcher) {
        val repository = FakeResponseRepository()
        val oldDetail = CompletableDeferred<Result<RegistrationDetailResponse>>()
        val oldReview = CompletableDeferred<Result<Unit>>()
        repository.pendingDetails[1 to 11] = oldDetail
        repository.pendingReviews[1 to 11] = oldReview
        val viewModel = RegistrationResponsesViewModel(repository)

        viewModel.loadRegistrations(1); advanceUntilIdle()
        viewModel.loadDetails(1, 11); runCurrent()
        viewModel.reviewRegistration(11, approve = true); runCurrent()

        viewModel.loadRegistrations(2); advanceUntilIdle()
        assertEquals(2, viewModel.listState.value.eventId)
        val eventTwoLoadsBeforeOldReviewCompletes = repository.listLoads

        oldDetail.complete(Result.success(detail("stale")))
        oldReview.complete(Result.success(Unit))
        advanceUntilIdle()

        assertEquals(2, viewModel.listState.value.eventId)
        assertEquals(eventTwoLoadsBeforeOldReviewCompletes, repository.listLoads)
        assertTrue(viewModel.details.value.isEmpty())
    }

    private class FakeResponseRepository : RegistrationResponseRepository {
        val responses = mutableMapOf(1 to listOf(response(11)), 2 to listOf(response(22)))
        val pendingLists = mutableMapOf<Int, MutableList<CompletableDeferred<Result<List<RegistrationResponseItem>>>>>()
        val pendingDetails = mutableMapOf<Pair<Int, Int>, CompletableDeferred<Result<RegistrationDetailResponse>>>()
        val pendingReviews = mutableMapOf<Pair<Int, Int>, CompletableDeferred<Result<Unit>>>()
        var listLoads = 0
        var detailLoads = 0

        override suspend fun getRegistrations(eventId: Int): Result<List<RegistrationResponseItem>> {
            listLoads++
            val pending = pendingLists[eventId]?.takeIf { it.isNotEmpty() }?.removeAt(0)
            return pending?.await() ?: Result.success(responses[eventId].orEmpty())
        }

        override suspend fun getDetails(eventId: Int, registrationId: Int): Result<RegistrationDetailResponse> {
            detailLoads++
            return pendingDetails.remove(eventId to registrationId)?.await()
                ?: Result.success(detail("event $eventId"))
        }

        override suspend fun review(eventId: Int, registrationId: Int, approve: Boolean): Result<Unit> {
            val result = pendingReviews.remove(eventId to registrationId)?.await() ?: Result.success(Unit)
            if (result.isSuccess) {
                val updatedStatus = if (approve) RegistrationResponseStatus.CONFIRMED else RegistrationResponseStatus.REJECTED
                responses[eventId] = responses[eventId].orEmpty().map { response ->
                    if (response.id == registrationId) response.copy(status = updatedStatus) else response
                }
            }
            return result
        }
    }

    private companion object {
        fun response(id: Int, name: String = "Registrant $id") = RegistrationResponseItem(
            id = id,
            name = name,
            email = "$id@example.com",
            type = RegistrationResponseType.INDIVIDUAL,
            status = RegistrationResponseStatus.WAITLISTED,
            submittedAt = null,
        )

        fun detail(answer: String) = RegistrationDetailResponse(
            teamAnswers = listOf(ResponseAnswer(1, "Question", "TEXT", answer)),
        )
    }
}
