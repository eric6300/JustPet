package com.taiwan.justvet.justpet.data

import android.os.Parcelable
import com.google.firebase.firestore.Exclude
import kotlinx.android.parcel.Parcelize

@Parcelize
data class EventTag(
    val type: String?,
    val index: Long?,
    val title: String?,
    @get:Exclude var isSelected: Boolean = false
): Parcelable