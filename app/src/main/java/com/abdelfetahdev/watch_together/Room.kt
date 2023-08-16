package com.abdelfetahdev.watch_together

// Room: _id, profile_image, admin, creator, name, description, total_members

data class Room(
    val _id: String,
    val profile_image: String,
    val admin: User,
    val creator: User,
    val name: String,
    val description: String,
    val total_members: Int)