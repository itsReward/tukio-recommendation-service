package com.tukio.recommendationservice.controller

import com.tukio.recommendationservice.dto.RecommendationRequest
import com.tukio.recommendationservice.dto.RecommendationResponse
import com.tukio.recommendationservice.service.RecommendationService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/recommendations")
class RecommendationController(private val recommendationService: RecommendationService) {

    @PostMapping
    fun getRecommendations(@RequestBody request: RecommendationRequest): ResponseEntity<RecommendationResponse> {
        return ResponseEntity.ok(recommendationService.getRecommendations(request))
    }

    @GetMapping("/user/{userId}")
    fun getRecommendationsForUser(
        @PathVariable userId: Long,
        @RequestParam(defaultValue = "5") count: Int,
        @RequestParam(defaultValue = "true") includeUpcoming: Boolean,
        @RequestParam(defaultValue = "false") includePast: Boolean,
        @RequestParam(defaultValue = "true") includeSimilarEvents: Boolean,
        @RequestParam(defaultValue = "true") includePopularEvents: Boolean
    ): ResponseEntity<RecommendationResponse> {
        val request = RecommendationRequest(
            userId = userId,
            count = count,
            includeUpcoming = includeUpcoming,
            includePast = includePast,
            includeSimilarEvents = includeSimilarEvents,
            includePopularEvents = includePopularEvents
        )
        return ResponseEntity.ok(recommendationService.getRecommendations(request))
    }

    @GetMapping("/user/{userId}/personalized")
    fun getPersonalizedRecommendations(
        @PathVariable userId: Long,
        @RequestParam(defaultValue = "5") count: Int
    ): ResponseEntity<List<Long>> {
        return ResponseEntity.ok(recommendationService.getPersonalizedRecommendations(userId, count))
    }

    @GetMapping("/user/{userId}/similar")
    fun getSimilarEventsRecommendations(
        @PathVariable userId: Long,
        @RequestParam(defaultValue = "5") count: Int
    ): ResponseEntity<List<Long>> {
        return ResponseEntity.ok(recommendationService.getSimilarEventsRecommendations(userId, count))
    }

    @GetMapping("/popular")
    fun getPopularEventsRecommendations(
        @RequestParam(defaultValue = "5") count: Int
    ): ResponseEntity<List<Long>> {
        return ResponseEntity.ok(recommendationService.getPopularEventsRecommendations(count))
    }

    @GetMapping("/trending")
    fun getTrendingEventsRecommendations(
        @RequestParam(defaultValue = "5") count: Int
    ): ResponseEntity<List<Long>> {
        return ResponseEntity.ok(recommendationService.getTrendingEventsRecommendations(count))
    }

    @GetMapping("/user/{userId}/location")
    fun getLocationBasedRecommendations(
        @PathVariable userId: Long,
        @RequestParam(defaultValue = "5") count: Int
    ): ResponseEntity<List<Long>> {
        return ResponseEntity.ok(recommendationService.getLocationBasedRecommendations(userId, count))
    }

    @GetMapping("/user/{userId}/time")
    fun getTimeBasedRecommendations(
        @PathVariable userId: Long,
        @RequestParam(defaultValue = "5") count: Int
    ): ResponseEntity<List<Long>> {
        return ResponseEntity.ok(recommendationService.getTimeBasedRecommendations(userId, count))
    }
}