package com.taiwan.justvet.justpet.data

data class PetEvent (
    val date: String? = null,
    val time: String? = null,
    val type: Int,
    val tag: String,
    val note: String
)