package com.example.campusconnect.feature.events.components

// ─── Models ───────────────────────────────────────────────────────────────────

data class TeamMember(
    val id       : Int,
    val name     : String,
    val subtitle : String,          // e.g. "MCA 2nd Year"
    val isLeader : Boolean = false,
    val initials : String = name
        .split(" ").take(2).joinToString("") { it.take(1).uppercase() }
)

data class ParticipantTeam(
    val id      : Int,
    val name    : String,
    val members : List<TeamMember>
)

data class SoloParticipant(
    val id       : Int,
    val name     : String,
    val subtitle : String,
    val initials : String = name
        .split(" ").take(2).joinToString("") { it.take(1).uppercase() }
)

// ─── FakeParticipantsService ──────────────────────────────────────────────────

class FakeParticipantsService {

    private val allTeams = listOf(
        ParticipantTeam(
            id = 1, name = "Team Alpha",
            members = listOf(
                TeamMember(1,  "Rahul Kumar",  "MCA 2nd Year", isLeader = true),
                TeamMember(2,  "Priya Sharma", "MCA 3rd Year"),
                TeamMember(3,  "Amit Singh",   "BCA 1st Year"),
                TeamMember(4,  "Sneha Nair",   "MCA 1st Year")
            )
        ),
        ParticipantTeam(
            id = 2, name = "Team Beta",
            members = listOf(
                TeamMember(5,  "Karan Mehta",  "MCA 2nd Year", isLeader = true),
                TeamMember(6,  "Divya Reddy",  "BCA 2nd Year"),
                TeamMember(7,  "Nikhil Joshi", "BCA 1st Year")
            )
        ),
        ParticipantTeam(
            id = 3, name = "Team Gamma",
            members = listOf(
                TeamMember(8,  "Ananya Gupta",    "MCA 1st Year", isLeader = true),
                TeamMember(9,  "Vikram Tiwari",   "BCA 2nd Year"),
                TeamMember(10, "Simran Kaur",     "BCA 1st Year"),
                TeamMember(11, "Ishaan Malhotra", "MCA 2nd Year")
            )
        ),
        ParticipantTeam(
            id = 4, name = "Team Delta",
            members = listOf(
                TeamMember(12, "Arjun Yadav",  "MCA 1st Year", isLeader = true),
                TeamMember(13, "Meera Pillai", "BCA 3rd Year"),
                TeamMember(14, "Ravi Shankar", "MCA 3rd Year")
            )
        ),
        ParticipantTeam(
            id = 5, name = "Team Echo",
            members = listOf(
                TeamMember(15, "Tanya Bose",   "MCA 2nd Year", isLeader = true),
                TeamMember(16, "Sameer Iyer",  "BCA 1st Year"),
                TeamMember(17, "Komal Desai",  "MCA 1st Year"),
                TeamMember(18, "Harsh Pandey", "BCA 2nd Year")
            )
        )
    )

    private val allSolo = listOf(
        SoloParticipant(1, "Pooja Verma",   "MCA 2nd Year"),
        SoloParticipant(2, "Rohan Nair",    "BCA 3rd Year"),
        SoloParticipant(3, "Neha Kapoor",   "MCA 1st Year"),
        SoloParticipant(4, "Yash Malhotra", "BCA 2nd Year"),
        SoloParticipant(5, "Ritika Jain",   "MCA 3rd Year"),
        SoloParticipant(6, "Siddharth Rao", "BCA 1st Year"),
        SoloParticipant(7, "Anika Sharma",  "MCA 2nd Year"),
        SoloParticipant(8, "Dev Patel",     "BCA 3rd Year")
    )

    fun getTeams(eventId: Int): List<ParticipantTeam> {
        val count = (eventId % 3) + 1
        val start = (eventId % allTeams.size)
        return allTeams.drop(start).take(count).ifEmpty { allTeams.take(count) }
    }

    fun getSoloParticipants(eventId: Int): List<SoloParticipant> {
        val count = (eventId % 4) + 2
        val start = (eventId % allSolo.size)
        return allSolo.drop(start).take(count).ifEmpty { allSolo.take(count) }
    }

    fun getTotalCount(eventId: Int): Int {
        return getTeams(eventId).sumOf { it.members.size } + getSoloParticipants(eventId).size
    }
}