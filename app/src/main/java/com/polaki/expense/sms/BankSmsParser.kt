package com.polaki.expense.sms

import com.polaki.expense.data.TransactionType

data class ParsedSms(val amount: Long, val type: TransactionType)

/**
 * Heuristic parser for Iranian bank transaction SMS.
 *
 * Tuned against real samples from Blu Bank (بلو) and Bank Melli (بانک ملی).
 * Each bank has its own template, so this tries a few specific patterns first
 * (most reliable) and falls back to a generic keyword+number scan.
 *
 * As more bank samples come in, add a dedicated pattern for that bank near
 * the top of [parse] rather than stretching the generic fallback — dedicated
 * patterns are far less likely to misfire on unrelated numbers in the message
 * (account numbers, dates, OTP codes, balances).
 */
object BankSmsParser {

    // "رمز پویا" / "رمز: 123456" messages are one-time passcodes for a
    // purchase that hasn't necessarily completed yet — never treat these as
    // a finished transaction.
    private val otpPattern = Regex("""رمز\s*(پویا)?\s*:?\s*\d{4,6}""")
    private val otpTitlePattern = Regex("""رمز\s*پویا""")

    // Bank Melli style: "برداشت:2,000,000-" or "واریز:1,500,000"
    private val colonFormat = Regex("""(برداشت|واریز)\s*:\s*([\d,]{4,})""")

    // Blu Bank style: "19,000,000 ریال به حساب شما نشست." / "... از حساب شما پرید."
    private val rialFlowFormat = Regex(
        """([\d,]{4,})\s*ریال\s*(از حساب شما پرید|به حساب شما نشست)"""
    )

    // Generic fallback keywords, used only if the specific patterns above don't match.
    private val withdrawKeywords = listOf("برداشت", "خرید", "پرداخت", "کسر", "پرید")
    private val depositKeywords = listOf("واریز", "افزایش موجودی", "بستانکار", "نشست")
    private val genericAmountRegex = Regex("""([\d,٬]{4,})\s*(ریال|تومان)?""")

    fun parse(sender: String, body: String): ParsedSms? {
        if (otpTitlePattern.containsMatchIn(body) || otpPattern.containsMatchIn(body)) {
            return null
        }

        colonFormat.find(body)?.let { match ->
            val keyword = match.groupValues[1]
            val amount = toToman(match.groupValues[2]) ?: return@let
            val type = if (keyword == "واریز") TransactionType.INCOME else TransactionType.EXPENSE
            return ParsedSms(amount, type)
        }

        rialFlowFormat.find(body)?.let { match ->
            val amount = toToman(match.groupValues[1]) ?: return@let
            val type = if (match.groupValues[2] == "به حساب شما نشست") TransactionType.INCOME else TransactionType.EXPENSE
            return ParsedSms(amount, type)
        }

        val looksLikeBankSms = withdrawKeywords.any { body.contains(it) } ||
            depositKeywords.any { body.contains(it) }
        if (!looksLikeBankSms) return null

        val match = genericAmountRegex.find(body) ?: return null
        val amount = toToman(match.groupValues[1], explicitUnit = match.groupValues[2]) ?: return null

        val type = if (depositKeywords.any { body.contains(it) }) {
            TransactionType.INCOME
        } else {
            TransactionType.EXPENSE
        }
        return ParsedSms(amount, type)
    }

    /**
     * Bank SMS amounts are almost always quoted in Rial; the app tracks Toman,
     * so divide by 10 unless the message explicitly says "تومان".
     */
    private fun toToman(rawDigits: String, explicitUnit: String = ""): Long? {
        val digitsOnly = rawDigits.replace(",", "").replace("٬", "")
        var amount = digitsOnly.toLongOrNull() ?: return null
        if (explicitUnit != "تومان") {
            amount /= 10
        }
        return amount.takeIf { it > 0 }
    }
}
