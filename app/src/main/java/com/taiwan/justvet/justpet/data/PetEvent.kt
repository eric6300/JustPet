package com.taiwan.justvet.justpet.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PetEvent (
    val petId: String? = null,
    val timeStamp: Long? = null,
    val year: Int,
    val month: Int,
    val dayOfMonth: Int,
    val timeString: String,
    val eventType: Int? = 0,
    val eventTags: List<EventTag>? = null,
    val tagTitleList: List<String>? = null,
    val note: String? = null
): Parcelable