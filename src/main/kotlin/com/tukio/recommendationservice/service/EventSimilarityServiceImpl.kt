package com.tukio.recommendationservice.service

import com.tukio.recommendationservice.client.EventServiceClient
import com.tukio.recommendationservice.dto.EventDTO
import com.tukio.recommendationservice.model.EventSimilarity
import com.tukio.recommendationservice.repository.EventSimilarityRepository
import org.apache.commons.math3.linear.ArrayRealVector
import org.apache.commons.math3.linear.RealVector
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class EventSimilarityServiceImpl(
    private val eventSimilarityRepository: EventSimilarityRepository,
    private val eventServiceClient: EventServiceClient
) : EventSimilarityService {

    private val logger = LoggerFactory.getLogger(EventSimilarityServiceImpl::class.java)

    @Transactional
    override fun calculateSimilarity(eventId1: Long, eventId2: Long): Double {
        // Ensure consistent ordering of IDs
        val orderedId1 = minOf(eventId1, eventId2)
        val orderedId2 = maxOf(eventId1, eventId2)

        // Check if we already have calculated similarity
        val existingSimilarity = eventSimilarityRepository.findByEventId1AndEventId2(orderedId1, orderedId2)
        if (existingSimilarity != null) {
            return existingSimilarity.similarityScore
        }

        // Fetch event details from event service
        val event1 = try {
            eventServiceClient.getEventById(orderedId1)
        } catch (e: Exception) {
            logger.error("Failed to fetch event $orderedId1: ${e.message}")
            return 0.0
        }

        val event2 = try {
            eventServiceClient.getEventById(orderedId2)
        } catch (e: Exception) {
            logger.error("Failed to fetch event $orderedId2: ${e.message}")
            return 0.0
        }

        // Calculate similarity score
        val similarityScore = calculateSimilarityScore(event1, event2)

        // Save the similarity score
        val eventSimilarity = EventSimilarity(
            eventId1 = orderedId1,
            eventId2 = orderedId2,
            similarityScore = similarityScore,
            lastCalculated = LocalDateTime.now()
        )

        eventSimilarityRepository.save(eventSimilarity)
        return similarityScore
    }

    override fun findSimilarEvents(eventId: Long, minSimilarityScore: Double, limit: Int): Map<Long, Double> {
        val similarEventData = eventSimilarityRepository.findSimilarEventIds(eventId, minSimilarityScore, limit)

        return similarEventData.associate {
            val otherEventId = it[0] as Long
            val similarityScore = it[1] as Double
            otherEventId to similarityScore
        }
    }

    override fun getSimilarityBetweenEvents(eventId1: Long, eventId2: Long): Double {
        // Ensure consistent ordering of IDs
        val orderedId1 = minOf(eventId1, eventId2)
        val orderedId2 = maxOf(eventId1, eventId2)

        return eventSimilarityRepository.findByEventId1AndEventId2(orderedId1, orderedId2)?.similarityScore
            ?: calculateSimilarity(orderedId1, orderedId2)
    }

    @Transactional
    override fun updateEventSimilarities(eventId: Long) {
        try {
            // Get all events to compare with
            val allEvents = eventServiceClient.getAllEvents()

            // Calculate similarity with each event
            for (otherEvent in allEvents) {
                if (otherEvent.id != eventId) {
                    calculateSimilarity(eventId, otherEvent.id)
                }
            }

            logger.info("Updated similarities for event $eventId with ${allEvents.size - 1} other events")
        } catch (e: Exception) {
            logger.error("Failed to update event similarities for event $eventId: ${e.message}")
        }
    }

    @Scheduled(cron = "0 0 2 * * *") // Run at 2 AM every day
    @Transactional
    override fun batchUpdateEventSimilarities() {
        try {
            logger.info("Starting batch update of event similarities")

            // Get all events
            val allEvents = eventServiceClient.getAllEvents()
            val eventIds = allEvents.map { it.id }

            // Create a matrix of similarities that need to be calculated
            var calculationCount = 0

            for (i in eventIds.indices) {
                for (j in i + 1 until eventIds.size) {
                    val eventId1 = eventIds[i]
                    val eventId2 = eventIds[j]

                    // Check if we need to recalculate (no existing record or outdated)
                    val existing = eventSimilarityRepository.findByEventId1AndEventId2(eventId1, eventId2)
                    if (existing == null || existing.lastCalculated.isBefore(LocalDateTime.now().minusDays(7))) {
                        calculateSimilarity(eventId1, eventId2)
                        calculationCount++
                    }
                }
            }

            logger.info("Completed batch update of event similarities. Calculated $calculationCount similarities.")
        } catch (e: Exception) {
            logger.error("Failed to perform batch update of event similarities: ${e.message}")
        }
    }

    // Private helper methods
    private fun calculateSimilarityScore(event1: EventDTO, event2: EventDTO): Double {
        // Simple similarity calculation based on various factors
        var score = 0.0

        // 1. Category similarity (highest weight)
        if (event1.categoryId == event2.categoryId) {
            score += 0.5
        }

        // 2. Tag similarity
        val commonTags = event1.tags.intersect(event2.tags)
        val tagSimilarity = if (event1.tags.isEmpty() || event2.tags.isEmpty()) {
            0.0
        } else {
            commonTags.size.toDouble() / maxOf(event1.tags.size, event2.tags.size)
        }
        score += tagSimilarity * 0.3

        // 3. Content similarity using TF-IDF approach (simplified)
        val contentSimilarity = calculateContentSimilarity(event1, event2)
        score += contentSimilarity * 0.2

        // Ensure score is between 0 and 1
        return score.coerceIn(0.0, 1.0)
    }

    private fun calculateContentSimilarity(event1: EventDTO, event2: EventDTO): Double {
        // Combine title and description for text analysis
        val text1 = "${event1.title} ${event1.description}".lowercase()
        val text2 = "${event2.title} ${event2.description}".lowercase()

        // Simple word frequency analysis (TF)
        val words1 = text1.split(Regex("\\W+")).filter { it.length > 2 }
        val words2 = text2.split(Regex("\\W+")).filter { it.length > 2 }

        if (words1.isEmpty() || words2.isEmpty()) {
            return 0.0
        }

        // Create vocabulary (unique words)
        val vocabulary = (words1 + words2).distinct()

        // Create term frequency vectors
        val vector1 = createTermFrequencyVector(words1, vocabulary)
        val vector2 = createTermFrequencyVector(words2, vocabulary)

        // Calculate cosine similarity
        return cosineSimilarity(vector1, vector2)
    }

    private fun createTermFrequencyVector(words: List<String>, vocabulary: List<String>): RealVector {
        val vector = ArrayRealVector(vocabulary.size)
        val wordCounts = words.groupingBy { it }.eachCount()

        for (i in vocabulary.indices) {
            val word = vocabulary[i]
            val count = wordCounts[word] ?: 0
            vector.setEntry(i, count.toDouble())
        }

        return vector
    }

    private fun cosineSimilarity(v1: RealVector, v2: RealVector): Double {
        val dotProduct = v1.dotProduct(v2)
        val normV1 = v1.norm
        val normV2 = v2.norm

        return if (normV1 == 0.0 || normV2 == 0.0) {
            0.0
        } else {
            dotProduct / (normV1 * normV2)
        }
    }
}