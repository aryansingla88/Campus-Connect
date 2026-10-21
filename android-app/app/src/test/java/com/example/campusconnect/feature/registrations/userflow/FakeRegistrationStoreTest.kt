package com.example.campusconnect.feature.registrations.userflow

import com.example.campusconnect.feature.registrations.userflow.data.FakeRegistrationStore
import com.example.campusconnect.feature.registrations.userflow.data.FakeUserRegistrationRepository
import com.example.campusconnect.feature.registrations.userflow.data.FakeRegistrationSubmitRepository
import com.example.campusconnect.feature.registrations.userflow.data.toParticipantRegistrationDetail
import com.example.campusconnect.feature.registrations.userflow.model.ParticipationMode
import com.example.campusconnect.feature.registrations.userflow.model.SubmissionContext
import com.example.campusconnect.feature.registrations.userflow.model.SubmissionRequest
import com.example.campusconnect.feature.registrations.userflow.model.TeamMembershipStatus
import com.example.campusconnect.feature.registrations.userflow.model.TeamRole
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FakeRegistrationStoreTest {
    private val userRepository = FakeUserRegistrationRepository()

    @Before fun reset() { FakeRegistrationStore.resetForTests() }

    @Test fun requestAndInviteTransitionsChangeDisplayedMembershipState() = runTest {
        userRepository.selectParticipant(3)
        assertTrue(userRepository.requestToJoin(1, 2).isSuccess)
        var data = userRepository.getTeams(1).getOrThrow()
        assertEquals(TeamMembershipStatus.REQUEST_PENDING, data.membershipStatus)
        assertEquals(2, data.pendingTeam?.id)
        assertTrue(data.teams.first { it.id == 2 }.memberCount == 2)

        FakeRegistrationStore.resetForTests()
        assertTrue(userRepository.respondToInvite(1, 101, true).isSuccess)
        data = userRepository.getTeams(1).getOrThrow()
        assertEquals(TeamMembershipStatus.MEMBER, data.membershipStatus)
        assertEquals(TeamRole.MEMBER, data.myTeamRole)
        assertEquals(6, data.myTeam?.id)
        assertEquals(4, data.myTeam?.memberCount)
    }

    @Test fun leaderCanManageRequestsAndInvitationsWithCapacityEnforcement() = runTest {
        val submitRepository = FakeRegistrationSubmitRepository()
        FakeRegistrationStore.selectParticipant(2)
        val request = SubmissionRequest(
            SubmissionContext(1, "Event", ParticipationMode.CREATE_TEAM, participantId = 2, teamName = "Leaders"),
            teamAnswers = mapOf(10 to "Participate", 11 to "Education"),
        )
        submitRepository.submit(request).getOrThrow()
        var data = userRepository.getTeams(1).getOrThrow()
        assertEquals(TeamRole.LEADER, data.myTeamRole)
        assertEquals(1, data.members.size)
        assertEquals(1, data.incomingJoinRequests.size)

        assertTrue(userRepository.respondToJoinRequest(1, data.incomingJoinRequests.single().id, true).isSuccess)
        data = userRepository.getTeams(1).getOrThrow()
        assertEquals(2, data.myTeam?.memberCount)
        assertTrue(userRepository.inviteMember(1, 3).isSuccess)
        data = userRepository.getTeams(1).getOrThrow()
        assertEquals("ira.kapoor@abc.edu", data.outgoingInvites.single().email)
        val firstInviteId = data.outgoingInvites.single().id
        assertTrue(userRepository.cancelInvitation(1, firstInviteId).isSuccess)
        userRepository.selectParticipant(3)
        assertTrue(userRepository.getTeams(1).getOrThrow().invites.isEmpty())
        userRepository.selectParticipant(2)
        assertTrue(userRepository.inviteMember(1, 3).isSuccess)
        data = userRepository.getTeams(1).getOrThrow()
        val inviteId = data.outgoingInvites.single().id
        userRepository.selectParticipant(3)
        assertEquals(TeamMembershipStatus.INVITED, userRepository.getTeams(1).getOrThrow().membershipStatus)
        assertTrue(userRepository.respondToInvite(1, inviteId, true).isSuccess)
        assertEquals(TeamMembershipStatus.MEMBER, userRepository.getTeams(1).getOrThrow().membershipStatus)
        userRepository.selectParticipant(2)
        data = userRepository.getTeams(1).getOrThrow()
        assertTrue(data.outgoingInvites.isEmpty())
    }

    @Test fun rejectedInviteAndFullTeamDoNotChangeMembership() = runTest {
        assertTrue(userRepository.respondToInvite(1, 101, true).isSuccess)
        var data = userRepository.getTeams(1).getOrThrow()
        assertEquals(4, data.myTeam?.memberCount)
        assertEquals(TeamMembershipStatus.MEMBER, data.membershipStatus)
        assertTrue(data.invites.isEmpty())

        FakeRegistrationStore.resetForTests()
        userRepository.selectParticipant(3)
        assertFalse(userRepository.requestToJoin(1, 5).isSuccess)
        assertEquals(TeamMembershipStatus.NONE, userRepository.getTeams(1).getOrThrow().membershipStatus)
    }

    @Test fun rejectingInvitationLeavesVisibleRejectedState() = runTest {
        assertTrue(userRepository.respondToInvite(1, 101, false).isSuccess)
        val data = userRepository.getTeams(1).getOrThrow()
        assertTrue(data.invites.isEmpty())
        assertEquals(TeamMembershipStatus.INVITE_REJECTED, data.membershipStatus)
    }

    @Test fun deterministicPersonasProvideJoinerLeaderAndSoloStartingState() = runTest {
        userRepository.selectParticipant(1)
        val joiner = userRepository.getTeams(1).getOrThrow()
        assertEquals(TeamMembershipStatus.INVITED, joiner.membershipStatus)
        assertEquals(101, joiner.invites.single().id)

        userRepository.selectParticipant(2)
        val leader = userRepository.getTeams(1).getOrThrow()
        assertEquals(TeamMembershipStatus.NONE, leader.membershipStatus)
        val teamRequest = SubmissionRequest(
            SubmissionContext(1, "Event", ParticipationMode.CREATE_TEAM, participantId = 2, teamName = "Demo Leaders"),
            mapOf(11 to "Education"),
        )
        FakeRegistrationSubmitRepository().submit(teamRequest).getOrThrow()
        val createdTeam = userRepository.getTeams(1).getOrThrow()
        assertEquals(TeamRole.LEADER, createdTeam.myTeamRole)
        assertTrue(createdTeam.incomingJoinRequests.isNotEmpty())

        userRepository.selectParticipant(3)
        val individual = userRepository.getTeams(1).getOrThrow()
        assertEquals(TeamMembershipStatus.NONE, individual.membershipStatus)
        assertTrue(individual.myTeam == null)
    }

    @Test fun savedRegistrationCanBeRevisitedWithParticipantAndTeamAnswers() = runTest {
        FakeRegistrationStore.selectParticipant(2)
        val repo = FakeRegistrationSubmitRepository()
        val receipt = repo.submit(SubmissionRequest(
            SubmissionContext(1, "Demo Event", ParticipationMode.CREATE_TEAM, participantId = 2,
                participantAnswers = mapOf(1 to "Aryan Mehta", 2 to "aryan.mehta@abc.edu", 4 to "Computer Science"), teamName = "Saved Team"),
            teamAnswers = mapOf(10 to "Shared answer", 11 to "Education"),
        )).getOrThrow()
        val saved = repo.getSavedRegistration(2)!!
        assertEquals(receipt, saved.receipt)
        assertEquals("Shared answer", saved.participantAnswers[10])
        assertEquals("Computer Science", saved.participantAnswers[4])
        assertEquals("Education", saved.teamAnswers[11])
        assertEquals("Saved Team", saved.teamName)
        assertEquals(receipt.registrationId, saved.registrationId)
        assertEquals(receipt.registrationId, repo.getSavedRegistration(2)!!.registrationId)
        val hostDetail = FakeRegistrationStore.registrationDetailsById(receipt.registrationId)!!
            .toParticipantRegistrationDetail()
        assertEquals(receipt.registrationId, hostDetail.response.id)
        assertEquals("Aryan Mehta", hostDetail.response.name)
        assertEquals("Saved Team", hostDetail.response.teamName)
        assertEquals("Education", hostDetail.detail.teamAnswers.first { it.fieldId == 11 }.answer)
        assertEquals("Computer Science", hostDetail.detail.members.single().individualAnswers.first().answer)
    }

    @Test fun eachPersonaGetsItsOwnRegistrationIdAndResponseDetails() = runTest {
        val repo = FakeRegistrationSubmitRepository()
        FakeRegistrationStore.selectParticipant(3)
        val soloReceipt = repo.submit(SubmissionRequest(
            SubmissionContext(1, "Demo Event", ParticipationMode.SOLO, participantId = 3,
                participantAnswers = mapOf(1 to "Ira Kapoor", 2 to "ira.kapoor@abc.edu", 4 to "Computer Science")),
            teamAnswers = mapOf(10 to "Solo shared response"),
        )).getOrThrow()
        FakeRegistrationStore.selectParticipant(2)
        val leaderReceipt = repo.submit(SubmissionRequest(
            SubmissionContext(1, "Demo Event", ParticipationMode.CREATE_TEAM, participantId = 2,
                participantAnswers = mapOf(1 to "Aryan Mehta", 2 to "aryan.mehta@abc.edu"), teamName = "Persona Team"),
            teamAnswers = mapOf(11 to "Education"),
        )).getOrThrow()

        assertTrue(soloReceipt.registrationId != leaderReceipt.registrationId)
        val solo = FakeRegistrationStore.registrationDetailsById(soloReceipt.registrationId)!!
            .toParticipantRegistrationDetail()
        val leader = FakeRegistrationStore.registrationDetailsById(leaderReceipt.registrationId)!!
            .toParticipantRegistrationDetail()
        assertEquals(3, solo.detail.members.single().userId)
        assertEquals("Ira Kapoor", solo.response.name)
        assertEquals("Solo shared response", solo.detail.teamAnswers.single().answer)
        assertEquals(2, leader.detail.members.single().userId)
        assertEquals("Persona Team", leader.response.teamName)
        assertEquals("Education", leader.detail.teamAnswers.single().answer)
    }


    @Test fun leaderAcceptanceHonorsCapacityAndUpdatesRequesterMembership() = runTest {
        FakeRegistrationStore.selectParticipant(2)
        val submitRepo = FakeRegistrationSubmitRepository()
        submitRepo.submit(SubmissionRequest(
            SubmissionContext(1, "Event", ParticipationMode.CREATE_TEAM, participantId = 2, teamName = "Capacity Team"),
            mapOf(11 to "Education"),
        )).getOrThrow()
        var teamData = userRepository.getTeams(1).getOrThrow()
        val createdTeamId = teamData.myTeam!!.id
        userRepository.respondToJoinRequest(1, teamData.incomingJoinRequests.single().id, true).getOrThrow()
        userRepository.inviteMember(1, 3).getOrThrow()
        val invitationId = userRepository.getTeams(1).getOrThrow().outgoingInvites.single().id
        userRepository.selectParticipant(3)
        userRepository.respondToInvite(1, invitationId, true).getOrThrow()
        userRepository.selectParticipant(1)
        userRepository.respondToInvite(1, 101, false).getOrThrow()
        userRepository.requestToJoin(1, createdTeamId).getOrThrow()
        userRepository.selectParticipant(2)
        teamData = userRepository.getTeams(1).getOrThrow()
        val pending = teamData.incomingJoinRequests.first { it.participantId == 1 }
        userRepository.respondToJoinRequest(1, pending.id, true).getOrThrow()
        teamData = userRepository.getTeams(1).getOrThrow()
        assertEquals(4, teamData.myTeam?.memberCount)
        userRepository.selectParticipant(1)
        assertEquals(TeamMembershipStatus.MEMBER, userRepository.getTeams(1).getOrThrow().membershipStatus)
    }
}
