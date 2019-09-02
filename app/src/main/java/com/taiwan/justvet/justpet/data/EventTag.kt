package com.taiwan.justvet.justpet.data

import com.taiwan.justvet.justpet.util.TagType

data class EventTag(
    val type: TagType,
    val index: Int,
    val title: String,
    var isSelected: Boolean? = false
)