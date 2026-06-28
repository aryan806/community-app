package com.communityheroai.data // FIXED: 19
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "issue_drafts")
data class IssueDraft( // FIXED: 7
    @PrimaryKey(autoGenerate = true) val draftId: Int = 0,
    val title: String,
    val description: String,
    val category: String,
    val severity: String,
    val locationName: String,
    val locationLat: Double,
    val locationLng: Double
)
