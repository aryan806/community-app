package com.example.data

import kotlinx.coroutines.flow.Flow
import android.net.Uri

interface IssueRepository {
    fun getRecentIssues(): Flow<List<Issue>>
    fun getIssuesByCategory(category: String): Flow<List<Issue>>
    suspend fun getIssueById(id: String): Result<Issue>
    suspend fun reportIssue(issue: Issue, imageUri: Uri?): Result<Unit>
    suspend fun getCommunityStats(): CommunityStats
}

data class CommunityStats(
    val totalReports: Int = 0,
    val resolvedIssues: Int = 0,
    val activeHeroes: Int = 0,
    val healthScore: Int = 85,
    val criticalIssues: Int = 0,
    val highPriorityIssues: Int = 0,
    val resolvedThisWeek: Int = 0,
    val mostReportedCategory: String = "None",
    val fastestDepartment: String = "None",
    val areaNeedingAttention: String = "None"
)
