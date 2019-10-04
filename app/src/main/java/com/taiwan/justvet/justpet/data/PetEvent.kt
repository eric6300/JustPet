package com.taiwan.justvet.justpet.data

import android.os.Parcelable
import com.google.firebase.firestore.Exclude
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PetEvent (
    @get:Exclude val petProfile: PetProfile? = null,
    val petId: String = "",
    val petName: String = "",
    val petSpecies: Long = 0,
    @get:Exclude val eventId: String = "",
    val timestamp: Long = 0,
    val year: Long = 0,
    val month: Long = 0,
    val dayOfMonth: Long = 0,
    val time: String = "",
    val eventType: Long? = 0,
    @get:Exclude val eventTags: List<EventTag>? = null,
    val eventTagsIndex: List<Long>? = null,
    val note: String? = "",
    val spirit: Double? = null,
    val appetite: Double? = null,
    val weight: Double = 0.0,
    val temperature: Double = 0.0,
    val respiratoryRate: Long = 0,
    val heartRate: Long = 0,
    val imageUrl: String? = null
): Parcelable