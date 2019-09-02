package com.taiwan.justvet.justpet.data

import android.os.Parcelable
import com.taiwan.justvet.justpet.util.TagType
import kotlinx.android.parcel.Parcelize

@Parcelize
data class EventTag(
    val type: TagType,
    val index: Int,
    val title: String,
    var isSelected: Boolean? = false
): Parcelable