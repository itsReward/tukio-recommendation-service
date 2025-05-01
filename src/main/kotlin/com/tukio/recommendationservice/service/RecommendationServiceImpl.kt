package com.tukio.recommendationservice.service

import com.tukio.recommendationservice.client.EventServiceClient
import com.tukio.recommendationservice.client.UserServiceClient
import com.tukio.recommendationservice.dto.*
import com.tukio.recommendationservice.model.ActivityType
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.DayOfWeek

@Service
class RecommendationServiceImpl(
    private val userActivityService: UserActivityService,
    private val userPreferenceService: UserPreferenceService,
    private val eventSimilarityService: EventSimilarityService,
    private val eventServiceClient: EventServiceClient,
    private val userServiceClient: UserServiceClient
) : RecommendationService {

    private val logger = LoggerFactory.getLogger(RecommendationServiceImpl::class.java)

    override fun getRecommendations(request: RecommendationRequest): RecommendationResponse {
        logger.info("Generating recommendations for user ${request.userId}")

        // Initialize lists for upcoming and past recommendations
        val upcomingRecommendations = mutableListOf<EventRecommendationDTO>()
        val pastRecommendations = mutableListOf<EventRecommendationDTO>()

        try {
            // Get event recommendations based on request parameters
            val recommendedEventIds = mutableSetOf<Long>()

            // Get personalized recommendations
            if (request.includePersonalizedRecommendations) {
                val personalizedIds = getPersonalizedRecommendations(request.userId, request.count)
                recommendedEventIds.addAll(personalizedIds)
            }

            // Get similar events recommendations
            if (request.includeSimilarEvents) {
                val similarEventIds = getSimilarEventsRecommendations(request.userId, request.count)
                recommendedEventIds.addAll(similarEventIds)
            }

            // Get popular events
            if (request.includePopularEvents) {
                val popularEventIds = getPopularEventsRecommendations(request.count)
                recommendedEventIds.addAll(popularEventIds)
            }

            // Get trending events
            val trendingEventIds = getTrendingEventsRecommendations(request.count / 2)
            recommendedEventIds.addAll(trendingEventIds)

            // Fetch event details
            val recommendedEvents = recommendedEventIds.mapNotNull { eventId ->
                try {
                    eventServiceClient.getEventById(eventId)
                } catch (e: Exception) {
                    logger.error("Failed to fetch event $eventId: ${e.message}")
                    null
                }
            }

            // Split into upcoming and past events
            val now = LocalDateTime.now()
            val (upcoming, past) = recommendedEvents.partition {
                it.startTime.isAfter(now)
            }

            // Convert to DTOs with recommendation types
            if (request.includeUpcoming) {
                upcomingRecommendations.addAll(upcoming.map { it.toRecommendationDTO() })
            }

            if (request.includePast) {
                pastRecommendations.addAll(past.map { it.toRecommendationDTO() })
            }

        } catch (e: Exception) {
            logger.error("Error generating recommendations: ${e.message}", e)
        }

        return RecommendationResponse(
            userId = request.userId,
            upcomingRecommendations = upcomingRecommendations,
            pastRecommendations = pastRecommendations,
            totalRecommendations = upcomingRecommendations.size + pastRecommendations.size
        )
    }

    override fun getPersonalizedRecommendations(userId: Long, count: Int): List<Long> {
        // Get user preferences
        val userPreferences = userPreferenceService.getUserPreferences(userId)
        if (userPreferences.isEmpty()) {
            return emptyList()
        }

        // Get top categories based on preference scores
        val topCategories = userPreferences
            .sortedByDescending { it.preferenceScore }
            .take(3)
            .map { it.categoryId }

        // Get events from these categories
        val recommendedEvents = mutableListOf<Long>()
        for (categoryId in topCategories) {
            try {
                val events = eventServiceClient.getEventsByCategory(categoryId)
                    .map { it.id }
                    .take((count / topCategories.size) + 1)

                recommendedEvents.addAll(events)
            } catch (e: Exception) {
                logger.error("Failed to fetch events for category $categoryId: ${e.message}")
            }
        }

        return recommendedEvents.take(count)
    }

    override fun getSimilarEventsRecommendations(userId: Long, count: Int): List<Long> {
        // Get events the user has engaged with
        val userAttendedEvents = userActivityService.getUserActivitiesByType(userId, ActivityType.ATTEND)
            .map { it.eventId }

        val userRatedEvents = userActivityService.getUserActivitiesByType(userId, ActivityType.RATE)
            .filter { it.rating != null && it.rating >= 4 } // Only consider highly rated events
            .map { it.eventId }

        val userFavoritedEvents = userActivityService.getUserActivitiesByType(userId, ActivityType.FAVORITE)
            .map { it.eventId }

        // Combine all events the user has shown positive interest in
        val userLikedEvents = (userAttendedEvents + userRatedEvents + userFavoritedEvents).distinct()

        if (userLikedEvents.isEmpty()) {
            return emptyList()
        }

        // Find similar events for each liked event
        val similarEvents = mutableMapOf<Long, Double>() // EventId to similarity score

        for (eventId in userLikedEvents) {
            val similar = eventSimilarityService.findSimilarEvents(eventId, 0.5, 5)

            // Merge into overall similar events, keeping highest similarity score
            for ((similarEventId, score) in similar) {
                val currentScore = similarEvents[similarEventId] ?: 0.0
                if (score > currentScore) {
                    similarEvents[similarEventId] = score
                }
            }
        }

        // Sort by similarity score and return top events
        return similarEvents.entries
            .sortedByDescending { it.value }
            .map { it.key }
            .take(count)
    }

    override fun getPopularEventsRecommendations(count: Int): List<Long> {
        // Get most registered events
        val popularRegisteredEvents = userActivityService.getMostPopularEvents(ActivityType.REGISTER, count)

        // Get most attended events
        val popularAttendedEvents = userActivityService.getMostPopularEvents(ActivityType.ATTEND, count)

        // Combine and deduplicate
        return (popularRegisteredEvents + popularAttendedEvents).distinct().take(count)
    }

    override fun getTrendingEventsRecommendations(count: Int): List<Long> {
        // Get events that are trending in the last 7 days
        val trendingRegisteredEvents = userActivityService.getTrendingEvents(ActivityType.REGISTER, 7, count)

        // Get events that are trending in views
        val trendingViewedEvents = userActivityService.getTrendingEvents(ActivityType.VIEW, 7, count)

        // Combine and deduplicate
        return (trendingRegisteredEvents + trendingViewedEvents).distinct().take(count)
    }

    override fun getLocationBasedRecommendations(userId: Long, count: Int): List<Long> {
        // Get user preferences
        val userPreferences = userPreferenceService.getUserPreferences(userId)

        // Extract location preferences
        val preferredLocations = userPreferences
            .mapNotNull { it.preferredLocation }
            .distinct()

        if (preferredLocations.isEmpty()) {
            return emptyList()
        }

        // Get events from these locations (mock implementation)
        // In a real implementation, we would call the event service with location filters
        val recommendedEvents = mutableListOf<Long>()

        try {
            // Mock: Use keyword search with location
            for (location in preferredLocations) {
                val events = eventServiceClient.searchEvents(null, location, null)
                    .map { it.id }
                    .take(count / preferredLocations.size + 1)

                recommendedEvents.addAll(events)
            }
        } catch (e: Exception) {
            logger.error("Failed to fetch events by location: ${e.message}")
        }

        return recommendedEvents.distinct().take(count)
    }

    override fun getTimeBasedRecommendations(userId: Long, count: Int): List<Long> {
        // Get user preferences
        val userPreferences = userPreferenceService.getUserPreferences(userId)

        // Extract time preferences
        val preferredDays = userPreferences
            .mapNotNull { it.preferredDayOfWeek }
            .distinct()
            .map { DayOfWeek.of(it) }

        val preferredTimes = userPreferences
            .mapNotNull { it.preferredTimeOfDay }
            .distinct()

        if (preferredDays.isEmpty() && preferredTimes.isEmpty()) {
            return emptyList()
        }

        // Get upcoming events
        val upcomingEvents = try {
            eventServiceClient.getUpcomingEvents()
        } catch (e: Exception) {
            logger.error("Failed to fetch upcoming events: ${e.message}")
            emptyList()
        }

        // Filter events based on day and time preferences
        val filteredEvents = upcomingEvents.filter { event ->
            val eventDay = event.startTime.dayOfWeek
            val eventHour = event.startTime.hour

            val matchesDay = preferredDays.isEmpty() || preferredDays.contains(eventDay)
            val matchesTime = preferredTimes.isEmpty() || matchesPreferredTime(eventHour, preferredTimes)

            matchesDay && matchesTime
        }

        return filteredEvents.map { it.id }.take(count)
    }

    // Helper methods
    private fun matchesPreferredTime(hour: Int, preferredTimes: List<String>): Boolean {
        return preferredTimes.any { timeRange ->
            when (timeRange.uppercase()) {
                "MORNING" -> hour in 5..11
                "AFTERNOON" -> hour in 12..17
                "EVENING" -> hour in 18..23
                else -> false
            }
        }
    }

    private fun EventDTO.toRecommendationDTO(): EventRecommendationDTO {
        // Determine recommendation type (simplified implementation)
        val recommendationType = when {
            // Implementation would determine the best type based on various factors
            this.currentRegistrations > 100 -> RecommendationType.POPULAR
            else -> RecommendationType.PERSONALIZED
        }

        return EventRecommendationDTO(
            eventId = this.id,
            title = this.title,
            description = this.description,
            categoryId = this.categoryId,
            categoryName = this.categoryName,
            startTime = this.startTime,
            location = this.location,
            venueId = this.venueId,
            venueName = this.venueName,
            imageUrl = this.imageUrl,
            tags = this.tags,
            registrationCount = this.currentRegistrations,
            recommendationType = recommendationType,
            similarityScore = null // Would be filled in for SIMILAR_EVENTS type
        )
    }
}