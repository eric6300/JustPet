package com.taiwan.justvet.justpet.data

import android.os.Parcelable
import com.google.firebase.firestore.Exclude
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UserProfile(
    @get:Exclude val profileId: String? = null,
    val UID: String?,
    val email: String?,
    @get:Exclude val pets: List<String>? = null
) : Parcelable