package com.example.data

data class Issue(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val imageUrl: String = "",
    val locationLat: Double = 0.0,
    val locationLng: Double = 0.0,
    val locationName: String = "",
    val status: String = "Pending", // Pending, In Progress, Resolved
    val severity: String = "Medium", // Critical, High, Medium, Low
    val upvotes: Int = 0,
    val reporterId: String = "",
    val timestamp: Long = System.currentTimeMillis()
) {
    val daysUnresolved: Int
        @com.google.firebase.firestore.Exclude get() = ((System.currentTimeMillis() - timestamp) / (1000 * 60 * 60 * 24)).coerceAtLeast(0).toInt()
}

val issueCategories = listOf(
    "Potholes",
    "Garbage",
    "Streetlights",
    "Water Leakage",
    "Sewage",
    "Traffic Signals",
    "Public Safety",
    "Other"
)
