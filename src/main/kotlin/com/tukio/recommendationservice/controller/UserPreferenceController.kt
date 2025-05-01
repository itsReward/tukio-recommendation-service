package com.tukio.recommendationservice.controller

import com.tukio.recommendationservice.dto.UserPreferenceDTO
import com.tukio.recommendationservice.service.UserPreferenceService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/preferences")
class UserPreferenceController(private val userPreferenceService: UserPreferenceService) {

    @GetMapping("/user/{userId}")
    fun getUserPreferences(@PathVariable userId: Long): ResponseEntity<List<UserPreferenceDTO>> {
        return ResponseEntity.ok(userPreferenceService.getUserPreferences(userId))
    }

    @GetMapping("/user/{userId}/category/{categoryId}")
    fun getUserPreferenceForCategory(
        @PathVariable userId: Long,
        @PathVariable categoryId: Long
    ): ResponseEntity<UserPreferenceDTO?> {
        val preference = userPreferenceService.getUserPreferenceForCategory(userId, categoryId)
        return if (preference != null) {
            ResponseEntity.ok(preference)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @PutMapping
    fun updateUserPreference(@RequestBody preferenceDTO: UserPreferenceDTO): ResponseEntity<UserPreferenceDTO> {
        return ResponseEntity.ok(userPreferenceService.updateUserPreference(preferenceDTO))
    }

    @GetMapping("/user/{userId}/tags")
    fun getUserPreferencesByTags(
        @PathVariable userId: Long,
        @RequestParam tags: Set<String>
    ): ResponseEntity<List<UserPreferenceDTO>> {
        return ResponseEntity.ok(userPreferenceService.getUserPreferencesByTags(userId, tags))
    }

    @GetMapping("/user/{userId}/categories")
    fun getFavoriteCategories(
        @PathVariable userId: Long,
        @RequestParam(defaultValue = "5") limit: Int
    ): ResponseEntity<List<Long>> {
        return ResponseEntity.ok(userPreferenceService.getFavoriteCategories(userId, limit))
    }

    @GetMapping("/user/{userId}/similar-users")
    fun findSimilarUsers(
        @PathVariable userId: Long,
        @RequestParam(defaultValue = "0.3") minSimilarityScore: Double
    ): ResponseEntity<List<Long>> {
        return ResponseEntity.ok(userPreferenceService.findSimilarUsersByInterests(userId, minSimilarityScore))
    }

    @PostMapping("/user/{userId}/analyze")
    fun updatePreferencesBasedOnActivity(@PathVariable userId: Long): ResponseEntity<List<UserPreferenceDTO>> {
        return ResponseEntity.ok(userPreferenceService.updatePreferencesBasedOnActivity(userId))
    }
}