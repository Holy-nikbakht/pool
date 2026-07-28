package com.polaki.expense.util

import java.text.NumberFormat
import java.util.Locale

private val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')

fun String.toPersianDigits(): String {
    val sb = StringBuilder()
    for (ch in this) {
        if (ch in '0'..'9') sb.append(persianDigits[ch - '0']) else sb.append(ch)
    }
    return sb.toString()
}

fun Long.toFormattedToman(): String {
    val formatter = NumberFormat.getNumberInstance(Locale.US)
    return formatter.format(this).toPersianDigits() + " تومان"
}

fun Long.toFormattedNumber(): String {
    val formatter = NumberFormat.getNumberInstance(Locale.US)
    return formatter.format(this).toPersianDigits()
}
