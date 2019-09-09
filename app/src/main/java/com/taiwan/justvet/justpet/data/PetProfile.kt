package com.taiwan.justvet.justpet.data

import android.os.Parcelable
import com.google.firebase.firestore.Exclude
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PetProfile(
    @get:Exclude val profileId: String? = null,
    val name: String?,
    val species: Long?,
    val gender: Long?,
    val neutered: Boolean? = false,
    val birthDay: String? = "",
    val idNumber: String? = "",
    val owner: String?
) : Parcelable