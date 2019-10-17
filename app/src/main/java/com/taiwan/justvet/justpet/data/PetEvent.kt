package com.taiwan.justvet.justpet.data

import android.icu.util.Calendar
import android.os.Parcelable
import android.text.format.DateFormat
import com.google.firebase.firestore.Exclude
import com.taiwan.justvet.justpet.JustPetApplication
import com.taiwan.justvet.justpet.R
import com.taiwan.justvet.justpet.util.Util
import com.taiwan.justvet.justpet.util.toDateFormat
import com.taiwan.justvet.justpet.util.toMonthOnlyFormat
import kotlinx.android.parcel.Parcelize
import java.text.SimpleDateFormat
import java.util.*

@Parcelize
data class PetEvent(
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
    val weight: Double? = null,
    val temperature: Double? = null,
    val respiratoryRate: Long? = null,
    val heartRate: Long? = null,
    val imageUrl: String? = null
) : Parcelable {

    fun getDateOfEvent(): Date? {
        return java.util.Calendar.getInstance().apply { timeInMillis = timestamp * 1000 }.time
    }
}