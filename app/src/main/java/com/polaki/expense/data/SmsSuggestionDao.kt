package com.polaki.expense.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SmsSuggestionDao {
    @Query("SELECT * FROM sms_suggestions WHERE handled = 0 ORDER BY date DESC")
    fun getPending(): Flow<List<SmsSuggestion>>

    @Insert
    suspend fun insert(suggestion: SmsSuggestion): Long

    @Update
    suspend fun update(suggestion: SmsSuggestion)
}
