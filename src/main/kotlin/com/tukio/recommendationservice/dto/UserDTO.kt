package com.tukio.recommendationservice.dto

data class UserDTO(
    val id: Long,
    val username: String,
    val firstName: String,
    val lastName: String,
    val department: String?,
    val interests: Set<String>,
    val graduationYear: Int?
)