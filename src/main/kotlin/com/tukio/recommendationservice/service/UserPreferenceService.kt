package com.tukio.recommendationservice.service

import com.tukio.recommendationservice.dto.UserPreferenceDTO

interface UserPreferenceService {

    fun getUserPreferences(userId: Long): List<UserPreferenceDTO>

    fun getUserPreferenceForCategory(userId: Long, categoryId: Long): UserPreferenceDTO?

    fun updateUserPreference(preferenceDTO: UserPreferenceDTO): UserPreferenceDTO

    fun getUserPreferencesByTags(userId: Long, tags: Set<String>): List<UserPreferenceDTO>

    fun getFavoriteCategories(userId: Long, limit: Int): List<Long>

    fun findSimilarUsersByInterests(userId: Long, minSimilarityScore: Double): List<Long>

    fun updatePreferencesBasedOnActivity(userId: Long): List<UserPreferenceDTO>
}