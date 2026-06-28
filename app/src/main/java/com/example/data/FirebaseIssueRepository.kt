package com.example.data

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirebaseIssueRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) : IssueRepository {

    override fun getRecentIssues(): Flow<List<Issue>> = callbackFlow {
        val listener = firestore.collection("issues")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(20)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val issues = snapshot.documents.mapNotNull { it.toObject(Issue::class.java) }
                    trySend(issues)
                }
            }
        awaitClose { listener.remove() }
    }

    override fun getIssuesByCategory(category: String): Flow<List<Issue>> = callbackFlow {
        val listener = firestore.collection("issues")
            .whereEqualTo("category", category)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val issues = snapshot.documents.mapNotNull { it.toObject(Issue::class.java) }
                    trySend(issues)
                }
            }
        awaitClose { listener.remove() }
    }

    override suspend fun getIssueById(id: String): Result<Issue> {
        return try {
            val snapshot = firestore.collection("issues").document(id).get().await()
            val issue = snapshot.toObject(Issue::class.java)
            if (issue != null) {
                Result.success(issue)
            } else {
                Result.failure(Exception("Issue not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun reportIssue(issue: Issue, imageUri: Uri?): Result<Unit> {
        return try {
            var imageUrl = ""
            if (imageUri != null) {
                val imageRef = storage.reference.child("issues/${UUID.randomUUID()}")
                val uploadTask = imageRef.putFile(imageUri).await()
                imageUrl = imageRef.downloadUrl.await().toString()
            }
            
            val newIssue = issue.copy(
                id = UUID.randomUUID().toString(),
                imageUrl = imageUrl
            )
            
            firestore.collection("issues").document(newIssue.id).set(newIssue).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCommunityStats(): CommunityStats {
        return try {
            val totalQuery = firestore.collection("issues").count().get(com.google.firebase.firestore.AggregateSource.SERVER).await()
            val total = totalQuery.count.toInt()
            
            val resolvedQuery = firestore.collection("issues").whereEqualTo("status", "Resolved").count().get(com.google.firebase.firestore.AggregateSource.SERVER).await()
            val resolved = resolvedQuery.count.toInt()
            
            val criticalQuery = firestore.collection("issues").whereEqualTo("severity", "Critical").count().get(com.google.firebase.firestore.AggregateSource.SERVER).await()
            val critical = criticalQuery.count.toInt()
            
            val highQuery = firestore.collection("issues").whereEqualTo("severity", "High").count().get(com.google.firebase.firestore.AggregateSource.SERVER).await()
            val high = highQuery.count.toInt()
            
            val weekAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L)
            val resolvedWeekQuery = firestore.collection("issues").whereEqualTo("status", "Resolved").whereGreaterThan("timestamp", weekAgo).count().get(com.google.firebase.firestore.AggregateSource.SERVER).await()
            val resolvedThisWeek = resolvedWeekQuery.count.toInt()
            
            val healthScore = if (total > 0) ((resolved.toFloat() / total) * 100).toInt() else 100
            
            CommunityStats(
                totalReports = total,
                resolvedIssues = resolved,
                activeHeroes = 120, // static for now
                healthScore = healthScore.coerceIn(0, 100),
                criticalIssues = critical,
                highPriorityIssues = high,
                resolvedThisWeek = resolvedThisWeek
            )
        } catch (e: Exception) {
            CommunityStats()
        }
    }
}
