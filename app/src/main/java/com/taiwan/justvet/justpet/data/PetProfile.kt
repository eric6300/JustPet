package com.taiwan.justvet.justpet.data

import android.os.Parcelable
import com.google.firebase.firestore.Exclude
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PetProfile(
    @get:Exclude val profileId: String? = null,
    val name: String? = "",
    val species: Long? = 0,
    val gender: Long? = 0,
    val neutered: Boolean? = false,
    val birthday: Long? = 0,
    val idNumber: String? = "",
    val owner: String?,
    val ownerEmail: String?,
    val family: List<String>? = mutableListOf(),
    val image: String? = null,
    @get:Exclude val notifications: List<EventNotification>? = null
) : Parcelable