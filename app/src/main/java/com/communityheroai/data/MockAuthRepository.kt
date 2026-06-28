package com.communityheroai.data // FIXED: 19

import android.app.Activity
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class MockAuthRepository : AuthRepository {
    private val _currentUser = MutableStateFlow<User?>(null)
    override val currentUser: Flow<User?> = _currentUser.asStateFlow()

    override suspend fun signInWithGoogle(idToken: String): Result<User> {
        delay(1000)
        val user = User(id = "google_user", name = "Citizen Hero", email = "citizen@example.com", level = 5, points = 1250, rank = "Neighborhood Watch")
        _currentUser.value = user
        return Result.success(user)
    }

    override suspend fun sendPhoneOtp(phoneNumber: String, activity: Activity, onCodeSent: (String) -> Unit, onError: (Exception) -> Unit) { // FIXED: 2
        onCodeSent("mock_verification_id")
    }

    override suspend fun verifyOtp(verificationId: String, code: String): Result<User> {
        delay(1000)
        if (code == "123456") {
            val user = User(id = "phone_user", phoneNumber = "+1234567890", name = "Local Hero", level = 3, points = 450, rank = "Scout")
            _currentUser.value = user
            return Result.success(user)
        } else {
            return Result.failure(Exception("Invalid OTP"))
        }
    }

    override suspend fun signOut() { _currentUser.value = null }
}
