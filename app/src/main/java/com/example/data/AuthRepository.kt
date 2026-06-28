package com.example.data

import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<User?>
    suspend fun signInWithGoogle(idToken: String): Result<User>
    fun sendPhoneOtp(
        activity: android.app.Activity,
        phoneNumber: String,
        onCodeSent: (String) -> Unit,
        onError: (Exception) -> Unit
    )
    suspend fun verifyOtp(verificationId: String, code: String): Result<User>
    suspend fun signOut()
}
