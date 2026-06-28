package com.example.data

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

    override fun sendPhoneOtp(
        activity: android.app.Activity,
        phoneNumber: String,
        onCodeSent: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        // Just mock success immediately
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

    override suspend fun signOut() {
        _currentUser.value = null
    }
}
