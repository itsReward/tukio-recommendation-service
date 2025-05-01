package com.tukio.recommendationservice.dto

import com.tukio.recommendationservice.model.ActivityType
import java.time.LocalDateTime

data class UserActivityDTO(
    val userId: Long,
    val eventId: Long,
    val activityType: ActivityType,
    val rating: Int? = null,
    val viewDuration: Int? = null
)

data class UserPreferenceDTO(
    val userId: Long,
    val categoryId: Long,
    val preferenceScore: Double,
    val tags: Set<String>? = null,
    val preferredLocation: String? = null,
    val preferredDayOfWeek: Int? = null,
    val preferredTimeOfDay: String? = null
)

data class RecommendationRequest(
    val userId: Long,
    val count: Int = 5,
    val includeUpcoming: Boolean = true,
    val includePast: Boolean = false,
    val includeSimilarEvents: Boolean = true,
    val includePopularEvents: Boolean = true,
    val includePersonalizedRecommendations: Boolean = true
)

data class EventRecommendationDTO(
    val eventId: Long,
    val title: String,
    val description: String,
    val categoryId: Long,
    val categoryName: String,
    val startTime: LocalDateTime,
    val location: String,
    val venueId: Long?,
    val venueName: String?,
    val imageUrl: String?,
    val tags: Set<String>,
    val registrationCount: Int,
    val recommendationType: RecommendationType,
    val similarityScore: Double? = null
)

data class RecommendationResponse(
    val userId: Long,
    val upcomingRecommendations: List<EventRecommendationDTO> = emptyList(),
    val pastRecommendations: List<EventRecommendationDTO> = emptyList(),
    val recommendationTimestamp: LocalDateTime = LocalDateTime.now(),
    val totalRecommendations: Int
)

enum class RecommendationType {
    PERSONALIZED,        // Based on user preferences and past behavior
    SIMILAR_EVENTS,      // Similar to events the user liked
    POPULAR,             // Popular events on campus
    TRENDING,            // Trending events (recent increase in popularity)
    LOCATION_BASED,      // Based on user's preferred location
    TIME_BASED           // Based on user's preferred time/day
}