package com.campus.Campus_Connect.features.event.service.priority;

import com.campus.Campus_Connect.features.event.entity.Event;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class PriorityCalculatorService {

    // weights (Total = 100 Points)
    private static final double WEIGHT_REGISTRATIONS = 35.0;
    private static final double WEIGHT_UPVOTES = 20.0;
    private static final double WEIGHT_COMMENTS = 15.0;
    private static final double WEIGHT_REPLIES = 10.0;
    private static final double WEIGHT_FRESHNESS = 10.0;
    private static final double WEIGHT_TIME_URGENCY = 10.0;

    public void calculateAndUpdatePriority(Event event) {
        // --- STAGE 1: FACTOR POINTS CALCULATION ---

        // 1. Registrations Score (Max 35)
        // Note: Agar abhi Event entity me 'capacity' ya 'registrationsCount' ka field nahi hai,
        // toh tum default/dummy value ya DB query laga sakte ho. Assume capacity = 100
        double registrationCount = 0; // Replace with actual: event.getRegistrationsCount()
        double capacity = 100.0; // Isko event ki actual capacity se replace karna future me
        double registrationScore = (registrationCount / capacity) * WEIGHT_REGISTRATIONS;
        if (registrationScore > WEIGHT_REGISTRATIONS) registrationScore = WEIGHT_REGISTRATIONS;

        // 2. Upvotes Score (Max 20)
        double upvotesCount = 0; // Replace with actual: event.getUpvotesCount()
        double upvotesScore = (upvotesCount / 50.0) * WEIGHT_UPVOTES; // Assuming 50 is viral
        if (upvotesScore > WEIGHT_UPVOTES) upvotesScore = WEIGHT_UPVOTES;

        // 3. Comments Score (Max 15)
        double commentsCount = 0; // Replace with actual: event.getCommentsCount()
        double commentsScore = (commentsCount / 20.0) * WEIGHT_COMMENTS;
        if (commentsScore > WEIGHT_COMMENTS) commentsScore = WEIGHT_COMMENTS;

        // 4. Replies Score (Max 10)
        double repliesCount = 0; // Replace with actual: event.getRepliesCount()
        double repliesScore = (repliesCount / 30.0) * WEIGHT_REPLIES;
        if (repliesScore > WEIGHT_REPLIES) repliesScore = WEIGHT_REPLIES;

        // 5. Freshness Score (Max 10) - Naye event ko zyada points
        long daysSinceCreation = ChronoUnit.DAYS.between(event.getCreatedAt(), Instant.now());
        double freshnessScore = calculateFreshnessScore(daysSinceCreation);

        // 6. Time Urgency (Max 10) - Jo event paas hai usko zyada points
        long hoursToStart = ChronoUnit.HOURS.between(Instant.now(), event.getStartTime());
        double timeUrgencyScore = calculateTimeUrgencyScore(hoursToStart);

        // --- STAGE 2: TOTAL PRIORITY SCORE ---
        double totalScore = registrationScore + upvotesScore + commentsScore
                + repliesScore + freshnessScore + timeUrgencyScore;

        event.setPriorityScore(totalScore);

        // --- STAGE 3: PRIORITY LEVEL (RANK) ASSIGNMENT ---
        int newLevel = determineLevelFromScore(totalScore);

        // THE CAPPING RULE: Agar pehle se Level 3 ya usse upar tha (Headstart / Earned),
        // toh kabhi 1 ya 2 par nahi girega. Minimum 3 par cap ho jayega.
        if (event.getBasePriorityLevel() >= 3 || event.getPriorityLevel() >= 3) {
            if (newLevel < 3) {
                newLevel = 3;
            }
        }

        // Final Assignment
        event.setPriorityLevel(newLevel);
    }

    // --- Helper Methods for Maths ---

    private double calculateFreshnessScore(long daysSinceCreation) {
        if (daysSinceCreation <= 1) return WEIGHT_FRESHNESS;      // 0-1 day = 10 pts
        if (daysSinceCreation <= 3) return 8.0;                   // 2-3 days = 8 pts
        if (daysSinceCreation <= 5) return 6.0;                   // 4-5 days = 6 pts
        if (daysSinceCreation <= 7) return 4.0;                   // 6-7 days = 4 pts
        return 0.0;
    }

    private double calculateTimeUrgencyScore(long hoursToStart) {
        if (hoursToStart < 0) return 0.0; // Event already started
        if (hoursToStart <= 24) return WEIGHT_TIME_URGENCY;       // Within 24 hours = 10 pts
        if (hoursToStart <= 48) return 8.0;                       // 2 days = 8 pts
        if (hoursToStart <= 120) return 6.0;                      // 3-5 days = 6 pts
        if (hoursToStart <= 168) return 4.0;                      // 1 week = 4 pts
        return 0.0;
    }

    private int determineLevelFromScore(double score) {
        // In values ko hum future me tune kar sakte hain testing ke baad
        if (score >= 85) return 6;
        if (score >= 70) return 5;
        if (score >= 55) return 4;
        if (score >= 40) return 3;
        if (score >= 25) return 2;
        return 1; // Default fallback
    }
}