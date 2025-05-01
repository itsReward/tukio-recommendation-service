package com.tukio.recommendationservice.service

import com.tukio.recommendationservice.dto.RecommendationRequest
import com.tukio.recommendationservice.dto.RecommendationResponse

interface RecommendationService {

    fun getRecommendations(request: RecommendationRequest): RecommendationResponse

    fun getPersonalizedRecommendations(userId: Long, count: Int): List<Long>

    fun getSimilarEventsRecommendations(userId: Long, count: Int): List<Long>

    fun getPopularEventsRecommendations(count: Int): List<Long>

    fun getTrendingEventsRecommendations(count: Int): List<Long>

    fun getLocationBasedRecommendations(userId: Long, count: Int): List<Long>

    fun getTimeBasedRecommendations(userId: Long, count: Int): List<Long>
}