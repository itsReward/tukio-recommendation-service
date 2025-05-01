package com.tukio.recommendationservice.repository

import com.tukio.recommendationservice.model.EventSimilarity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface EventSimilarityRepository : JpaRepository<EventSimilarity, Long> {

    fun findByEventId1AndEventId2(eventId1: Long, eventId2: Long): EventSimilarity?

    @Query("""
        SELECT es FROM EventSimilarity es
        WHERE (es.eventId1 = :eventId OR es.eventId2 = :eventId)
        AND es.similarityScore >= :minScore
        ORDER BY es.similarityScore DESC
    """)
    fun findSimilarEvents(
        @Param("eventId") eventId: Long,
        @Param("minScore") minScore: Double
    ): List<EventSimilarity>

    @Query("""
        SELECT es FROM EventSimilarity es
        WHERE (es.eventId1 = :eventId OR es.eventId2 = :eventId)
        AND es.similarityScore >= :minScore
        AND es.lastCalculated >= :since
        ORDER BY es.similarityScore DESC
        LIMIT :limit
    """)
    fun findRecentSimilarEvents(
        @Param("eventId") eventId: Long,
        @Param("minScore") minScore: Double,
        @Param("since") since: LocalDateTime,
        @Param("limit") limit: Int
    ): List<EventSimilarity>

    @Query("""
        SELECT CASE
            WHEN es.eventId1 = :eventId THEN es.eventId2
            ELSE es.eventId1
        END as otherEventId,
        es.similarityScore
        FROM EventSimilarity es
        WHERE (es.eventId1 = :eventId OR es.eventId2 = :eventId)
        AND es.similarityScore >= :minScore
        ORDER BY es.similarityScore DESC
        LIMIT :limit
    """)
    fun findSimilarEventIds(
        @Param("eventId") eventId: Long,
        @Param("minScore") minScore: Double,
        @Param("limit") limit: Int
    ): List<Array<Any>>
}