package com.taiwan.justvet.justpet.calendar

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.taiwan.justvet.justpet.*
import com.taiwan.justvet.justpet.data.EventTag
import com.taiwan.justvet.justpet.data.JustPetRepository
import com.taiwan.justvet.justpet.data.PetEvent
import com.taiwan.justvet.justpet.data.UserProfile
import com.taiwan.justvet.justpet.event.EventViewModel.Companion.APPETITE
import com.taiwan.justvet.justpet.event.EventViewModel.Companion.DAY_OF_MONTH
import com.taiwan.justvet.justpet.event.EventViewModel.Companion.EVENT_TAGS_INDEX
import com.taiwan.justvet.justpet.event.EventViewModel.Companion.HEART_RATE
import com.taiwan.justvet.justpet.event.EventViewModel.Companion.IMAGE_URL
import com.taiwan.justvet.justpet.event.EventViewModel.Companion.INDEX
import com.taiwan.justvet.justpet.event.EventViewModel.Companion.MONTH
import com.taiwan.justvet.justpet.event.EventViewModel.Companion.NOTE
import com.taiwan.justvet.justpet.event.EventViewModel.Companion.PET_ID
import com.taiwan.justvet.justpet.event.EventViewModel.Companion.PET_NAME
import com.taiwan.justvet.justpet.event.EventViewModel.Companion.PET_SPECIES
import com.taiwan.justvet.justpet.event.EventViewModel.Companion.RESPIRATORY_RATE
import com.taiwan.justvet.justpet.event.EventViewModel.Companion.SPIRIT
import com.taiwan.justvet.justpet.event.EventViewModel.Companion.TEMPERATURE
import com.taiwan.justvet.justpet.event.EventViewModel.Companion.TIME
import com.taiwan.justvet.justpet.event.EventViewModel.Companion.TIMESTAMP
import com.taiwan.justvet.justpet.event.EventViewModel.Companion.TITLE
import com.taiwan.justvet.justpet.event.EventViewModel.Companion.TYPE
import com.taiwan.justvet.justpet.event.EventViewModel.Companion.WEIGHT
import com.taiwan.justvet.justpet.event.EventViewModel.Companion.YEAR
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

    private val _navigateToEventFragment = MutableLiveData<PetEvent>()
    val navigateToEventFragment: LiveData<PetEvent>
        get() = _navigateToEventFragment

    private val _showDeleteDialog = MutableLiveData<PetEvent>()
    val showDeleteDialog: LiveData<PetEvent>
        get() = _showDeleteDialog

    val localDate = LocalDate.now()

    private val petsReference = JustPetRepository.firestoreInstance.collection(PETS)

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
                    petsReference.document(petId).collection(EVENTS)
                        .whereEqualTo(YEAR, year)
                        .whereEqualTo(MONTH, month)
                        .get()
                        .addOnSuccessListener { document ->
                            Log.d(ERIC, "get months data success, data size = ${document.size()}")
                            if (document.size() > 0) {
                                for (event in document) {
                                    data.add(
                                        PetEvent(
                                            eventId = event.id,
                                            petId = event[PET_ID] as String,
                                            petName = event[PET_NAME] as String,
                                            petSpecies = event[PET_SPECIES] as Long,
                                            timestamp = event[TIMESTAMP] as Long,
                                            year = event[YEAR] as Long,
                                            month = event[MONTH] as Long,
                                            dayOfMonth = event[DAY_OF_MONTH] as Long,
                                            time = event[TIME] as String,
                                            eventTagsIndex = event[EVENT_TAGS_INDEX] as List<Long>?,
                                            note = event[NOTE] as String?,
                                            spirit = event[SPIRIT] as Double?,
                                            appetite = event[APPETITE] as Double?,
                                            weight = event[WEIGHT] as Double?,
                                            temperature = event[TEMPERATURE] as Double?,
                                            respiratoryRate = event[RESPIRATORY_RATE] as Long?,
                                            heartRate = event[HEART_RATE] as Long?,
                                            imageUrl = event[IMAGE_URL] as String?
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
        var index = 1
        for (event in data) {
            val tagList = mutableListOf<EventTag>()
            petsReference.document(event.petId).collection(EVENTS).document(event.eventId)
                .collection(TAGS)
                .get()
                .addOnSuccessListener { document ->

                    for (tag in document) {
                        tagList.add(
                            EventTag(
                                type = tag[TYPE] as String?,
                                index = tag[INDEX] as Long?,
                                title = tag[TITLE] as String?
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
                            note = event.note,
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

                    if (index == data.size) {
                        _monthEventsData.value = finalMonthEventData.sortedBy {
                            it.timestamp
                        }
                    }

                    index++
                }
                .addOnFailureListener {
                    Log.d(ERIC, "getEventWithTags() failed : $it")
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
        petsReference.document(petEvent.petId).collection(EVENTS).document(petEvent.eventId).collection(TAGS)
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

    private fun deleteEvent(petEvent: PetEvent) {
        petsReference.document(petEvent.petId).collection(EVENTS).document(petEvent.eventId).delete()
            .addOnSuccessListener {
                refreshEventData()
            }
            .addOnFailureListener {
                Log.d(ERIC, "deleteEvent() failed : $it")
            }
    }

    private fun deleteTags(petEvent: PetEvent, documentId: String) {
        petsReference.document(petEvent.petId).collection(EVENTS).document(petEvent.eventId).collection(TAGS)
            .document(documentId).delete()
            .addOnSuccessListener {
                Log.d(ERIC, "deleteTags() succeeded")
            }
            .addOnFailureListener {
                Log.d(ERIC, "deleteTags() failed : $it")
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

    fun navigateToEventFragment(petEvent: PetEvent) {
        _navigateToEventFragment.value = petEvent
    }

    fun navigateToEventFragmentCompleted() {
        _navigateToEventFragment.value = null
    }
}
