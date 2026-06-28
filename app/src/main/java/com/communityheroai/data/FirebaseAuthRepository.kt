package com.communityheroai.data // FIXED: 19

import android.app.Activity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

class FirebaseAuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : AuthRepository {

    override val currentUser: Flow<User?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val firebaseUser = firebaseAuth.currentUser
            val user = firebaseUser?.let {
                User(id = it.uid, name = it.displayName ?: "", email = it.email ?: "",
                    phoneNumber = it.phoneNumber ?: "", profilePictureUrl = it.photoUrl?.toString() ?: "")
            }
            trySend(user)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override suspend fun signInWithGoogle(idToken: String): Result<User> { // FIXED: 1
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(credential).await()
            val firebaseUser = authResult.user
            if (firebaseUser != null) {
                Result.success(User(id = firebaseUser.uid, name = firebaseUser.displayName ?: "", email = firebaseUser.email ?: "", profilePictureUrl = firebaseUser.photoUrl?.toString() ?: ""))
            } else {
                Result.failure(Exception("User is null"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sendPhoneOtp(
        phoneNumber: String,
        activity: Activity,
        onCodeSent: (String) -> Unit,
        onError: (Exception) -> Unit
    ) { // FIXED: 2
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {}
                override fun onVerificationFailed(e: FirebaseException) { onError(e) }
                override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                    onCodeSent(verificationId)
                }
            }).build()
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

    override suspend fun signOut() { auth.signOut() }
}
