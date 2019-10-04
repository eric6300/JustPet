package com.taiwan.justvet.justpet.util

import android.text.format.DateFormat
import com.google.firebase.firestore.DocumentSnapshot
import com.taiwan.justvet.justpet.JustPetApplication
import com.taiwan.justvet.justpet.R
import com.taiwan.justvet.justpet.data.PetProfile
import com.taiwan.justvet.justpet.util.Util.getString
import java.text.SimpleDateFormat
import java.util.*

fun Long.toDateString(): String {
    return DateFormat.format(
        JustPetApplication.appContext.getString(R.string.date_format),
        Calendar.getInstance().apply { timeInMillis = this@toDateString * 1000 }
    ).toString()
}

fun Long.toDateFormat(): Date {
    return Calendar.getInstance(Locale.getDefault()).apply {
        this.timeInMillis = this@toDateFormat * 1000
    }.time
}

fun Date.toFullDateTimeFormat(): String {
    return SimpleDateFormat(
        Util.getString(R.string.time_list_format),
        Locale.getDefault()
    ).format(this)
}

fun Date.toDateFormat(): String {
    return SimpleDateFormat(
        Util.getString(R.string.date_format),
        Locale.getDefault()
    ).format(this)
}

fun String.toTimestamp(): Long {
    return SimpleDateFormat(
        getString(R.string.date_format),
        Locale.getDefault()
    ).parse(this)?.time?.div(1000)  ?: 0
}

fun DocumentSnapshot.toPetProfile(): PetProfile {
    return PetProfile(
        profileId = this.id,
        name = this["name"] as String?,
        species = this["species"] as Long?,
        gender = this["gender"] as Long?,
        neutered = this["neutered"] as Boolean?,
        birthday = this["birthday"] as Long,
        idNumber = this["idNumber"] as String?,
        owner = this["owner"] as String?,
        ownerEmail = this["ownerEmail"] as String?,
        family = this["family"] as List<String>?,
        image = this["image"] as String?
    )
}