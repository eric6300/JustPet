package com.taiwan.justvet.justpet.data

import android.net.Uri
import android.os.Parcelable
import com.google.firebase.firestore.Exclude
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UserProfile(
    @get:Exclude val profileId: String? = null,
    val uid: String?,
    val email: String?,
    val pets: List<String>? = emptyList(),
    @get:Exclude val displayName: String? = null,
    @get:Exclude val photoUrl: Uri? = null
) : Parcelable