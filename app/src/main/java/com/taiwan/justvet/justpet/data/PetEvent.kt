package com.taiwan.justvet.justpet.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PetEvent (
    val timeStamp: Long? = null,
    val year: Int,
    val month: Int,
    val dayOfMonth: Int? = null,
    val timeString: String? = null,
    val eventType: Int? = null,
    val tags: List<EventTag>? = null,
    val note: String? = null
): Parcelable