package com.taiwan.justvet.justpet.calendar

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.taiwan.justvet.justpet.*
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

    private val _navigateToDetail = MutableLiveData<PetEvent>()
    val navigateToDetail: LiveData<PetEvent>
        get() = _navigateToDetail

    private val _showDeleteDialog = MutableLiveData<PetEvent>()
    val showDeleteDialog: LiveData<PetEvent>
        get() = _showDeleteDialog

    val localDate = LocalDate.now()

    val firebase = FirebaseFirestore.getInstance()
    val pets = firebase.collection(PETS)

    init {
        UserManager.userProfile.value?.let {
            getMonthEventsData(it, localDate.year.toLong(), localDate.monthValue.toLong())
        }
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
                                for (event in document) {
                                    data.add(
                                        PetEvent(
                                            eventId = event.id,
                                            petId = event["petId"] as String?,
                                            petName = event["petName"] as String?,
                                            petSpecies = event["petSpecies"] as Long?,
                                            timestamp = event["timestamp"] as Long?,
                                            year = event["year"] as Long,
                                            month = event["month"] as Long,
                                            dayOfMonth = event["dayOfMonth"] as Long,
                                            time = event["time"] as String,
                                            eventTagsIndex = event["eventTagsIndex"] as List<Long>?,
                                            note = event["note"] as String?,
                                            spirit = event["spirit"] as Double?,
                                            appetite = event["appetite"] as Double?,
                                            weight = event["weight"] as String?,
                                            temperature = event["temperature"] as String?,
                                            respiratoryRate = event["respiratoryRate"] as String?,
                                            heartRate = event["heartRate"] as String?,
                                            imageUrl = event["imageUrl"] as String?
                                        )
                                    )
                                }
                                getEventWithTags(data)
                            }
                        }
                        .addOnFailureListener {
                            Log.d(ERIC, "getMonthEventsData() failed : $it")
                        }
                }
            }
        }
    }

    fun getEventWithTags(data: List<PetEvent>) {
        val finalMonthEventData = mutableListOf<PetEvent>()
        var index = 0
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
                                        petSpecies = event.petSpecies,
                                        eventId = event.eventId,
                                        timestamp = event.timestamp,
                                        year = event.year,
                                        month = event.month,
                                        dayOfMonth = event.dayOfMonth,
                                        time = event.time,
                                        eventTagsIndex = event.eventTagsIndex,
                                        spirit = event.spirit,
                                        appetite = event.appetite,
                                        weight = event.weight,
                                        temperature = event.temperature,
                                        respiratoryRate = event.respiratoryRate,
                                        heartRate = event.heartRate,
                                        imageUrl = event.imageUrl,
                                        eventTags = tagList
                                    )
                                )
                                index++
                                if (index == data.size) {
                                    _monthEventsData.value = finalMonthEventData.sortedBy {
                                        it.timestamp
                                    }
                                }
                            }
                        }
                        .addOnFailureListener {
                            Log.d(ERIC, "getEventWithTags() failed : $it")
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

    fun showDeleteDialog(petEvent: PetEvent) {
        _showDeleteDialog.value = petEvent
    }

    fun showDeleteDialogCompleted() {
        _showDeleteDialog.value = null
    }

    fun getEventTagsToDelete(petEvent: PetEvent) {
        petEvent.petId?.let { petId ->
            petEvent.eventId?.let { eventId ->
                pets.document(petId).collection(EVENTS).document(eventId).collection(TAGS)
                    .get()
                    .addOnSuccessListener {
                        for (item in it) {
                            deleteTags(petEvent, item.id)
                        }
                        deleteEvent(petEvent)
                    }
                    .addOnFailureListener {
                        Log.d(ERIC, "deleteEvent() failed : $it")
                    }
            }
        }
    }

    private fun deleteEvent(petEvent: PetEvent) {
        petEvent.petId?.let { petId ->
            petEvent.eventId?.let { eventId ->
                pets.document(petId).collection(EVENTS).document(eventId).delete()
                    .addOnSuccessListener {
                        refreshEventData()
                    }
                    .addOnFailureListener {
                        Log.d(ERIC, "deleteEvent() failed : $it")
                    }
            }
        }
    }

    private fun deleteTags(petEvent: PetEvent, documentId: String) {
        petEvent.petId?.let { petId ->
            petEvent.eventId?.let { eventId ->
                pets.document(petId).collection(EVENTS).document(eventId).collection(TAGS)
                    .document(documentId).delete()
                    .addOnSuccessListener {
                        Log.d(ERIC, "deleteTags() succeeded")
                    }
                    .addOnFailureListener {
                        Log.d(ERIC, "deleteTags() failed : $it")
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

    fun navigateToDetail(petEvent: PetEvent) {
        _navigateToDetail.value = petEvent
        Log.d(ERIC, "navigateToDetail")
    }

    fun navigateToDetailCompleted() {
        _navigateToDetail.value = null
    }
}
