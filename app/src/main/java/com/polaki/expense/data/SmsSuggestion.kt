package com.polaki.expense.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sms_suggestions")
data class SmsSuggestion(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Long,
    val type: TransactionType,
    val sender: String,
    val rawMessage: String,
    val date: Long,
    // becomes true once the user confirms or dismisses it
    val handled: Boolean = false
)
