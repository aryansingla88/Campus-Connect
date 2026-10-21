package com.example.campusconnect.feature.registrations.userflow.data

import com.example.campusconnect.feature.registrations.userflow.model.ParticipationMode
import com.example.campusconnect.feature.registrations.userflow.model.SubmissionRequest
import com.example.campusconnect.feature.registrations.userflow.model.TeamInvite
import com.example.campusconnect.feature.registrations.userflow.model.TeamInvitation
import com.example.campusconnect.feature.registrations.userflow.model.TeamInviteCandidate
import com.example.campusconnect.feature.registrations.userflow.model.TeamJoinRequest
import com.example.campusconnect.feature.registrations.userflow.model.TeamMember
import com.example.campusconnect.feature.registrations.userflow.model.TeamMembershipStatus
import com.example.campusconnect.feature.registrations.userflow.model.TeamPortalData
import com.example.campusconnect.feature.registrations.userflow.model.TeamRole
import com.example.campusconnect.feature.registrations.userflow.model.TeamSummary
import com.example.campusconnect.feature.registrations.userflow.model.SavedRegistrationDetails
import com.example.campusconnect.feature.registrations.userflow.model.SubmissionReceipt
import com.example.campusconnect.feature.registrations.userflow.model.TEAM_NAME_MAX_LENGTH
import com.example.campusconnect.feature.registrations.userflow.model.TEAM_NAME_MIN_LENGTH

/** Shared in-memory source of truth for the fake participant and submission repositories. */
internal object FakeRegistrationStore {
    data class Persona(val id: Int, val name: String, val email: String, val phone: String)

    private data class ParticipantState(
        var currentTeamId: Int? = null,
        var currentRole: TeamRole? = null,
        val pendingTeamIds: MutableSet<Int> = mutableSetOf(),
        var lastRequestRejected: Boolean = false,
        var lastInviteRejected: Boolean = false,
        val incomingInvites: MutableList<TeamInvite> = mutableListOf(),
    )

    private data class TeamRecord(
        val id: Int,
        val name: String,
        val leaderName: String,
        val maxMembers: Int,
        val tags: List<String>,
        val members: MutableList<TeamMember>,
    ) {
        fun summary() = TeamSummary(id, name, leaderName, members.size, maxMembers, tags, members.toList())
    }

    private val teams = linkedMapOf<Int, TeamRecord>()
    private val joinRequests = mutableListOf<TeamJoinRequest>()
    private val sentInvites = mutableListOf<TeamInvitation>()
    private val participantStates = mutableMapOf<Int, ParticipantState>()
    private val registrations = mutableMapOf<Int, SavedRegistrationDetails>()
    private val latestRegistrationByParticipant = mutableMapOf<Int, Int>()
    private var activeParticipantId: Int = 1
    private val participantNames = mapOf(1 to "Aarav Sharma", 2 to "Aryan Mehta", 3 to "Ira Kapoor")
    private val participantEmails = mapOf(1 to "aarav@abc.edu", 2 to "aryan.mehta@abc.edu", 3 to "ira.kapoor@abc.edu")
    private val participantPrograms = mapOf(1 to "MCA · 2027", 2 to "BTech CSE · 2028", 3 to "BSc CS · 2028")
    private val activeState: ParticipantState get() = participantStates.getOrPut(activeParticipantId) { ParticipantState() }
    private val currentTeamId: Int? get() = activeState.currentTeamId
    private val currentRole: TeamRole? get() = activeState.currentRole
    private var nextTeamId = 8
    private var nextRequestId = 200
    private var nextInviteId = 300
    private var nextRegistrationId = 1001

    init { resetForTests() }

    @Synchronized
    fun selectParticipant(userId: Int) {
        activeParticipantId = userId
        participantStates.getOrPut(userId) { ParticipantState() }
    }

    @Synchronized
    fun currentPersona(): Persona = Persona(
        activeParticipantId,
        participantNames[activeParticipantId] ?: "Campus User $activeParticipantId",
        participantEmails[activeParticipantId] ?: "user$activeParticipantId@abc.edu",
        "+91 98765 43210",
    )

    @Synchronized
    fun resetForTests() {
        teams.clear()
        joinRequests.clear()
        sentInvites.clear()
        registrations.clear()
        latestRegistrationByParticipant.clear()
        participantStates.clear()
        activeParticipantId = 1
        nextTeamId = 8
        nextRequestId = 200
        nextInviteId = 300
        nextRegistrationId = 1001

        fun seed(id: Int, name: String, leader: String, count: Int, capacity: Int, tags: List<String>) {
            val members = MutableList(count) { index ->
                val memberName = if (index == 0) leader else "Member ${id}_$index"
                val memberId = if (id == 6 && index == 0) 602 else id * 100 + index
                val email = if (id == 6 && index == 0) "rohan.das@abc.edu" else "member${id}_$index@abc.edu"
                TeamMember(memberId, memberName, email, if (index == 0) TeamRole.LEADER else TeamRole.MEMBER,
                    program = "BTech CSE · 2027")
            }
            teams[id] = TeamRecord(id, name, leader, capacity, tags, members)
        }

        seed(1, "Code Crafters", "Riya Malhotra", 3, 4, listOf("Kotlin", "Android", "ML"))
        seed(2, "Byte Builders", "Dev Patel", 2, 4, listOf("UI/UX", "Firebase", "Web"))
        seed(3, "Syntax Squad", "Isha Rao", 1, 4, listOf("Web", "AI"))
        seed(4, "Think Tank", "Kabir Joshi", 3, 4, listOf("ML", "DL", "Data Science"))
        seed(5, "Pixel Pioneers", "Meera Nair", 4, 4, listOf("Design", "Web"))
        seed(6, "InnovateX", "Rohan Das", 3, 4, listOf("Innovation", "Web"))
        seed(7, "Tech Titans", "Karan Singh", 2, 4, listOf("Robotics", "AI"))

        val joinerState = participantStates.getOrPut(1) { ParticipantState() }
        joinerState.incomingInvites += TeamInvite(
            101, "InnovateX", "Rohan Das", 3, 4,
            sentAgo = "2 hrs ago",
            memberNames = listOf("Rohan Das", "Member 6_1", "Member 6_2"),
            teamId = 6,
            participantId = 1,
            tags = teams.getValue(6).tags,
        )
        participantStates[99] = ParticipantState(pendingTeamIds = mutableSetOf(6))
        joinRequests += TeamJoinRequest(nextRequestId++, 6, "Maya Patel", "maya.patel@abc.edu", participantId = 99, program = "MCA · 2027")
    }

    @Synchronized
    fun teamPortalData(): TeamPortalData {
        val state = activeState
        val currentTeam = state.currentTeamId?.let(teams::get)
        val pendingTeams = state.pendingTeamIds.mapNotNull(teams::get)
        val status = when {
            state.currentRole == TeamRole.LEADER -> TeamMembershipStatus.LEADER
            state.currentRole == TeamRole.MEMBER -> TeamMembershipStatus.MEMBER
            pendingTeams.isNotEmpty() -> TeamMembershipStatus.REQUEST_PENDING
            state.incomingInvites.isNotEmpty() -> TeamMembershipStatus.INVITED
            state.lastRequestRejected -> TeamMembershipStatus.REQUEST_REJECTED
            state.lastInviteRejected -> TeamMembershipStatus.INVITE_REJECTED
            else -> TeamMembershipStatus.NONE
        }
        return TeamPortalData(
            teams = teams.values.map(TeamRecord::summary),
            invites = state.incomingInvites.toList(),
            myTeam = currentTeam?.summary(),
            myTeamRole = state.currentRole,
            members = currentTeam?.members?.toList().orEmpty(),
            incomingJoinRequests = if (state.currentRole == TeamRole.LEADER) joinRequests.filter { it.teamId == state.currentTeamId } else emptyList(),
            outgoingInvites = if (state.currentRole == TeamRole.LEADER) sentInvites.filter { it.teamId == state.currentTeamId } else emptyList(),
            membershipStatus = status,
            pendingTeam = pendingTeams.firstOrNull()?.summary(),
            pendingTeamIds = pendingTeams.map { it.id }.toSet(),
            inviteCandidates = if (state.currentRole == TeamRole.LEADER) listOf(1, 3).mapNotNull { id ->
                val candidateState = participantStates[id] ?: ParticipantState()
                if (candidateState.currentTeamId != null || candidateState.pendingTeamIds.isNotEmpty() || candidateState.incomingInvites.isNotEmpty() ||
                    sentInvites.any { it.teamId == state.currentTeamId && it.registrationId == id }) null
                else TeamInviteCandidate(id, id, participantNames.getValue(id), participantEmails.getValue(id), participantPrograms[id].orEmpty())
            } else emptyList(),
        )
    }

    @Synchronized
    fun requestToJoin(teamId: Int) {
        check(activeState.currentTeamId == null) { "You already belong to a team." }
        val team = teams[teamId] ?: error("Team is no longer available.")
        check(team.members.size < team.maxMembers) { "This team is full." }
        check(activeState.incomingInvites.isEmpty()) { "Respond to your pending team invitation first." }
        if (teamId in activeState.pendingTeamIds) return
        check(activeState.pendingTeamIds.isEmpty()) { "You already have a pending request to join another team." }
        activeState.pendingTeamIds += teamId
        activeState.lastRequestRejected = false
        val persona = currentPersona()
        joinRequests += TeamJoinRequest(nextRequestId++, teamId, persona.name, persona.email, participantId = persona.id)
    }

    @Synchronized
    fun respondToInvite(inviteId: Int, accept: Boolean) {
        val invite = activeState.incomingInvites.firstOrNull { it.id == inviteId } ?: error("Invitation is no longer available.")
        if (accept) {
            check(activeState.currentTeamId == null) { "You already belong to a team." }
            val team = teams[invite.teamId] ?: error("Team is no longer available.")
            check(team.members.size < team.maxMembers) { "This team is full." }
            val persona = currentPersona()
            team.members += TeamMember(persona.id, persona.name, persona.email, program = participantPrograms[persona.id].orEmpty())
            activeState.currentTeamId = team.id
            activeState.currentRole = TeamRole.MEMBER
            activeState.pendingTeamIds.clear()
            activeState.lastInviteRejected = false
            joinRequests.removeAll { it.participantId == persona.id }
            activeState.incomingInvites.clear()
            sentInvites.removeAll { it.registrationId == activeParticipantId }
        } else {
            activeState.incomingInvites.remove(invite)
            sentInvites.removeAll { it.registrationId == activeParticipantId && it.teamId == invite.teamId }
            activeState.lastInviteRejected = true
        }
    }

    @Synchronized
    fun createTeam(request: SubmissionRequest): TeamSummary? {
        if (request.context.mode != ParticipationMode.CREATE_TEAM) return null
        check(activeState.currentTeamId == null) { "You already belong to a team." }
        check(activeState.pendingTeamIds.isEmpty() && activeState.incomingInvites.isEmpty()) {
            "Resolve your pending team request or invitation before creating a team."
        }
        val name = request.context.teamName?.trim().orEmpty()
        require(name.length in TEAM_NAME_MIN_LENGTH..TEAM_NAME_MAX_LENGTH) {
            "Team names must be between $TEAM_NAME_MIN_LENGTH and $TEAM_NAME_MAX_LENGTH characters."
        }
        require(teams.values.none { it.name.equals(name, ignoreCase = true) }) { "A team with this name already exists for this event." }
        val id = nextTeamId++
        val tags = request.teamAnswers.values.flatMap { it.split('\n') }.filter(String::isNotBlank).take(3)
        val persona = currentPersona()
        val member = TeamMember(persona.id, persona.name, persona.email, TeamRole.LEADER, participantPrograms[persona.id].orEmpty())
        val team = TeamRecord(id, name, persona.name, 4, tags, mutableListOf(member))
        teams[id] = team
        activeState.currentTeamId = id
        activeState.currentRole = TeamRole.LEADER
        activeState.pendingTeamIds.clear()
        // Demo data gives the leader a real pending request to manage after creation.
        participantStates[98] = ParticipantState(pendingTeamIds = mutableSetOf(id))
        joinRequests += TeamJoinRequest(nextRequestId++, id, "Maya Patel", "maya.patel@abc.edu", participantId = 98, program = "MCA · 2027")
        return team.summary()
    }

    @Synchronized
    fun isTeamNameAvailable(name: String): Boolean = teams.values.none { it.name.equals(name.trim(), ignoreCase = true) }

    @Synchronized
    fun saveRegistration(request: SubmissionRequest, receipt: SubmissionReceipt, teamId: Int?) {
        registrations[receipt.registrationId] = SavedRegistrationDetails(
            registrationId = receipt.registrationId,
            participantId = activeParticipantId,
            receipt = receipt,
            eventName = request.context.eventName,
            mode = request.context.mode,
            participantAnswers = request.context.participantAnswers.toMap(),
            teamAnswers = request.teamAnswers.toMap(),
            teamName = request.context.teamName,
            teamId = teamId ?: request.context.teamId,
        )
        latestRegistrationByParticipant[activeParticipantId] = receipt.registrationId
    }

    @Synchronized
    fun allocateRegistrationId(): Int = nextRegistrationId++

    @Synchronized
    fun registrationDetails(participantId: Int): SavedRegistrationDetails? =
        latestRegistrationByParticipant[participantId]?.let(registrations::get)

    @Synchronized
    fun registrationDetailsById(registrationId: Int): SavedRegistrationDetails? = registrations[registrationId]

    @Synchronized
    fun teamSummary(teamId: Int) = teams[teamId]?.summary()

    @Synchronized
    fun respondToJoinRequest(requestId: Int, accept: Boolean) {
        check(activeState.currentRole == TeamRole.LEADER && activeState.currentTeamId != null) { "Only the team leader can manage requests." }
        val request = joinRequests.firstOrNull { it.id == requestId && it.teamId == currentTeamId }
            ?: error("Join request is no longer available.")
        val team = teams.getValue(request.teamId)
        if (accept) {
            check(team.members.size < team.maxMembers) { "There is no space left on this team." }
            val requester = participantStates.getOrPut(request.participantId) { ParticipantState() }
            check(requester.currentTeamId == null) { "This participant already joined another team." }
            check(team.id in requester.pendingTeamIds) { "This join request is no longer pending." }
            team.members += TeamMember(request.participantId, request.participantName, request.email, program = request.program)
            requester.currentTeamId = team.id
            requester.currentRole = TeamRole.MEMBER
            requester.pendingTeamIds.clear()
            requester.lastRequestRejected = false
            requester.lastInviteRejected = false
            requester.incomingInvites.clear()
            sentInvites.removeAll { it.registrationId == request.participantId }
            joinRequests.removeAll { it.participantId == request.participantId && it.id != request.id }
        } else {
            participantStates[request.participantId]?.let { declined ->
                declined.pendingTeamIds.remove(request.teamId)
                if (declined.pendingTeamIds.isEmpty()) declined.lastRequestRejected = true
            }
        }
        joinRequests.remove(request)
    }

    @Synchronized
    fun cancelJoinRequest(teamId: Int) {
        check(activeState.currentTeamId == null) { "You already belong to a team." }
        if (!activeState.pendingTeamIds.remove(teamId)) return
        joinRequests.removeAll { it.teamId == teamId && it.participantId == activeParticipantId }
        activeState.lastRequestRejected = false
    }

    @Synchronized
    fun inviteMember(registrationId: Int) {
        check(activeState.currentRole == TeamRole.LEADER && activeState.currentTeamId != null) { "Only the team leader can invite members." }
        require(registrationId in participantNames) { "Choose a registered participant." }
        val team = teams.getValue(currentTeamId!!)
        val reserved = sentInvites.count { it.teamId == team.id }
        check(team.members.size + reserved < team.maxMembers) { "There is no space left to invite another member." }
        val target = participantStates.getOrPut(registrationId) { ParticipantState() }
        check(target.currentTeamId == null) { "This participant already belongs to a team." }
        check(target.pendingTeamIds.isEmpty()) { "This participant already has a pending team request." }
        check(target.incomingInvites.isEmpty()) { "This participant already has a pending team invitation." }
        check(sentInvites.none { it.teamId == team.id && it.registrationId == registrationId }) { "An invitation is already pending for this participant." }
        val name = participantNames.getValue(registrationId)
        val email = participantEmails.getValue(registrationId)
        sentInvites += TeamInvitation(nextInviteId++, team.id, email, registrationId = registrationId, participantName = name,
            program = participantPrograms[registrationId].orEmpty())
        target.incomingInvites += TeamInvite(
            id = sentInvites.last().id,
            teamName = team.name,
            invitedBy = currentPersona().name,
            memberCount = team.members.size,
            maxMembers = team.maxMembers,
            sentAgo = "Just now",
            memberNames = team.members.map { it.name },
            teamId = team.id,
            participantId = registrationId,
            tags = team.tags,
        )
        target.lastInviteRejected = false
    }

    @Synchronized
    fun cancelInvitation(invitationId: Int) {
        check(activeState.currentRole == TeamRole.LEADER && activeState.currentTeamId != null) { "Only the team leader can manage invitations." }
        val invite = sentInvites.firstOrNull { it.id == invitationId && it.teamId == currentTeamId }
            ?: error("Invitation is no longer pending.")
        sentInvites.remove(invite)
        participantStates[invite.registrationId]?.incomingInvites?.removeAll { it.id == invite.id }
    }
}
