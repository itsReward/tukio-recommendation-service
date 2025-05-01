package com.tukio.recommendationservice.service

import com.tukio.recommendationservice.dto.UserActivityDTO
import com.tukio.recommendationservice.model.ActivityType

interface UserActivityService {

    fun recordUserActivity(userActivityDTO: UserActivityDTO)

    fun getUserActivities(userId: Long): List<UserActivityDTO>

    fun getUserActivitiesByType(userId: Long, activityType: ActivityType): List<UserActivityDTO>

    fun getEventActivities(eventId: Long): List<UserActivityDTO>

    fun getEventActivitiesByType(eventId: Long, activityType: ActivityType): List<UserActivityDTO>

    fun getAverageRatingForEvent(eventId: Long): Double?

    fun getMostPopularEvents(activityType: ActivityType, limit: Int): List<Long>

    fun getTrendingEvents(activityType: ActivityType, daysAgo: Int, limit: Int): List<Long>
}