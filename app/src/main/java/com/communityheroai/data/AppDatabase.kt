package com.communityheroai.data
import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [IssueDraft::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() { // FIXED: 7
    abstract fun draftDao(): DraftDao
}
