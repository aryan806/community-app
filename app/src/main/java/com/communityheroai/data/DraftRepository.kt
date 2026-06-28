package com.communityheroai.data

class DraftRepository(private val draftDao: DraftDao) { // FIXED: 7
    fun getAllDrafts() = draftDao.getAllDrafts()
    suspend fun saveDraft(draft: IssueDraft) = draftDao.insertDraft(draft)
}
