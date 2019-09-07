package com.taiwan.justvet.justpet.calendar

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.taiwan.justvet.justpet.EVENTS
import com.taiwan.justvet.justpet.PETS
import com.taiwan.justvet.justpet.TAG
import com.taiwan.justvet.justpet.TAGS
import com.taiwan.justvet.justpet.data.EventTag
import com.taiwan.justvet.justpet.data.PetEvent
import com.taiwan.justvet.justpet.data.UserProfile
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate

class CalendarViewModel : ViewModel() {

    private val _monthEventsData = MutableLiveData<List<PetEvent>>()
    val monthEventsData: LiveData<List<PetEvent>>
        get() = _monthEventsData

    private val _dayEventsData = MutableLiveData<List<PetEvent>>()
    val dayEventsData: LiveData<List<PetEvent>>
        get() = _dayEventsData

    private val _decorateListOfEvents = MutableLiveData<List<PetEvent>>()
    val decorateListOfEvents: LiveData<List<PetEvent>>
        get() = _decorateListOfEvents

    private val _refreshEventData = MutableLiveData<Boolean>()
    val refreshEventData: LiveData<Boolean>
        get() = _refreshEventData

    val localDate = LocalDate.now()

    val firebase = FirebaseFirestore.getInstance()
    val pets = firebase.collection(PETS)

    init {
        getMonthEventsData(mockUser(), localDate.year.toLong(), localDate.monthValue.toLong())
    }

    fun mockUser(): UserProfile {
        val petList = ArrayList<String>()
        petList.let {
            it.add("5DjrhdAlZka29LSmOe12")
            it.add("BR1unuBGFmeioH4VpKc2")
            it.add("FeHxkWD6VwpPMtL2bZT4")
        }
        return UserProfile("Vn4lVYwPEM9RBoQXjyTr","eric6300", "6300eric@gmail.com", petList)
    }

    fun getMonthEventsData(UserProfile: UserProfile, year: Long, month: Long) {
        UserProfile.pets?.let {
            viewModelScope.launch {
                val data = mutableListOf<PetEvent>()
                for (petId in UserProfile.pets) {
                    pets.document(petId).collection(EVENTS)
                        .whereEqualTo("year", year)
                        .whereEqualTo("month", month)
                        .get()
                        .addOnSuccessListener { document ->
                            if (document.size() > 0) {
                                Log.d(TAG, "=== pet id : $petId ===")
                                for (event in document) {
                                    Log.d(TAG, "event id : ${event.id}")
                                    data.add(
                                        PetEvent(
                                            eventId = event.id,
                                            petId = event["petId"] as String?,
                                            petName = event["petName"] as String?,
                                            timestamp = event["timestamp"] as Long,
                                            year = event["year"] as Long,
                                            month = event["month"] as Long,
                                            dayOfMonth = event["dayOfMonth"] as Long,
                                            time = event["time"] as String
                                        )
                                    )
                                }
                                getEventWithTags(data)
                            }
                        }
                        .addOnFailureListener {
                            Log.d(TAG, "getMonthEventsData() failed : $it")
                        }
                }
            }
        }
    }

    fun getEventWithTags(data: List<PetEvent>) {
        val finalMonthEventData = mutableListOf<PetEvent>()
        for (event in data) {
            event.petId?.let {
                event.eventId?.let {
                    val tagList = mutableListOf<EventTag>()
                    pets.document(event.petId).collection(EVENTS).document(event.eventId)
                        .collection(TAGS)
                        .get()
                        .addOnSuccessListener { document ->
                            if (document.size() > 0) {
                                for (tag in document) {
                                    tagList.add(
                                        EventTag(
                                            type = tag["type"] as String?,
                                            index = tag["index"] as Long?,
                                            title = tag["title"] as String?,
                                            isSelected = tag["isSelected"] as Boolean?
                                        )
                                    )
                                }
                                finalMonthEventData.add(
                                    PetEvent(
                                        petId = event.petId,
                                        petName = event.petName,
                                        eventId = event.eventId,
                                        timestamp = event.timestamp,
                                        year = event.year,
                                        month = event.month,
                                        dayOfMonth = event.dayOfMonth,
                                        time = event.time,
                                        eventTags = tagList
                                    )
                                )
                                _monthEventsData.value = finalMonthEventData
                            }
                        }
                        .addOnFailureListener {
                            Log.d(TAG, "getEventWithTags() failed : $it")
                        }
                }
            }
        }
    }

    fun getDecorationEvents(eventList: List<PetEvent>?) {
        eventList?.let {
            _decorateListOfEvents.value = eventList
        }
    }

    fun dayEventsFilter(year: Long, month: Long, dayOfMonth: Long?, events: List<PetEvent>?) {
        events?.let { eventList ->
            if (dayOfMonth != null) {
                val newList = eventList.filter { event ->
                    (event.year == year) && (event.month == month) && (event.dayOfMonth == dayOfMonth)
                }
                _dayEventsData.value = newList
            }
        }
    }

    fun deleteEvent(petEvent: PetEvent) {
        petEvent.petId?.let { petId ->
            petEvent.eventId?.let { eventId ->
                pets.document(petId).collection(EVENTS).document(eventId).delete()
                    .addOnSuccessListener {
                        deleteEventTags(petEvent)
                    }
                    .addOnFailureListener {
                        Log.d(TAG, "deleteEvent() failed : $it")
                    }
            }
        }
    }

    private fun deleteEventTags(petEvent: PetEvent) {
        petEvent.petId?.let { petId ->
            petEvent.eventId?.let { eventId ->
                pets.document(petId).collection(EVENTS).document(eventId).collection(TAGS)
                    .document().delete()
                    .addOnSuccessListener {
                        refreshEventData()
                    }
                    .addOnFailureListener {
                        Log.d(TAG, "deleteEvent() failed : $it")
                    }
            }
        }
    }

    fun default() {
        _dayEventsData.value = ArrayList()
        _monthEventsData.value = ArrayList()
    }

    private fun refreshEventData() {
        _refreshEventData.value = true
    }

    fun refreshEventDataCompleted() {
        _refreshEventData.value = false
    }
}
