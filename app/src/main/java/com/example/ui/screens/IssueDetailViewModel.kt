package com.example.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.DependencyProvider
import com.example.data.Issue
import com.example.data.IssueRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class IssueDetailViewModel(private val issueRepository: IssueRepository) : ViewModel() {

    private val _issue = MutableStateFlow<Issue?>(null)
    val issue = _issue.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    fun loadIssue(id: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            issueRepository.getIssueById(id)
                .onSuccess { 
                    _issue.value = it
                    _isLoading.value = false
                }
                .onFailure {
                    _error.value = it.message
                    _isLoading.value = false
                }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return IssueDetailViewModel(DependencyProvider.issueRepository) as T
            }
        }
    }
}
