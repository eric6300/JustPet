package com.taiwan.justvet.justpet.data

data class PetProfile(
    val name: String,
    val species: Int,
    val gender: Int,
    val idChip: String? = null,
    val image: String? = null,
    val petEvents: List<PetEvent>? = null
)