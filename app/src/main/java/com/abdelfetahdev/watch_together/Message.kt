package com.abdelfetahdev.watch_together

// Message: user, message, type, _createdAt
data class Message(
    val user: User,
    val message: String,
    val type: String,
    val _createdAt: String
)