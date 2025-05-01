package com.tukio.recommendationservice.client

import com.tukio.recommendationservice.dto.UserDTO
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class UserServiceClientFallback : UserServiceClient {

    private val logger = LoggerFactory.getLogger(UserServiceClientFallback::class.java)

    override fun getUserById(userId: Long): UserDTO {
        logger.warn("Fallback: getUserById for userId $userId")
        return createEmptyUserDTO(userId)
    }

    override fun getUserProfile(userId: Long): UserDTO {
        logger.warn("Fallback: getUserProfile for userId $userId")
        return createEmptyUserDTO(userId)
    }

    override fun getUsersByInterests(interests: Set<String>): List<UserDTO> {
        logger.warn("Fallback: getUsersByInterests with interests $interests")
        return emptyList()
    }

    private fun createEmptyUserDTO(userId: Long): UserDTO {
        return UserDTO(
            id = userId,
            username = "unknown_user",
            firstName = "Unknown",
            lastName = "User",
            department = null,
            interests = emptySet(),
            graduationYear = null
        )
    }
}