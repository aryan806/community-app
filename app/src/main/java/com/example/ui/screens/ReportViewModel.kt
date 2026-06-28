package com.example.ui.screens

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.DependencyProvider
import com.example.data.Issue
import com.example.data.IssueRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReportViewModel(private val issueRepository: IssueRepository) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _isSuccess = MutableStateFlow(false)
    val isSuccess = _isSuccess.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    fun submitIssue(issue: Issue, imageUri: Uri?) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _isSuccess.value = false
            
            val result = issueRepository.reportIssue(issue, imageUri)
            result.onSuccess {
                _isSuccess.value = true
            }.onFailure {
                _error.value = it.message
            }
            
            _isLoading.value = false
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ReportViewModel(DependencyProvider.issueRepository) as T
            }
        }
    }
}
