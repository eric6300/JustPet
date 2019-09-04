package com.taiwan.justvet.justpet.data

data class PetProfile(
    val name: String?,
    val species: Long?,
    val gender: Long?,
    val neutered: Boolean?,
    val birthDay: String?,
    val idNumber: String?,
    val owner: String?
)