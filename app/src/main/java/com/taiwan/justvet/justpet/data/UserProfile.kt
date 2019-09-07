package com.taiwan.justvet.justpet.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UserProfile(
    val UID: String?,
    val email: String?,
    val pets: ArrayList<String>? = null
) : Parcelable