package com.tukio.recommendationservice.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "user_activities")
data class UserActivity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val userId: Long,

    @Column(nullable = false)
    val eventId: Long,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val activityType: ActivityType,

    @Column(nullable = true)
    val rating: Int? = null,

    @Column(nullable = true)
    val viewDuration: Int? = null, // in seconds

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)