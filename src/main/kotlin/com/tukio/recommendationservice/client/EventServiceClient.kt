package com.tukio.recommendationservice.client

import com.tukio.recommendationservice.dto.EventDTO
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(name = "tukio-events-service", fallback = EventServiceClientFallback::class)
interface EventServiceClient {

    @GetMapping("/api/events/{id}")
    fun getEventById(@PathVariable("id") eventId: Long): EventDTO

    @GetMapping("/api/events")
    fun getAllEvents(): List<EventDTO>

    @GetMapping("/api/events/upcoming")
    fun getUpcomingEvents(): List<EventDTO>

    @GetMapping("/api/events/category/{categoryId}")
    fun getEventsByCategory(@PathVariable("categoryId") categoryId: Long): List<EventDTO>

    @GetMapping("/api/events/search")
    fun searchEvents(
        @RequestParam(required = false) categoryId: Long?,
        @RequestParam(required = false) keyword: String?,
        @RequestParam(required = false) tags: List<String>?
    ): List<EventDTO>
}