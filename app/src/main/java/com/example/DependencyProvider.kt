package com.example

import com.example.data.AuthRepository
import com.example.data.FirebaseAuthRepository
import com.example.data.FirebaseIssueRepository
import com.example.data.IssueRepository
import com.example.data.MockAuthRepository
import com.example.data.MockIssueRepository

object DependencyProvider {
    val authRepository: AuthRepository by lazy { 
        // TODO: Switch to FirebaseAuthRepository() once Firebase is configured
        MockAuthRepository()
    }
    val issueRepository: IssueRepository by lazy { 
        // TODO: Switch to FirebaseIssueRepository() once Firebase is configured
        MockIssueRepository()
    }
}
