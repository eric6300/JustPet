package com.taiwan.justvet.justpet.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FamilyInvite (
    val petId: String? = "",
    val petName: String? = "",
    val inviteeEmail: String? = "",
    val inviterName: String? = "",
    val inviterEmail: String? = ""
) : Parcelable