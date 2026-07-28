package com.polaki.expense.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.polaki.expense.data.Category
import com.polaki.expense.data.Transaction
import com.polaki.expense.data.TransactionType
import com.polaki.expense.util.toFormattedToman
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TransactionRow(transaction: Transaction, category: Category?) {
    val color = try {
        Color(android.graphics.Color.parseColor(category?.colorHex ?: "#1AD1A5"))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }
    val sign = if (transaction.type == TransactionType.INCOME) "+" else "-"
    val amountColor = if (transaction.type == TransactionType.INCOME)
        MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                category?.name ?: "بدون دسته",
                style = MaterialTheme.typography.titleMedium
            )
            if (transaction.note.isNotBlank()) {
                Text(
                    transaction.note,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                formatDate(transaction.date),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Text(
            "$sign${transaction.amount.toFormattedToman()}",
            color = amountColor,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun formatDate(millis: Long): String {
    val sdf = SimpleDateFormat("yyyy/MM/dd", Locale.US)
    return sdf.format(Date(millis))
}
