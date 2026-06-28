package com.communityheroai.ui.screens // FIXED: 19

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.communityheroai.DependencyProvider
import com.communityheroai.data.CommunityStats
import com.communityheroai.data.IssueRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(private val issueRepository: IssueRepository) : ViewModel() {

    val recentIssues = issueRepository.getRecentIssues().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    private val _stats = MutableStateFlow(CommunityStats())
    val stats = _stats.asStateFlow()

    init { loadStats() }

    private fun loadStats() {
        viewModelScope.launch { _stats.value = issueRepository.getCommunityStats() }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                HomeViewModel(DependencyProvider.issueRepository) as T
        }
    }
}
