package com.taiwan.justvet.justpet.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PetProfile(
    val id: String?,
    val name: String?,
    val species: Long?,
    val gender: Long?,
    val neutered: Boolean?,
    val birthDay: String?,
    val idNumber: String?,
    val owner: String?
) : Parcelable