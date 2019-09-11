package com.taiwan.justvet.justpet.event

import android.icu.util.Calendar
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.taiwan.justvet.justpet.*
import com.taiwan.justvet.justpet.data.EventTag
import com.taiwan.justvet.justpet.data.PetEvent
import com.taiwan.justvet.justpet.util.BarScore
import com.taiwan.justvet.justpet.util.Util.getString
import java.text.SimpleDateFormat
import java.util.*

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

    private val _showDatePickerDialog = MutableLiveData<Boolean>()
    val showDatePickerDialog: LiveData<Boolean>
        get() = _showDatePickerDialog

    private val _showTimePickerDialog = MutableLiveData<Boolean>()
    val showTimePickerDialog: LiveData<Boolean>
        get() = _showTimePickerDialog

    val eventNote = MutableLiveData<String>()

    var eventSpirit: Double? = 0.0
    var eventAppetite: Double? = 0.0

    val eventWeight = MutableLiveData<String>()
    val eventTemper = MutableLiveData<String>()
    val eventRR = MutableLiveData<String>()
    val eventHR = MutableLiveData<String>()
    val eventTimestamp = MutableLiveData<Long>()

    val calendar = Calendar.getInstance()

    val firebase = FirebaseFirestore.getInstance()
    val eventDatabase = petEvent.petId?.let { petId ->
        firebase.collection(PETS).document(petId).collection(EVENTS)
    }

    init {
        initialEvent()
        setEventTags()
        updateDateAndTime()
    }

    private fun initialEvent() {
        petEvent.let {
            eventNote.value = it.note
            eventSpirit = it.spirit
            eventAppetite = it.appetite
            eventWeight.value = it.weight
            eventTemper.value = it.temperature
            eventRR.value = it.respiratoryRate
            eventHR.value = it.heartRate
            eventTimestamp.value = it.timestamp
        }
    }

    private fun setEventTags() {
        _eventTags.value = petEvent.eventTags
    }

    fun updateDateAndTime() {
        _dateAndTime.value = SimpleDateFormat(
            getString(R.string.date_time_format),
            Locale.TAIWAN
        ).format(calendar.time)
    }

    fun setSpiritScore(score: Float) {
        for (item in BarScore.values()) {
            if (score.toDouble() == item.score) {
                eventSpirit = item.score
            }
        }
        Log.d(TAG, "eventSpirit : ${eventSpirit}")
    }

    fun setAppetiteScore(score: Float) {
        for (item in BarScore.values()) {
            if (score.toDouble() == item.score) {
                eventAppetite = item.score
            }
        }
        Log.d(TAG, "eventAppetite : ${eventAppetite}")
    }

    fun checkEventId() {
        if (petEvent.eventId == null) {
            postEvent()
        } else {
            updateEvent()
        }
    }

    fun postEvent() {
        // get selected time and date string list
        val timeList = SimpleDateFormat(
            getString(R.string.timelist_format),
            Locale.TAIWAN
        ).format(calendar.time).split("/")

        val finalEvent = petEvent.let {
            PetEvent(
                petProfile = it.petProfile,
                petId = it.petId,
                petName = it.petName,
                timestamp = eventTimestamp.value,
                year = timeList[0].toLong(),
                month = timeList[1].toLong(),
                dayOfMonth = timeList[2].toLong(),
                time = timeList[3],
                eventType = it.eventType,
                eventTags = it.eventTags,
                note = eventNote.value,
                spirit = eventSpirit,
                appetite = eventAppetite,
                weight = eventWeight.value,
                temperature = eventTemper.value,
                respiratoryRate = eventRR.value,
                heartRate = eventHR.value
            )
        }
        eventDatabase?.let {
            it.add(finalEvent)
                .addOnSuccessListener { documentReference ->
                    Log.d(TAG, "postEvent succeeded ID : ${documentReference.id}")
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
                        }
                        .addOnFailureListener {
                            Log.d(TAG, "postTags failed : $it")
                        }
                }
                navigateToCalendar()
            }
        }
    }

    fun updateEvent() {
        val finalEvent =
            mapOf(
                "note" to eventNote.value,
                "spirit" to eventSpirit,
                "appetite" to eventAppetite,
                "weight" to eventWeight.value,
                "temperature" to eventTemper.value,
                "respiratoryRate" to eventRR.value,
                "heartRate" to eventHR.value
            )

        petEvent.eventId?.let {
            eventDatabase?.document(it)?.update(finalEvent)?.addOnSuccessListener {
                Log.d(TAG, "update succeeded")
            }?.addOnFailureListener {
                Log.d(TAG, "update failed")
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

    fun showDatePickerDialog() {
        _showDatePickerDialog.value = true
    }

    fun showDateDialogCompleted() {
        _showDatePickerDialog.value = false
    }

    fun showTimePickerDialog() {
        _showTimePickerDialog.value = true
    }

    fun showTimeDialogCompleted() {
        _showTimePickerDialog.value = false
    }

}