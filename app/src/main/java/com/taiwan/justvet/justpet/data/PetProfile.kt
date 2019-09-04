package com.taiwan.justvet.justpet.data

data class PetProfile(
    val name: String,
    val species: Int,
    val gender: Int,
    val birthDay: String? = null,
    val idNumber: String? = null
)