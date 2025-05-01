package com.tukio.recommendationservice.client

import com.tukio.recommendationservice.dto.EventDTO
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class EventServiceClientFallback : EventServiceClient {

    private val logger = LoggerFactory.getLogger(EventServiceClientFallback::class.java)

    override fun getEventById(eventId: Long): EventDTO {
        logger.warn("Fallback: getEventById for eventId $eventId")
        return createEmptyEventDTO(eventId)
    }

    override fun getAllEvents(): List<EventDTO> {
        logger.warn("Fallback: getAllEvents")
        return emptyList()
    }

    override fun getUpcomingEvents(): List<EventDTO> {
        logger.warn("Fallback: getUpcomingEvents")
        return emptyList()
    }

    override fun getEventsByCategory(categoryId: Long): List<EventDTO> {
        logger.warn("Fallback: getEventsByCategory for categoryId $categoryId")
        return emptyList()
    }

    override fun searchEvents(categoryId: Long?, keyword: String?, tags: List<String>?): List<EventDTO> {
        logger.warn("Fallback: searchEvents with categoryId $categoryId, keyword $keyword, tags $tags")
        return emptyList()
    }

    private fun createEmptyEventDTO(eventId: Long): EventDTO {
        return EventDTO(
            id = eventId,
            title = "Event data unavailable",
            description = "Event service is currently unavailable",
            categoryId = 0,
            categoryName = "Unknown",
            startTime = LocalDateTime.now(),
            endTime = LocalDateTime.now().plusHours(1),
            location = "Unknown",
            venueId = null,
            venueName = null,
            maxParticipants = 0,
            organizer = "Unknown",
            organizerId = 0,
            imageUrl = null,
            tags = emptySet(),
            status = "UNKNOWN",
            currentRegistrations = 0,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
    }
}