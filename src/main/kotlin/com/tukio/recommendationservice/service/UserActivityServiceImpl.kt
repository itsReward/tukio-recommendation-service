package com.tukio.recommendationservice.service

import com.tukio.recommendationservice.dto.UserActivityDTO
import com.tukio.recommendationservice.model.ActivityType
import com.tukio.recommendationservice.model.UserActivity
import com.tukio.recommendationservice.repository.UserActivityRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class UserActivityServiceImpl(
    private val userActivityRepository: UserActivityRepository
) : UserActivityService {

    private val logger = LoggerFactory.getLogger(UserActivityServiceImpl::class.java)

    @Transactional
    override fun recordUserActivity(userActivityDTO: UserActivityDTO) {
        val userActivity = UserActivity(
            userId = userActivityDTO.userId,
            eventId = userActivityDTO.eventId,
            activityType = userActivityDTO.activityType,
            rating = userActivityDTO.rating,
            viewDuration = userActivityDTO.viewDuration
        )

        userActivityRepository.save(userActivity)
        logger.info("Recorded user activity: ${userActivity.userId} - ${userActivity.eventId} - ${userActivity.activityType}")
    }

    override fun getUserActivities(userId: Long): List<UserActivityDTO> {
        return userActivityRepository.findByUserId(userId).map { it.toDTO() }
    }

    override fun getUserActivitiesByType(userId: Long, activityType: ActivityType): List<UserActivityDTO> {
        return userActivityRepository.findByUserIdAndActivityType(userId, activityType).map { it.toDTO() }
    }

    override fun getEventActivities(eventId: Long): List<UserActivityDTO> {
        return userActivityRepository.findByEventId(eventId).map { it.toDTO() }
    }

    override fun getEventActivitiesByType(eventId: Long, activityType: ActivityType): List<UserActivityDTO> {
        return userActivityRepository.findByEventIdAndActivityType(eventId, activityType).map { it.toDTO() }
    }

    override fun getAverageRatingForEvent(eventId: Long): Double? {
        return userActivityRepository.findAverageRatingForEvent(eventId)
    }

    override fun getMostPopularEvents(activityType: ActivityType, limit: Int): List<Long> {
        return userActivityRepository.findMostPopularEventsByActivity(activityType, limit)
            .map { (it[0] as Long) }
    }

    override fun getTrendingEvents(activityType: ActivityType, daysAgo: Int, limit: Int): List<Long> {
        val since = LocalDateTime.now().minusDays(daysAgo.toLong())
        return userActivityRepository.findTrendingEventsByActivity(activityType, since, limit)
            .map { (it[0] as Long) }
    }

    // Extension function to convert UserActivity to UserActivityDTO
    private fun UserActivity.toDTO(): UserActivityDTO {
        return UserActivityDTO(
            userId = this.userId,
            eventId = this.eventId,
            activityType = this.activityType,
            rating = this.rating,
            viewDuration = this.viewDuration
        )
    }
}