package com.example.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FirebaseAuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : AuthRepository {

    override val currentUser: Flow<User?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser != null) {
                // Launch a coroutine to fetch extra user info from Firestore
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                        val doc = db.collection("users").document(firebaseUser.uid).get().await()
                        val user = User(
                            id = firebaseUser.uid,
                            name = firebaseUser.displayName ?: "",
                            email = firebaseUser.email ?: "",
                            phoneNumber = firebaseUser.phoneNumber ?: "",
                            profilePictureUrl = firebaseUser.photoUrl?.toString() ?: "",
                            level = doc.getLong("level")?.toInt() ?: 1,
                            points = doc.getLong("points")?.toInt() ?: 0,
                            rank = doc.getString("rank") ?: "Novice"
                        )
                        trySend(user)
                    } catch (e: Exception) {
                        val user = User(
                            id = firebaseUser.uid,
                            name = firebaseUser.displayName ?: "",
                            email = firebaseUser.email ?: "",
                            phoneNumber = firebaseUser.phoneNumber ?: "",
                            profilePictureUrl = firebaseUser.photoUrl?.toString() ?: ""
                        )
                        trySend(user)
                    }
                }
            } else {
                trySend(null)
            }
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override suspend fun signInWithGoogle(idToken: String): Result<User> {
        return try {
            val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(credential).await()
            val firebaseUser = authResult.user
            if (firebaseUser != null) {
                Result.success(User(
                    id = firebaseUser.uid,
                    name = firebaseUser.displayName ?: "",
                    email = firebaseUser.email ?: "",
                    profilePictureUrl = firebaseUser.photoUrl?.toString() ?: ""
                ))
            } else {
                Result.failure(Exception("Google Sign-In failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun sendPhoneOtp(
        activity: android.app.Activity,
        phoneNumber: String,
        onCodeSent: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, java.util.concurrent.TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    // Usually handled by auto-retrieval
                }
                override fun onVerificationFailed(e: com.google.firebase.FirebaseException) {
                    onError(e)
                }
                override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                    onCodeSent(verificationId)
                }
            })
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    override suspend fun verifyOtp(verificationId: String, code: String): Result<User> {
        return try {
            val credential = PhoneAuthProvider.getCredential(verificationId, code)
            val authResult = auth.signInWithCredential(credential).await()
            val firebaseUser = authResult.user
            if (firebaseUser != null) {
                Result.success(User(id = firebaseUser.uid, phoneNumber = firebaseUser.phoneNumber ?: ""))
            } else {
                Result.failure(Exception("User is null"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signOut() {
        auth.signOut()
    }
}
