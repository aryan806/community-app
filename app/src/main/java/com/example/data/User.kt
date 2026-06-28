package com.example.data

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val profilePictureUrl: String = "",
    val level: Int = 1,
    val points: Int = 0,
    val rank: String = "Novice"
)
