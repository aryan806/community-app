package com.communityheroai.data // FIXED: 19

import android.net.Uri
import kotlinx.coroutines.flow.Flow

interface IssueRepository {
    fun getRecentIssues(): Flow<List<Issue>>
    fun getIssuesByCategory(category: String): Flow<List<Issue>>
    fun getIssueById(id: String): Flow<Issue?> // FIXED: 4
    suspend fun reportIssue(issue: Issue, imageUri: Uri?): Result<Unit>
    suspend fun getCommunityStats(): CommunityStats
    suspend fun upvoteIssue(issueId: String): Result<Unit> // FIXED: 8
    suspend fun verifyResolution(issueId: String): Result<Unit> // FIXED: 11
    fun getComments(issueId: String): Flow<List<Comment>> // FIXED: 9
    suspend fun addComment(issueId: String, text: String, authorName: String): Result<Unit> // FIXED: 9
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
