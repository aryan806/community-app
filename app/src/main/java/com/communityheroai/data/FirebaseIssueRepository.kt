package com.communityheroai.data // FIXED: 19

import android.net.Uri
import com.google.firebase.firestore.FieldValue
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
                if (error != null) { close(error); return@addSnapshotListener }
                if (snapshot != null) {
                    val issues = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Issue::class.java)?.copy(
                            daysUnresolved = ((System.currentTimeMillis() - (doc.getLong("timestamp") ?: 0L)) / 86400000L).toInt() // FIXED: 15
                        )
                    }
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
                if (error != null) { close(error); return@addSnapshotListener }
                if (snapshot != null) {
                    val issues = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Issue::class.java)?.copy(
                            daysUnresolved = ((System.currentTimeMillis() - (doc.getLong("timestamp") ?: 0L)) / 86400000L).toInt() // FIXED: 15
                        )
                    }
                    trySend(issues)
                }
            }
        awaitClose { listener.remove() }
    }
    
    override fun getIssueById(id: String): Flow<Issue?> = callbackFlow { // FIXED: 4
        val listener = firestore.collection("issues").document(id)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                if (snapshot != null && snapshot.exists()) {
                    val issue = snapshot.toObject(Issue::class.java)?.copy(
                        daysUnresolved = ((System.currentTimeMillis() - (snapshot.getLong("timestamp") ?: 0L)) / 86400000L).toInt()
                    )
                    trySend(issue)
                } else {
                    trySend(null)
                }
            }
        awaitClose { listener.remove() }
    }

    override suspend fun reportIssue(issue: Issue, imageUri: Uri?): Result<Unit> {
        return try {
            var imageUrl = ""
            if (imageUri != null) {
                val imageRef = storage.reference.child("issues/${UUID.randomUUID()}")
                imageRef.putFile(imageUri).await()
                imageUrl = imageRef.downloadUrl.await().toString()
            }
            val newIssue = issue.copy(id = UUID.randomUUID().toString(), imageUrl = imageUrl)
            firestore.collection("issues").document(newIssue.id).set(newIssue).await()
            firestore.collection("stats").document("global").update("totalReports", FieldValue.increment(1)).await() // FIXED: 16
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCommunityStats(): CommunityStats {
        return try {
            val doc = firestore.collection("stats").document("global").get().await() // FIXED: 18
            val total = doc.getLong("totalReports")?.toInt() ?: 0
            val resolved = doc.getLong("resolvedIssues")?.toInt() ?: 0
            val heroes = doc.getLong("activeHeroes")?.toInt() ?: 120
            CommunityStats(totalReports = total, resolvedIssues = resolved, activeHeroes = heroes)
        } catch (e: Exception) {
            CommunityStats()
        }
    }
    
    override suspend fun upvoteIssue(issueId: String): Result<Unit> { // FIXED: 8
        return try {
            firestore.collection("issues").document(issueId).update("upvotes", FieldValue.increment(1)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun verifyResolution(issueId: String): Result<Unit> { // FIXED: 11
        return try {
            firestore.collection("issues").document(issueId).update("status", "Resolved").await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override fun getComments(issueId: String): Flow<List<Comment>> = callbackFlow { // FIXED: 9
        val listener = firestore.collection("issues").document(issueId).collection("comments")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                trySend(snapshot?.documents?.mapNotNull { it.toObject(Comment::class.java) } ?: emptyList())
            }
        awaitClose { listener.remove() }
    }
    
    override suspend fun addComment(issueId: String, text: String, authorName: String): Result<Unit> { // FIXED: 9
        return try {
            val comment = Comment(id = UUID.randomUUID().toString(), text = text, authorName = authorName)
            firestore.collection("issues").document(issueId).collection("comments").document(comment.id).set(comment).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
