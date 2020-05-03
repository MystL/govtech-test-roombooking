package com.vin.booking.utils.extensions

fun getDayOfMonthSuffix(n: Int): String? {
    if(n < 1 && n > 31) {
        throw IllegalArgumentException("illegal day of month: $n")
    }
    return if (n in 11..13) {
        "th"
    } else when (n % 10) {
        1 -> "st"
        2 -> "nd"
        3 -> "rd"
        else -> "th"
    }
}