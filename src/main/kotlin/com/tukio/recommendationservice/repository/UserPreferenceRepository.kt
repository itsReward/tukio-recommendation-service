package com.tukio.recommendationservice.repository

import com.tukio.recommendationservice.model.UserPreference
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface UserPreferenceRepository : JpaRepository<UserPreference, Long> {

    fun findByUserId(userId: Long): List<UserPreference>

    fun findByUserIdAndCategoryId(userId: Long, categoryId: Long): UserPreference?

    @Query("""
        SELECT p FROM UserPreference p
        WHERE p.userId = :userId
        ORDER BY p.preferenceScore DESC
    """)
    fun findUserPreferencesByScoreDesc(@Param("userId") userId: Long): List<UserPreference>

    @Query("""
        SELECT p FROM UserPreference p
        JOIN p.tags t
        WHERE p.userId = :userId
        AND t IN :tags
        GROUP BY p.id
        HAVING COUNT(DISTINCT t) > 0
        ORDER BY p.preferenceScore DESC
    """)
    fun findUserPreferencesByTags(
        @Param("userId") userId: Long,
        @Param("tags") tags: Set<String>
    ): List<UserPreference>

    @Query("""
        SELECT DISTINCT p.userId FROM UserPreference p
        JOIN p.tags t
        WHERE t IN :tags
        AND p.preferenceScore >= :minScore
        GROUP BY p.userId
        HAVING COUNT(DISTINCT t) >= :minTagMatches
    """)
    fun findSimilarUsersByTags(
        @Param("tags") tags: Set<String>,
        @Param("minScore") minScore: Double,
        @Param("minTagMatches") minTagMatches: Long
    ): List<Long>
}