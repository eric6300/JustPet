package com.taiwan.justvet.justpet.util

import android.text.format.DateFormat
import com.taiwan.justvet.justpet.JustPetApplication
import com.taiwan.justvet.justpet.R
import java.util.*

fun Long.timestampToDateString(): String {
    return DateFormat.format(
        JustPetApplication.appContext.getString(R.string.date_format),
        Calendar.getInstance().apply { timeInMillis = this@timestampToDateString }
    ).toString()
}

fun Long.timestampToTimeString(): String {
    return DateFormat.format(
        JustPetApplication.appContext.getString(R.string.time_format),
        Calendar.getInstance().apply { timeInMillis = this@timestampToTimeString }
    ).toString()
}