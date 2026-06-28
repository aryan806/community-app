package com.example.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.DependencyProvider
import com.example.data.AuthRepository
import com.example.data.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _isInitializing = MutableStateFlow(true)
    val isInitializing = _isInitializing.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser = _currentUser.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                _currentUser.value = user
                _isInitializing.value = false
            }
        }
    }

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    fun signInWithGoogle(activity: android.app.Activity) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            authRepository.signInWithGoogle("mock_token").onFailure {
                _error.value = it.message
            }
            _isLoading.value = false
        }
    }

    private var currentVerificationId: String? = null

    fun sendPhoneOtp(activity: android.app.Activity, phoneNumber: String, onCodeSent: () -> Unit) {
        _isLoading.value = true
        _error.value = null
        authRepository.sendPhoneOtp(
            activity,
            phoneNumber,
            onCodeSent = { verificationId -> 
                currentVerificationId = verificationId
                _isLoading.value = false
                onCodeSent()
            },
            onError = {
                _isLoading.value = false
                _error.value = it.message
            }
        )
    }

    fun verifyOtp(code: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            if (currentVerificationId == null) {
                _error.value = "Verification ID missing"
                _isLoading.value = false
                return@launch
            }
            authRepository.verifyOtp(currentVerificationId!!, code).onFailure {
                _error.value = it.message
            }
            _isLoading.value = false
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AuthViewModel(DependencyProvider.authRepository) as T
            }
        }
    }
}
