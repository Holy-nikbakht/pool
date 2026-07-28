package com.polaki.expense

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.polaki.expense.ui.components.AddTransactionSheet
import com.polaki.expense.ui.components.SmsSuggestionDialog
import com.polaki.expense.ui.screens.AccountsScreen
import com.polaki.expense.ui.screens.BudgetScreen
import com.polaki.expense.ui.screens.HomeScreen
import com.polaki.expense.ui.theme.PulakiTheme

private enum class Tab { HOME, BUDGET, ACCOUNTS }

class MainActivity : ComponentActivity() {

    private val viewModel: PulakiViewModel by viewModels()

    private val requestSmsPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* granted or denied — either way, the rest of the app works fine without it */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val hasSmsPermission = ContextCompat.checkSelfPermission(
            this, Manifest.permission.RECEIVE_SMS
        ) == PackageManager.PERMISSION_GRANTED
        if (!hasSmsPermission) {
            requestSmsPermission.launch(Manifest.permission.RECEIVE_SMS)
        }

        setContent {
            PulakiTheme {
                // Force RTL layout for the whole app since content is Persian.
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    PulakiApp(viewModel)
                }
            }
        }
    }
}

@Composable
private fun PulakiApp(viewModel: PulakiViewModel) {
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val pendingSmsSuggestions by viewModel.pendingSmsSuggestions.collectAsStateWithLifecycle()

    var currentTab by remember { mutableStateOf(Tab.HOME) }
    var showAddSheet by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            Box {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    NavigationBarItem(
                        selected = currentTab == Tab.HOME,
                        onClick = { currentTab = Tab.HOME },
                        icon = { Icon(Icons.Default.Home, contentDescription = "خانه") },
                        label = { Text("خانه") }
                    )
                    Spacer(Modifier.width(56.dp)) // room for center FAB
                    NavigationBarItem(
                        selected = currentTab == Tab.BUDGET,
                        onClick = { currentTab = Tab.BUDGET },
                        icon = { Icon(Icons.Default.PieChart, contentDescription = "بودجه") },
                        label = { Text("بودجه") }
                    )
                    NavigationBarItem(
                        selected = currentTab == Tab.ACCOUNTS,
                        onClick = { currentTab = Tab.ACCOUNTS },
                        icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "حساب‌ها") },
                        label = { Text("حساب‌ها") }
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddSheet = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "افزودن تراکنش")
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (currentTab) {
                Tab.HOME -> HomeScreen(
                    transactions = transactions,
                    categories = categories,
                    searchQuery = searchQuery,
                    onSearchQueryChange = viewModel::setSearchQuery
                )
                Tab.BUDGET -> BudgetScreen(categories = categories, transactions = transactions)
                Tab.ACCOUNTS -> AccountsScreen(accounts = accounts, transactions = transactions)
            }
        }
    }

    if (showAddSheet) {
        AddTransactionSheet(
            categories = categories,
            accounts = accounts,
            onDismiss = { showAddSheet = false },
            onConfirm = { amount, type, categoryId, accountId, note ->
                viewModel.addTransaction(
                    amount = amount,
                    type = type,
                    categoryId = categoryId,
                    accountId = accountId,
                    note = note,
                    date = System.currentTimeMillis()
                )
                showAddSheet = false
            }
        )
    }

    // Show one SMS suggestion at a time (oldest first), asking the user to
    // confirm before it becomes a real transaction.
    val nextSuggestion = pendingSmsSuggestions.lastOrNull()
    if (nextSuggestion != null) {
        SmsSuggestionDialog(
            suggestion = nextSuggestion,
            categories = categories,
            accounts = accounts,
            onConfirm = { categoryId, accountId ->
                viewModel.confirmSmsSuggestion(nextSuggestion, categoryId, accountId)
            },
            onDismiss = { viewModel.dismissSmsSuggestion(nextSuggestion) }
        )
    }
}
