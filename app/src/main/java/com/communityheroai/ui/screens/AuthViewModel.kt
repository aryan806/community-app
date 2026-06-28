package com.communityheroai.ui.screens // FIXED: 19

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.communityheroai.DependencyProvider
import com.communityheroai.data.AuthRepository
import com.communityheroai.data.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

    val currentUser = authRepository.currentUser.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), null
    )

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _verificationId = MutableStateFlow("") // FIXED: 3

    fun signInWithGoogle(idToken: String) { // FIXED: 1
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            authRepository.signInWithGoogle(idToken).onFailure { _error.value = it.message }
            _isLoading.value = false
        }
    }

    fun sendPhoneOtp(phoneNumber: String, activity: Activity, onCodeSent: () -> Unit) { // FIXED: 2
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            authRepository.sendPhoneOtp(phoneNumber, activity,
                onCodeSent = { vid ->
                    _verificationId.value = vid // FIXED: 3
                    _isLoading.value = false
                    onCodeSent()
                },
                onError = { e ->
                    _isLoading.value = false
                    _error.value = e.message
                }
            )
        }
    }

    fun verifyOtp(code: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            authRepository.verifyOtp(_verificationId.value, code).onFailure { _error.value = it.message } // FIXED: 3
            _isLoading.value = false
        }
    }

    fun signOut() { viewModelScope.launch { authRepository.signOut() } }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                AuthViewModel(DependencyProvider.authRepository) as T
        }
    }
}
