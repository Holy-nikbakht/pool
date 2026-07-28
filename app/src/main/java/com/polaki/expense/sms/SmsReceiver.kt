package com.polaki.expense.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.polaki.expense.data.AppDatabase
import com.polaki.expense.data.SmsSuggestion
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isNullOrEmpty()) return

        // Multi-part SMS: concatenate bodies, keep the sender of the first part.
        val sender = messages.first().originatingAddress ?: "نامشخص"
        val fullBody = messages.joinToString(separator = "") { it.messageBody ?: "" }

        val parsed = BankSmsParser.parse(sender, fullBody) ?: return

        val appContext = context.applicationContext
        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getInstance(appContext)
            db.smsSuggestionDao().insert(
                SmsSuggestion(
                    amount = parsed.amount,
                    type = parsed.type,
                    sender = sender,
                    rawMessage = fullBody,
                    date = System.currentTimeMillis()
                )
            )
        }
    }
}
