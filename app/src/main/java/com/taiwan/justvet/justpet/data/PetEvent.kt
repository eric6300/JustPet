package com.taiwan.justvet.justpet.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PetEvent (
    val date: String? = null,
    val time: String? = null,
    val type: Int,
    val tag: Int,
    val note: String
): Parcelable