package com.communityheroai.ui.screens // FIXED: 19

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.communityheroai.data.IssueRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class IssueDetailsViewModel(
    savedStateHandle: SavedStateHandle, // FIXED: 4
    private val issueRepository: IssueRepository
) : ViewModel() {
    private val issueId: String = checkNotNull(savedStateHandle["issueId"])

    val issue = issueRepository.getIssueById(issueId).stateIn( // FIXED: 4
        viewModelScope, SharingStarted.WhileSubscribed(5000), null
    )

    val comments = issueRepository.getComments(issueId).stateIn( // FIXED: 9
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    fun upvote() { // FIXED: 8
        viewModelScope.launch { issueRepository.upvoteIssue(issueId) }
    }

    fun verifyResolution() { // FIXED: 11
        viewModelScope.launch { issueRepository.verifyResolution(issueId) }
    }

    fun submitComment(text: String, authorName: String) { // FIXED: 9
        if (text.isBlank()) return
        viewModelScope.launch { issueRepository.addComment(issueId, text, authorName) }
    }
}
