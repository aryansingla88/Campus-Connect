package com.example.campusconnect.feature.events.data

// ─── Medal models ─────────────────────────────────────────────────────────────

enum class MedalType(
    val label    : String,
    val subtitle : String,
    val rank     : Int
) {
    GOLD(   label = "Gold Medal",   subtitle = "Top Performer",     rank = 1),
    SILVER( label = "Silver Medal", subtitle = "Outstanding Work",  rank = 2),
    BRONZE( label = "Bronze Medal", subtitle = "Excellent Effort",  rank = 3)
}

data class MedalAward(
    val eventId     : Int,
    val medalType   : MedalType,
    val recipientId : Int,           // id from UserAccess / SoloParticipant / TeamMember
    val recipientName : String,
    val recipientSubtitle : String,  // "MCA 2nd Year" / "Team Alpha" etc
    val isTeam      : Boolean = false
)

// ─── FakeMedalService ─────────────────────────────────────────────────────────

class FakeMedalService {

    // In-memory store: eventId → list of awards (max 3 per event)
    private val awards = mutableListOf<MedalAward>()

    /** Award a medal. Replaces any existing award of the same type for this event. */
    fun awardMedal(award: MedalAward) {
        awards.removeAll { it.eventId == award.eventId && it.medalType == award.medalType }
        awards.add(award)
    }

    /** Remove a medal award (un-award). */
    fun removeAward(eventId: Int, medalType: MedalType) {
        awards.removeAll { it.eventId == eventId && it.medalType == medalType }
    }

    /** Get all awards for a specific event. */
    fun getAwardsForEvent(eventId: Int): List<MedalAward> =
        awards.filter { it.eventId == eventId }

    /** Get awards for a specific recipient across all events. */
    fun getAwardsForRecipient(recipientId: Int): List<MedalAward> =
        awards.filter { it.recipientId == recipientId }

    /** Check if a specific medal type is already awarded for an event. */
    fun isAwarded(eventId: Int, medalType: MedalType): Boolean =
        awards.any { it.eventId == eventId && it.medalType == medalType }
}