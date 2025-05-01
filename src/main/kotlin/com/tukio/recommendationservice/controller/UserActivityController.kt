package com.tukio.recommendationservice.controller

import com.tukio.recommendationservice.dto.UserActivityDTO
import com.tukio.recommendationservice.model.ActivityType
import com.tukio.recommendationservice.service.UserActivityService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/activities")
class UserActivityController(private val userActivityService: UserActivityService) {

    @PostMapping
    fun recordActivity(@RequestBody activityDTO: UserActivityDTO): ResponseEntity<Void> {
        userActivityService.recordUserActivity(activityDTO)
        return ResponseEntity.status(HttpStatus.CREATED).build()
    }

    @GetMapping("/user/{userId}")
    fun getUserActivities(@PathVariable userId: Long): ResponseEntity<List<UserActivityDTO>> {
        return ResponseEntity.ok(userActivityService.getUserActivities(userId))
    }

    @GetMapping("/user/{userId}/type/{activityType}")
    fun getUserActivitiesByType(
        @PathVariable userId: Long,
        @PathVariable activityType: ActivityType
    ): ResponseEntity<List<UserActivityDTO>> {
        return ResponseEntity.ok(userActivityService.getUserActivitiesByType(userId, activityType))
    }

    @GetMapping("/event/{eventId}")
    fun getEventActivities(@PathVariable eventId: Long): ResponseEntity<List<UserActivityDTO>> {
        return ResponseEntity.ok(userActivityService.getEventActivities(eventId))
    }

    @GetMapping("/event/{eventId}/type/{activityType}")
    fun getEventActivitiesByType(
        @PathVariable eventId: Long,
        @PathVariable activityType: ActivityType
    ): ResponseEntity<List<UserActivityDTO>> {
        return ResponseEntity.ok(userActivityService.getEventActivitiesByType(eventId, activityType))
    }

    @GetMapping("/event/{eventId}/rating")
    fun getEventAverageRating(@PathVariable eventId: Long): ResponseEntity<Map<String, Double?>> {
        val rating = userActivityService.getAverageRatingForEvent(eventId)
        return ResponseEntity.ok(mapOf("averageRating" to rating))
    }

    @GetMapping("/popular")
    fun getPopularEvents(
        @RequestParam activityType: ActivityType,
        @RequestParam(defaultValue = "10") limit: Int
    ): ResponseEntity<List<Long>> {
        return ResponseEntity.ok(userActivityService.getMostPopularEvents(activityType, limit))
    }

    @GetMapping("/trending")
    fun getTrendingEvents(
        @RequestParam activityType: ActivityType,
        @RequestParam(defaultValue = "7") daysAgo: Int,
        @RequestParam(defaultValue = "10") limit: Int
    ): ResponseEntity<List<Long>> {
        return ResponseEntity.ok(userActivityService.getTrendingEvents(activityType, daysAgo, limit))
    }
}