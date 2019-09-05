package com.taiwan.justvet.justpet.data

import android.os.Parcelable
import com.google.firebase.firestore.Exclude
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PetEvent (
    val petProfile: PetProfile? = null,
    val timestamp: Long,
    val year: Long,
    val month: Long,
    val dayOfMonth: Long,
    val time: String,
    val eventType: Long? = 0,
    @get:Exclude val eventTags: List<EventTag>? = null,
    val tagTitleList: List<String>? = null,
    val note: String? = null
): Parcelable