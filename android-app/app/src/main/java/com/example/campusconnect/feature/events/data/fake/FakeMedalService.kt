package com.example.campusconnect.feature.events.data.fake

import com.example.campusconnect.feature.events.model.MedalAward
import com.example.campusconnect.feature.events.model.MedalType

object FakeMedalService {

    private val awards = mutableListOf<MedalAward>()

    /** Award a medal. Replaces any existing award of the same type for this event. */
    fun awardMedal(award: MedalAward) {
        awards.removeAll { it.eventId == award.eventId && it.medalType == award.medalType }
        awards.add(award)
    }

    /** Remove a medal award. */
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