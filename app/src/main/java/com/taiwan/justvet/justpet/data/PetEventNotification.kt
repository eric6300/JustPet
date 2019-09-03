package com.taiwan.justvet.justpet.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PetEventNotification(
    val notificationId: Long? = null,
    val type: Int,
    val title: String,
    val timeStamp: Long
): Parcelable