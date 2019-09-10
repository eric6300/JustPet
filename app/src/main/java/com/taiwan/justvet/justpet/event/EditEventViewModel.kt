package com.taiwan.justvet.justpet.event

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.taiwan.justvet.justpet.EVENTS
import com.taiwan.justvet.justpet.PETS
import com.taiwan.justvet.justpet.TAG
import com.taiwan.justvet.justpet.TAGS
import com.taiwan.justvet.justpet.data.EventTag
import com.taiwan.justvet.justpet.data.PetEvent

class EditEventViewModel(val petEvent: PetEvent) : ViewModel() {

    private val _navigateToCalendar = MutableLiveData<Boolean>()
    val navigateToCalendar: LiveData<Boolean>
        get() = _navigateToCalendar

    private val _expandStatus = MutableLiveData<Boolean>()
    val expandStatus: LiveData<Boolean>
        get() = _expandStatus

    private val _eventTags = MutableLiveData<List<EventTag>>()
    val eventTags: LiveData<List<EventTag>>
        get() = _eventTags

    private val _dateAndTime = MutableLiveData<String>()
    val dateAndTime: LiveData<String>
        get() = _dateAndTime

    val eventNote = MutableLiveData<String>()

    val firebase = FirebaseFirestore.getInstance()
    val eventDatabase = petEvent.petProfile?.profileId?.let { petId ->
        firebase.collection(PETS).document(petId).collection(EVENTS)
    }

    init {
        setEventTags()
        setDateAndTime()
    }

    private fun setEventTags() {
        _eventTags.value = petEvent.eventTags
    }

    private fun setDateAndTime() {
        _dateAndTime.value =
            "${petEvent.year}年${petEvent.month}月${petEvent.dayOfMonth}日 ${petEvent.time}"
    }

    fun postEvent() {
        val finalEvent = petEvent.let {
            PetEvent(
                petProfile = it.petProfile,
                petId = it.petId,
                petName = it.petName,
                timestamp = it.timestamp,
                year = it.year,
                month = it.month,
                dayOfMonth = it.dayOfMonth,
                time = it.time,
                eventType = it.eventType,
                eventTags = it.eventTags,
                note = eventNote.value
            )
        }
        eventDatabase?.let {
            it.add(finalEvent)
                .addOnSuccessListener { documentReference ->
                    Log.d(TAG, "postEvent succeeded ID : ${documentReference.id}")
                    navigateToCalendar()
                    postTags(documentReference.id)
                }
                .addOnFailureListener { e ->
                    Log.d(TAG, "postEvent failed : $e")
                }
        }
    }

    fun postTags(eventId: String) {
        petEvent.eventTags?.apply {
            eventDatabase?.let {
                for (tag in this) {
                    it.document(eventId).collection(TAGS).add(tag)
                        .addOnSuccessListener {
                            Log.d(TAG, "postTags succeeded ID : ${it.id}")
                            navigateToCalendar()
                        }
                        .addOnFailureListener {
                            Log.d(TAG, "postTags failed : $it")
                        }
                }
            }
        }
    }

    fun navigateToCalendar() {
        _navigateToCalendar.value = true
    }

    fun navigateToCalendarCompleted() {
        _navigateToCalendar.value = null
    }

    fun expandAdvanceMenu() {
        _expandStatus.value = _expandStatus.value != true
    }

}