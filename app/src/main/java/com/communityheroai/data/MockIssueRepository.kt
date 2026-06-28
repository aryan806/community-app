package com.communityheroai.data // FIXED: 19

import android.net.Uri
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.map
import java.util.UUID

class MockIssueRepository : IssueRepository {
    private val _issues = MutableStateFlow<List<Issue>>(
        listOf(
            Issue(id = "1", title = "Large Pothole on Main St", description = "Deep pothole causing traffic slowdowns and potential vehicle damage.", category = "Potholes", status = "Pending", severity = "High", upvotes = 24, daysUnresolved = 3, locationName = "Main St & 4th Ave", locationLat = 37.7749, locationLng = -122.4194),
            Issue(id = "2", title = "Broken Streetlight", description = "Streetlight has been out for a week, making the intersection unsafe at night.", category = "Streetlights", status = "In Progress", severity = "Medium", upvotes = 12, daysUnresolved = 7, locationName = "Elm St", locationLat = 37.7849, locationLng = -122.4094),
            Issue(id = "3", title = "Water Main Leak", description = "Significant water leakage near the central park entrance.", category = "Water Leakage", status = "Resolved", severity = "Critical", upvotes = 45, daysUnresolved = 0, locationName = "Central Park West", locationLat = 37.7649, locationLng = -122.4294)
        )
    )

    override fun getRecentIssues(): Flow<List<Issue>> = _issues.asStateFlow()

    override fun getIssuesByCategory(category: String): Flow<List<Issue>> =
        MutableStateFlow(_issues.value.filter { it.category == category })
        
    override fun getIssueById(id: String): Flow<Issue?> = _issues.map { list -> list.find { it.id == id } } // FIXED: 4

    override suspend fun reportIssue(issue: Issue, imageUri: Uri?): Result<Unit> {
        delay(1000)
        val daysUnresolved = ((System.currentTimeMillis() - issue.timestamp) / 86400000L).toInt() // FIXED: 15
        val newIssue = issue.copy(id = UUID.randomUUID().toString(), daysUnresolved = daysUnresolved)
        _issues.update { current -> listOf(newIssue) + current }
        return Result.success(Unit)
    }

    override suspend fun getCommunityStats(): CommunityStats {
        delay(500)
        return CommunityStats(
            totalReports = _issues.value.size + 142, resolvedIssues = 89, activeHeroes = 56,
            healthScore = 78, criticalIssues = 3, highPriorityIssues = 12, resolvedThisWeek = 24,
            mostReportedCategory = "Potholes", fastestDepartment = "Waste Management", areaNeedingAttention = "Downtown"
        )
    }
    
    override suspend fun upvoteIssue(issueId: String): Result<Unit> { // FIXED: 8
        _issues.update { list ->
            list.map { if (it.id == issueId) it.copy(upvotes = it.upvotes + 1) else it }
        }
        return Result.success(Unit)
    }
    
    override suspend fun verifyResolution(issueId: String): Result<Unit> { // FIXED: 11
        _issues.update { list ->
            list.map { if (it.id == issueId) it.copy(status = "Resolved") else it }
        }
        return Result.success(Unit)
    }
    
    private val comments = MutableStateFlow<List<Comment>>(listOf(
        Comment("1", "I saw this too. Thanks for reporting!", authorName = "Alice")
    ))
    
    override fun getComments(issueId: String): Flow<List<Comment>> = comments.asStateFlow() // FIXED: 9
    
    override suspend fun addComment(issueId: String, text: String, authorName: String): Result<Unit> { // FIXED: 9
        comments.update { list -> list + Comment(id = UUID.randomUUID().toString(), text = text, authorName = authorName) }
        return Result.success(Unit)
    }
}
