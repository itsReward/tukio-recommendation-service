package com.tukio.recommendationservice.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "event_similarities")
data class EventSimilarity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val eventId1: Long,

    @Column(nullable = false)
    val eventId2: Long,

    @Column(nullable = false)
    val similarityScore: Double, // Range from 0.0 to 1.0

    @Column(nullable = false)
    val lastCalculated: LocalDateTime = LocalDateTime.now()
)