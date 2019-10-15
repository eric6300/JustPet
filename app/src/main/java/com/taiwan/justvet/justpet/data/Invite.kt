package com.taiwan.justvet.justpet.data

import android.os.Parcelable
import com.google.firebase.firestore.Exclude
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Invite (
    @get: Exclude val inviteId: String? = "",
    val petId: String? = "",
    val petName: String? = "",
    val inviteeEmail: String? = "",
    val inviterName: String? = "",
    val inviterEmail: String? = ""
) : Parcelable