package com.tukio.recommendationservice.model

enum class ActivityType {
    VIEW,           // User viewed the event details
    REGISTER,       // User registered for the event
    ATTEND,         // User attended the event
    RATE,           // User rated the event
    SHARE,          // User shared the event
    FAVORITE,       // User favorited the event
    CANCEL          // User cancelled registration
}