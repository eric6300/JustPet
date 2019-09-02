package com.taiwan.justvet.justpet.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PetEvent (
    val date: String? = null,
    val time: String? = null,
    val eventType: Int? = null,
    val tags: Int? = null,
    val note: String
): Parcelable