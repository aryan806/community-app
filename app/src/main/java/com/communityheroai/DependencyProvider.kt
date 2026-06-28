package com.communityheroai // FIXED: 19

import android.content.Context
import androidx.room.Room
import com.communityheroai.data.AuthRepository
import com.communityheroai.data.FirebaseAuthRepository
import com.communityheroai.data.FirebaseIssueRepository
import com.communityheroai.data.IssueRepository
import com.communityheroai.data.MockAuthRepository
import com.communityheroai.data.MockIssueRepository
import com.communityheroai.data.DraftRepository
import com.communityheroai.data.AppDatabase

object DependencyProvider {
    lateinit var draftRepository: DraftRepository // FIXED: 7

    fun init(context: Context) { // FIXED: 7
        val db = Room.databaseBuilder(context, AppDatabase::class.java, "hero_db").build()
        draftRepository = DraftRepository(db.draftDao())
    }

    val authRepository: AuthRepository by lazy {
        try {
            FirebaseAuthRepository()
        } catch (e: Exception) { // FIXED: 20
            MockAuthRepository()
        }
    }
    val issueRepository: IssueRepository by lazy {
        try {
            FirebaseIssueRepository()
        } catch (e: Exception) { // FIXED: 20
            MockIssueRepository()
        }
    }
}
