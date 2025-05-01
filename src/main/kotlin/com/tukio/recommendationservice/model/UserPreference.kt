package com.tukio.recommendationservice.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "user_preferences")
data class UserPreference(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val userId: Long,

    @Column(nullable = false)
    val categoryId: Long,

    @Column(nullable = false)
    var preferenceScore: Double, // Range from 0.0 to 1.0

    @ElementCollection
    @CollectionTable(name = "user_preference_tags", joinColumns = [JoinColumn(name = "preference_id")])
    @Column(name = "tag")
    var tags: MutableSet<String> = mutableSetOf(),

    @Column(nullable = true)
    var preferredLocation: String? = null,

    @Column(nullable = true)
    var preferredDayOfWeek: Int? = null, // 1-7 for Monday-Sunday

    @Column(nullable = true)
    var preferredTimeOfDay: String? = null, // MORNING, AFTERNOON, EVENING

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
)