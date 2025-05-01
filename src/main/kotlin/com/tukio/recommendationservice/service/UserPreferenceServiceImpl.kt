package com.tukio.recommendationservice.service

import com.tukio.recommendationservice.dto.UserPreferenceDTO
import com.tukio.recommendationservice.model.ActivityType
import com.tukio.recommendationservice.model.UserPreference
import com.tukio.recommendationservice.repository.UserActivityRepository
import com.tukio.recommendationservice.repository.UserPreferenceRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class UserPreferenceServiceImpl(
    private val userPreferenceRepository: UserPreferenceRepository,
    private val userActivityRepository: UserActivityRepository
) : UserPreferenceService {

    private val logger = LoggerFactory.getLogger(UserPreferenceServiceImpl::class.java)

    override fun getUserPreferences(userId: Long): List<UserPreferenceDTO> {
        return userPreferenceRepository.findByUserId(userId).map { it.toDTO() }
    }

    override fun getUserPreferenceForCategory(userId: Long, categoryId: Long): UserPreferenceDTO? {
        return userPreferenceRepository.findByUserIdAndCategoryId(userId, categoryId)?.toDTO()
    }

    @Transactional
    override fun updateUserPreference(preferenceDTO: UserPreferenceDTO): UserPreferenceDTO {
        // Find existing preference or create new one
        val preference = userPreferenceRepository.findByUserIdAndCategoryId(
            preferenceDTO.userId,
            preferenceDTO.categoryId
        ) ?: UserPreference(
            userId = preferenceDTO.userId,
            categoryId = preferenceDTO.categoryId,
            preferenceScore = 0.0
        )

        // Update fields
        preference.preferenceScore = preferenceDTO.preferenceScore

        preferenceDTO.tags?.let {
            preference.tags.clear()
            preference.tags.addAll(it)
        }

        preferenceDTO.preferredLocation?.let { preference.preferredLocation = it }
        preferenceDTO.preferredDayOfWeek?.let { preference.preferredDayOfWeek = it }
        preferenceDTO.preferredTimeOfDay?.let { preference.preferredTimeOfDay = it }

        preference.updatedAt = LocalDateTime.now()

        val savedPreference = userPreferenceRepository.save(preference)
        logger.info("Updated user preference: ${preference.userId} - ${preference.categoryId} - ${preference.preferenceScore}")

        return savedPreference.toDTO()
    }

    override fun getUserPreferencesByTags(userId: Long, tags: Set<String>): List<UserPreferenceDTO> {
        return userPreferenceRepository.findUserPreferencesByTags(userId, tags).map { it.toDTO() }
    }

    override fun getFavoriteCategories(userId: Long, limit: Int): List<Long> {
        return userPreferenceRepository.findUserPreferencesByScoreDesc(userId)
            .take(limit)
            .map { it.categoryId }
    }

    override fun findSimilarUsersByInterests(userId: Long, minSimilarityScore: Double): List<Long> {
        // Get user's preferences
        val userPreferences = userPreferenceRepository.findByUserId(userId)
        if (userPreferences.isEmpty()) {
            return emptyList()
        }

        // Extract tags from all preferences
        val userTags = userPreferences.flatMap { it.tags }.toSet()
        if (userTags.isEmpty()) {
            return emptyList()
        }

        // Find users with similar tags
        val minTagMatches = (userTags.size * 0.3).toLong() // Require at least 30% tag match
        return userPreferenceRepository.findSimilarUsersByTags(userTags, minSimilarityScore, minTagMatches)
            .filter { it != userId } // Exclude the user themselves
    }

    @Transactional
    override fun updatePreferencesBasedOnActivity(userId: Long): List<UserPreferenceDTO> {
        // Get user's recent activities
        val recentActivities = userActivityRepository.findByUserId(userId)
        if (recentActivities.isEmpty()) {
            return emptyList()
        }

        // Group activities by event ID to get event information
        val eventActivities = recentActivities.groupBy { it.eventId }

        // Update preferences based on activities (simplified example)
        val updatedPreferences = mutableListOf<UserPreference>()

        // In a real implementation, we would:
        // 1. Get event details from event service to know categories
        // 2. Calculate scores based on activity types (view, register, rate, etc.)
        // 3. Update preference scores accordingly

        // Simplified implementation assuming we have category information
        val mockCategoryMap = mapOf(
            1L to 1L, // EventId to CategoryId mapping
            2L to 2L,
            3L to 1L
        )

        val activityScores = mapOf(
            ActivityType.VIEW to 0.1,
            ActivityType.REGISTER to 0.3,
            ActivityType.ATTEND to 0.5,
            ActivityType.RATE to 0.2,
            ActivityType.SHARE to 0.4,
            ActivityType.FAVORITE to 0.6,
            ActivityType.CANCEL to -0.3
        )

        // Process each event the user interacted with
        for ((eventId, activities) in eventActivities) {
            // Mock category for the event
            val categoryId = mockCategoryMap[eventId] ?: continue

            // Find or create preference for this category
            val preference = userPreferenceRepository.findByUserIdAndCategoryId(userId, categoryId)
                ?: UserPreference(
                    userId = userId,
                    categoryId = categoryId,
                    preferenceScore = 0.0
                )

            // Calculate score adjustment based on activities
            var scoreAdjustment = 0.0
            for (activity in activities) {
                scoreAdjustment += activityScores[activity.activityType] ?: 0.0

                // Add bonus for high ratings
                if (activity.activityType == ActivityType.RATE && activity.rating != null) {
                    scoreAdjustment += (activity.rating - 3) * 0.1 // Scale rating impact
                }
            }

            // Apply score adjustment (bounded between 0 and 1)
            preference.preferenceScore = (preference.preferenceScore + scoreAdjustment).coerceIn(0.0, 1.0)
            preference.updatedAt = LocalDateTime.now()

            updatedPreferences.add(userPreferenceRepository.save(preference))
        }

        return updatedPreferences.map { it.toDTO() }
    }

    // Extension function to convert UserPreference to UserPreferenceDTO
    private fun UserPreference.toDTO(): UserPreferenceDTO {
        return UserPreferenceDTO(
            userId = this.userId,
            categoryId = this.categoryId,
            preferenceScore = this.preferenceScore,
            tags = this.tags,
            preferredLocation = this.preferredLocation,
            preferredDayOfWeek = this.preferredDayOfWeek,
            preferredTimeOfDay = this.preferredTimeOfDay
        )
    }
}