package com.communityheroai.data // FIXED: 19

import android.app.Activity
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<User?>
    suspend fun signInWithGoogle(idToken: String): Result<User>
    suspend fun sendPhoneOtp(
        phoneNumber: String,
        activity: Activity, // FIXED: 2
        onCodeSent: (String) -> Unit,
        onError: (Exception) -> Unit
    )
    suspend fun verifyOtp(verificationId: String, code: String): Result<User>
    suspend fun signOut()
}
