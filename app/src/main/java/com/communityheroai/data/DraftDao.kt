package com.communityheroai.data
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Delete
import kotlinx.coroutines.flow.Flow

@Dao
interface DraftDao { // FIXED: 7
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDraft(draft: IssueDraft)

    @Query("SELECT * FROM issue_drafts")
    fun getAllDrafts(): Flow<List<IssueDraft>>

    @Delete
    suspend fun deleteDraft(draft: IssueDraft)
}
