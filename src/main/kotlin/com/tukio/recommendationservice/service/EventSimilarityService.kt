package com.tukio.recommendationservice.service

interface EventSimilarityService {

    fun calculateSimilarity(eventId1: Long, eventId2: Long): Double

    fun findSimilarEvents(eventId: Long, minSimilarityScore: Double, limit: Int): Map<Long, Double>

    fun getSimilarityBetweenEvents(eventId1: Long, eventId2: Long): Double

    fun updateEventSimilarities(eventId: Long)

    fun batchUpdateEventSimilarities()
}