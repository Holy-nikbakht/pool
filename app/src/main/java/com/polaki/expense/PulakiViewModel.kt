package com.polaki.expense

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.polaki.expense.data.Account
import com.polaki.expense.data.AppDatabase
import com.polaki.expense.data.Category
import com.polaki.expense.data.SmsSuggestion
import com.polaki.expense.data.Transaction
import com.polaki.expense.data.TransactionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PulakiViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)

    val accounts: StateFlow<List<Account>> = db.accountDao().getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<Category>> = db.categoryDao().getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactions: StateFlow<List<Transaction>> = db.transactionDao().getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // Bank SMS detected transactions awaiting user confirmation.
    val pendingSmsSuggestions: StateFlow<List<SmsSuggestion>> = db.smsSuggestionDao().getPending()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Confirms a detected SMS transaction, turning it into a real transaction. */
    fun confirmSmsSuggestion(suggestion: SmsSuggestion, categoryId: Long, accountId: Long) {
        viewModelScope.launch {
            db.transactionDao().insert(
                Transaction(
                    amount = suggestion.amount,
                    type = suggestion.type,
                    categoryId = categoryId,
                    accountId = accountId,
                    note = "پیامک بانکی",
                    date = suggestion.date
                )
            )
            db.smsSuggestionDao().update(suggestion.copy(handled = true))
        }
    }

    /** Dismisses a detected SMS transaction without recording it. */
    fun dismissSmsSuggestion(suggestion: SmsSuggestion) {
        viewModelScope.launch {
            db.smsSuggestionDao().update(suggestion.copy(handled = true))
        }
    }

    fun addTransaction(
        amount: Long,
        type: TransactionType,
        categoryId: Long,
        accountId: Long,
        note: String,
        date: Long
    ) {
        viewModelScope.launch {
            db.transactionDao().insert(
                Transaction(
                    amount = amount,
                    type = type,
                    categoryId = categoryId,
                    accountId = accountId,
                    note = note,
                    date = date
                )
            )
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch { db.transactionDao().delete(transaction) }
    }

    fun addCategory(name: String, type: TransactionType, colorHex: String, monthlyBudget: Long) {
        viewModelScope.launch {
            db.categoryDao().insert(
                Category(name = name, type = type, colorHex = colorHex, monthlyBudget = monthlyBudget)
            )
        }
    }

    fun addAccount(name: String, initialBalance: Long, colorHex: String) {
        viewModelScope.launch {
            db.accountDao().insert(Account(name = name, initialBalance = initialBalance, colorHex = colorHex))
        }
    }

    fun accountBalance(accountId: Long, allTransactions: List<Transaction>, account: Account): Long {
        val delta = allTransactions.filter { it.accountId == accountId }.sumOf {
            if (it.type == TransactionType.INCOME) it.amount else -it.amount
        }
        return account.initialBalance + delta
    }
}
