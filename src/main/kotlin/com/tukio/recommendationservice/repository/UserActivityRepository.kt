package com.tukio.recommendationservice.repository

import com.tukio.recommendationservice.model.ActivityType
import com.tukio.recommendationservice.model.UserActivity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface UserActivityRepository : JpaRepository<UserActivity, Long> {

    fun findByUserId(userId: Long): List<UserActivity>

    fun findByUserIdAndEventId(userId: Long, eventId: Long): List<UserActivity>

    fun findByUserIdAndActivityType(userId: Long, activityType: ActivityType): List<UserActivity>

    fun findByEventId(eventId: Long): List<UserActivity>

    fun findByEventIdAndActivityType(eventId: Long, activityType: ActivityType): List<UserActivity>

    @Query("""
        SELECT a FROM UserActivity a
        WHERE a.userId = :userId
        AND a.activityType = :activityType
        AND a.createdAt >= :since
        ORDER BY a.createdAt DESC
    """)
    fun findRecentActivitiesByUser(
        @Param("userId") userId: Long,
        @Param("activityType") activityType: ActivityType,
        @Param("since") since: LocalDateTime
    ): List<UserActivity>

    @Query("""
        SELECT a.eventId, COUNT(a) as count
        FROM UserActivity a
        WHERE a.activityType = :activityType
        GROUP BY a.eventId
        ORDER BY count DESC
        LIMIT :limit
    """)
    fun findMostPopularEventsByActivity(
        @Param("activityType") activityType: ActivityType,
        @Param("limit") limit: Int
    ): List<Array<Any>>

    @Query("""
        SELECT a.eventId, COUNT(a) as count
        FROM UserActivity a
        WHERE a.activityType = :activityType
        AND a.createdAt >= :since
        GROUP BY a.eventId
        ORDER BY count DESC
        LIMIT :limit
    """)
    fun findTrendingEventsByActivity(
        @Param("activityType") activityType: ActivityType,
        @Param("since") since: LocalDateTime,
        @Param("limit") limit: Int
    ): List<Array<Any>>

    @Query("""
        SELECT AVG(a.rating)
        FROM UserActivity a
        WHERE a.eventId = :eventId
        AND a.activityType = com.tukio.recommendationservice.model.ActivityType.RATE
        AND a.rating IS NOT NULL
    """)
    fun findAverageRatingForEvent(@Param("eventId") eventId: Long): Double?
}