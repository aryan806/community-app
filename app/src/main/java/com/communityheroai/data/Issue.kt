package com.communityheroai.data // FIXED: 19

data class Issue(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val imageUrl: String = "",
    val locationLat: Double = 0.0,
    val locationLng: Double = 0.0,
    val locationName: String = "",
    val status: String = "Pending",
    val severity: String = "Medium",
    val upvotes: Int = 0,
    val daysUnresolved: Int = 0,
    val reporterId: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class Comment( // FIXED: 9
    val id: String = "",
    val text: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

val issueCategories = listOf(
    "Potholes", "Garbage", "Streetlights", "Water Leakage",
    "Sewage", "Traffic Signals", "Public Safety", "Other"
)
