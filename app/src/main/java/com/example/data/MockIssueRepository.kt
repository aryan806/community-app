package com.example.data

import android.net.Uri
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

class MockIssueRepository : IssueRepository {
    private val _issues = MutableStateFlow<List<Issue>>(
        listOf(
            Issue(
                id = "1",
                title = "Large Pothole on Main St",
                description = "Deep pothole causing traffic slowdowns and potential vehicle damage.",
                category = "Potholes",
                status = "Pending",
                severity = "High",
                upvotes = 24,
                locationName = "Main St & 4th Ave",
                locationLat = 37.7749,
                locationLng = -122.4194,
                timestamp = System.currentTimeMillis() - 86400000 * 3
            ),
            Issue(
                id = "2",
                title = "Broken Streetlight",
                description = "Streetlight has been out for a week, making the intersection unsafe at night.",
                category = "Streetlights",
                status = "In Progress",
                severity = "Medium",
                upvotes = 12,
                locationName = "Elm St",
                locationLat = 37.7849,
                locationLng = -122.4094,
                timestamp = System.currentTimeMillis() - 86400000 * 7
            ),
            Issue(
                id = "3",
                title = "Water Main Leak",
                description = "Significant water leakage near the central park entrance.",
                category = "Water Leakage",
                status = "Resolved",
                severity = "Critical",
                upvotes = 45,
                locationName = "Central Park West",
                locationLat = 37.7649,
                locationLng = -122.4294,
                timestamp = System.currentTimeMillis()
            )
        )
    )

    override fun getRecentIssues(): Flow<List<Issue>> = _issues.asStateFlow()

    override fun getIssuesByCategory(category: String): Flow<List<Issue>> {
        // Return filtered flow, simplified for mock
        return MutableStateFlow(_issues.value.filter { it.category == category })
    }

    override suspend fun getIssueById(id: String): Result<Issue> {
        val issue = _issues.value.find { it.id == id }
        return if (issue != null) {
            Result.success(issue)
        } else {
            Result.failure(Exception("Issue not found"))
        }
    }

    override suspend fun reportIssue(issue: Issue, imageUri: Uri?): Result<Unit> {
        delay(1000) // Simulate network
        val newIssue = issue.copy(id = UUID.randomUUID().toString())
        _issues.update { current ->
            listOf(newIssue) + current
        }
        return Result.success(Unit)
    }

    override suspend fun getCommunityStats(): CommunityStats {
        delay(500)
        return CommunityStats(
            totalReports = _issues.value.size + 142,
            resolvedIssues = 89,
            activeHeroes = 56,
            healthScore = 78,
            criticalIssues = 3,
            highPriorityIssues = 12,
            resolvedThisWeek = 24,
            mostReportedCategory = "Potholes",
            fastestDepartment = "Waste Management",
            areaNeedingAttention = "Downtown"
        )
    }
}
