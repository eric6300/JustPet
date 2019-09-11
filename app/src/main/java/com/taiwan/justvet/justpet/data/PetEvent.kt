package com.taiwan.justvet.justpet.data

import android.os.Parcelable
import com.google.firebase.firestore.Exclude
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PetEvent (
    @get:Exclude val petProfile: PetProfile? = null,
    val petId: String? = "",
    val petName: String? = "",
    @get:Exclude val eventId: String? = null,
    val timestamp: Long,
    val year: Long,
    val month: Long,
    val dayOfMonth: Long,
    val time: String,
    val eventType: Long? = 0,
    @get:Exclude val eventTags: List<EventTag>? = null,
    val note: String? = "",
    val spirit: Double? = null,
    val appetite: Double? = null,
    val weight: String? = null,
    val temperature: String? = null,
    val respiratoryRate: String? = null,
    val heartRate: String? = null
): Parcelable