package com.polaki.expense.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionType { EXPENSE, INCOME }

@Entity(tableName = "accounts")
data class Account(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val initialBalance: Long = 0,
    val colorHex: String = "#1AD1A5",
    val icon: String = "wallet"
)

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: TransactionType,
    val colorHex: String = "#1AD1A5",
    val icon: String = "category",
    val parentId: Long? = null,
    // monthly budget limit in Toman, 0 = no budget set
    val monthlyBudget: Long = 0
)

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Long,
    val type: TransactionType,
    val categoryId: Long,
    val accountId: Long,
    val note: String = "",
    // epoch millis
    val date: Long
)
