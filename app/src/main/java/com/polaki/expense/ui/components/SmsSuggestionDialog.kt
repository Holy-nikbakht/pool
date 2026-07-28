package com.polaki.expense.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.polaki.expense.data.Account
import com.polaki.expense.data.Category
import com.polaki.expense.data.SmsSuggestion
import com.polaki.expense.data.TransactionType
import com.polaki.expense.util.toFormattedToman

@Composable
fun SmsSuggestionDialog(
    suggestion: SmsSuggestion,
    categories: List<Category>,
    accounts: List<Account>,
    onConfirm: (categoryId: Long, accountId: Long) -> Unit,
    onDismiss: () -> Unit
) {
    val filteredCategories = categories.filter { it.type == suggestion.type }
    var selectedCategory by remember(suggestion.id) { mutableStateOf(filteredCategories.firstOrNull()) }
    var selectedAccount by remember(suggestion.id) { mutableStateOf(accounts.firstOrNull()) }

    val amountColor = if (suggestion.type == TransactionType.INCOME)
        MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
    val typeLabel = if (suggestion.type == TransactionType.INCOME) "واریز" else "برداشت"

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(24.dp),
        icon = { Icon(Icons.Default.Sms, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
        title = { Text("تراکنش از پیامک بانکی") },
        text = {
            Column {
                Text(
                    "یک $typeLabel به مبلغ ${suggestion.amount.toFormattedToman()} در پیامک بانکی شناسایی شد. می‌خوای ثبتش کنم؟",
                    color = amountColor,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(16.dp))

                if (filteredCategories.isNotEmpty()) {
                    Text("دسته‌بندی", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(filteredCategories) { category ->
                            val color = try {
                                Color(android.graphics.Color.parseColor(category.colorHex))
                            } catch (e: Exception) {
                                MaterialTheme.colorScheme.primary
                            }
                            val selected = category.id == selectedCategory?.id
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (selected) color.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { selectedCategory = category }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(category.name, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                if (accounts.isNotEmpty()) {
                    Text("حساب", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(accounts) { account ->
                            val selected = account.id == selectedAccount?.id
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .clickable { selectedAccount = account }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(account.name, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = selectedCategory != null && selectedAccount != null,
                onClick = {
                    val category = selectedCategory
                    val account = selectedAccount
                    if (category != null && account != null) {
                        onConfirm(category.id, account.id)
                    }
                }
            ) {
                Text("ثبت کن", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("نادیده بگیر", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}

