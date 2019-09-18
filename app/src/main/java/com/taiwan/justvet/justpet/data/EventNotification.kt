package com.taiwan.justvet.justpet.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class EventNotification(
    val type: Int,
    val title: String
) : Parcelable