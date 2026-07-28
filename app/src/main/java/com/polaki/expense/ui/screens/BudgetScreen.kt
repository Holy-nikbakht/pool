package com.polaki.expense.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.polaki.expense.data.Category
import com.polaki.expense.data.Transaction
import com.polaki.expense.data.TransactionType
import com.polaki.expense.util.toFormattedToman
import java.util.Calendar

@Composable
fun BudgetScreen(categories: List<Category>, transactions: List<Transaction>) {
    val budgeted = categories.filter { it.type == TransactionType.EXPENSE && it.monthlyBudget > 0 }

    val cal = Calendar.getInstance()
    cal.set(Calendar.DAY_OF_MONTH, 1)
    cal.set(Calendar.HOUR_OF_DAY, 0)
    val monthStart = cal.timeInMillis
    val monthEnd = System.currentTimeMillis()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text("بودجه‌بندی ماهانه", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))

        if (budgeted.isEmpty()) {
            Text(
                "برای هر دسته می‌تونی سقف هزینه ماهانه تعیین کنی تا قبل از تموم شدنش هشدار بگیری.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(budgeted) { category ->
                    val spent = transactions.filter {
                        it.categoryId == category.id &&
                            it.type == TransactionType.EXPENSE &&
                            it.date in monthStart..monthEnd
                    }.sumOf { it.amount }

                    val ratio = (spent.toFloat() / category.monthlyBudget.toFloat()).coerceIn(0f, 1.5f)
                    val color = try {
                        Color(android.graphics.Color.parseColor(category.colorHex))
                    } catch (e: Exception) {
                        MaterialTheme.colorScheme.primary
                    }
                    val barColor = if (ratio >= 1f) MaterialTheme.colorScheme.secondary else color

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(16.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(category.name, fontWeight = FontWeight.SemiBold)
                            Text(
                                "${spent.toFormattedToman()} / ${category.monthlyBudget.toFormattedToman()}",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { ratio.coerceAtMost(1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = barColor,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        if (ratio >= 0.85f) {
                            Spacer(Modifier.height(6.dp))
                            Text(
                                if (ratio >= 1f) "بودجه این دسته تموم شده!" else "نزدیک سقف بودجه‌ای",
                                color = MaterialTheme.colorScheme.secondary,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        }
    }
}
