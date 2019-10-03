package com.taiwan.justvet.justpet.util

import android.text.format.DateFormat
import com.google.firebase.firestore.DocumentSnapshot
import com.taiwan.justvet.justpet.JustPetApplication
import com.taiwan.justvet.justpet.R
import com.taiwan.justvet.justpet.data.PetProfile
import java.text.SimpleDateFormat
import java.util.*

fun Long.timestampToDateString(): String {
    return DateFormat.format(
        JustPetApplication.appContext.getString(R.string.date_format),
        Calendar.getInstance().apply { timeInMillis = this@timestampToDateString * 1000 }
    ).toString()
}

fun Long.timestampToTimeString(): String {
    return DateFormat.format(
        JustPetApplication.appContext.getString(R.string.time_format),
        Calendar.getInstance().apply { timeInMillis = this@timestampToTimeString * 1000 }
    ).toString()
}

fun DocumentSnapshot.toPetProfile(): PetProfile {
    return PetProfile(
        profileId = this.id,
        name = this["name"] as String?,
        species = this["species"] as Long?,
        gender = this["gender"] as Long?,
        neutered = this["neutered"] as Boolean?,
        birthday = this["birthday"] as Long?,
        idNumber = this["idNumber"] as String?,
        owner = this["owner"] as String?,
        ownerEmail = this["ownerEmail"] as String?,
        family = this["family"] as List<String>?,
        image = this["image"] as String?
    )
}

fun Date.toFullTimeStringFormat(): String {
    return SimpleDateFormat(
        Util.getString(R.string.timelist_format),
        Locale.TAIWAN
    ).format(this)
}