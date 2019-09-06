package com.taiwan.justvet.justpet.calendar

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.taiwan.justvet.justpet.data.EventTag
import com.taiwan.justvet.justpet.data.PetEvent
import com.taiwan.justvet.justpet.data.userProfile
import com.taiwan.justvet.justpet.home.TAG
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate

class CalendarViewModel : ViewModel() {

    private val _eventsData = MutableLiveData<List<PetEvent>>()
    val eventsData: LiveData<List<PetEvent>>
        get() = _eventsData

    private val _filteredEvents = MutableLiveData<List<PetEvent>>()
    val filteredEvents: LiveData<List<PetEvent>>
        get() = _filteredEvents

    private val _decorateListOfEvents = MutableLiveData<List<PetEvent>>()
    val decorateListOfEvents: LiveData<List<PetEvent>>
        get() = _decorateListOfEvents

    private val _firstTimeDecoration = MutableLiveData<List<PetEvent>>()
    val firstTimeDecoration: LiveData<List<PetEvent>>
        get() = _firstTimeDecoration

    val localDate = LocalDate.now()

    val firebase = FirebaseFirestore.getInstance()
    val pets = firebase.collection("pets")

    init {
        getPetEventsData(mockUser())
    }

    fun mockUser(): userProfile {
        val petList = ArrayList<String>()
        petList.let {
            it.add("5DjrhdAlZka29LSmOe12")
            it.add("BR1unuBGFmeioH4VpKc2")
            it.add("FeHxkWD6VwpPMtL2bZT4")
        }
        return userProfile("eric6300", "6300eric@gmail.com", petList)
    }

    fun getPetEventsData(userProfile: userProfile) {
        userProfile.pets?.let {
            viewModelScope.launch {
                val data = mutableListOf<PetEvent>()
                for (petId in userProfile.pets) {
                    pets.document(petId).collection("events")
                        .get()
                        .addOnSuccessListener { document ->
                            for (event in document) {
                                Log.d(TAG, "${event.id}")
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
                        .addOnFailureListener {
                            Log.d(TAG, "Failed")
                        }
                }
            }
        }
    }

    fun getEventWithTags(data: List<PetEvent>) {
        val finalEventData = mutableListOf<PetEvent>()
        for (event in data) {
            event.petId?.let {
                event.eventId?.let {
                    val tagList = mutableListOf<EventTag>()
                    pets.document(event.petId).collection("events").document(event.eventId)
                        .collection("tags")
                        .get()
                        .addOnSuccessListener { document ->
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
                            finalEventData.add(
                                PetEvent(
                                    petId = event.petId,
                                    petName = event.petName,
                                    timestamp = event.timestamp,
                                    year = event.year,
                                    month = event.month,
                                    dayOfMonth = event.dayOfMonth,
                                    time = event.time,
                                    eventTags = tagList
                                )
                            )
                            _eventsData.value = finalEventData
                        }
                        .addOnFailureListener {
                            Log.d(TAG, "Failed")
                        }
                }
            }
        }
    }

    fun getDecotaionEvents(finalEventData: List<PetEvent>?) {
        finalEventData?.let {
            val decorationEvents = it.filter { event ->
                (event.year == localDate.year.toLong()) && (event.month == localDate.monthValue.toLong())
            }
            _firstTimeDecoration.value = decorationEvents
        }
    }

    fun eventFilter(year: Long, month: Long, dayOfMonth: Long?, events: List<PetEvent>?) {
        events?.let { eventList ->
            if (dayOfMonth != null) {
                val newList = eventList.filter { event ->
                    (event.year == year) && (event.month == month) && (event.dayOfMonth == dayOfMonth)
                }
                _filteredEvents.value = newList
            } else {
                // Get decoration list of the month
                val newList = _eventsData.value?.filter { event ->
                    (event.year == year) && (event.month == month)
                }
                _decorateListOfEvents.value = newList
            }
        }
    }
}