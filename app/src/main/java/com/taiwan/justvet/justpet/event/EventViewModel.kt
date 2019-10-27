package com.taiwan.justvet.justpet.event

import android.icu.util.Calendar
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.UploadTask
import com.taiwan.justvet.justpet.*
import com.taiwan.justvet.justpet.data.EventTag
import com.taiwan.justvet.justpet.data.JustPetRepository
import com.taiwan.justvet.justpet.data.PetEvent
import com.taiwan.justvet.justpet.util.LoadStatus
import com.taiwan.justvet.justpet.util.Util.getString
import com.taiwan.justvet.justpet.ext.toEventDateAndTimeFormat
import com.taiwan.justvet.justpet.ext.toTimeListFormat

class EventViewModel(val petEvent: PetEvent) : ViewModel() {

    private val _navigateToCalendarFragment = MutableLiveData<Boolean>()
    val navigateToCalendarFragment: LiveData<Boolean>
        get() = _navigateToCalendarFragment

    private val _expandStatus = MutableLiveData<Boolean>()
    val expandStatus: LiveData<Boolean>
        get() = _expandStatus

    private val _loadStatus = MutableLiveData<LoadStatus>()
    val loadStatus: LiveData<LoadStatus>
        get() = _loadStatus

    private val _eventTags = MutableLiveData<List<EventTag>>()
    val eventTags: LiveData<List<EventTag>>
        get() = _eventTags

    private val _dateAndTimeOfEvent = MutableLiveData<String>()
    val dateAndTimeOfEvent: LiveData<String>
        get() = _dateAndTimeOfEvent

    private val _showDatePickerDialog = MutableLiveData<Boolean>()
    val showDatePickerDialog: LiveData<Boolean>
        get() = _showDatePickerDialog

    private val _showTimePickerDialog = MutableLiveData<Boolean>()
    val showTimePickerDialog: LiveData<Boolean>
        get() = _showTimePickerDialog

    private val _showGallery = MutableLiveData<Boolean>()
    val showGallery: LiveData<Boolean>
        get() = _showGallery

    var eventSpirit: Double? = 0.0
    var eventAppetite: Double? = 0.0
    val eventNote = MutableLiveData<String>()
    val eventWeight = MutableLiveData<String>()
    val eventTemper = MutableLiveData<String>()
    val eventRr = MutableLiveData<String>()
    val eventHr = MutableLiveData<String>()
    val eventTimestamp = MutableLiveData<Long>()
    val eventImage = MutableLiveData<String>()

    val calendar: Calendar = Calendar.getInstance()

    private val eventsReference =
        JustPetRepository.firestoreInstance.collection(PETS).document(petEvent.petId).collection(EVENTS)

    private val storageReference = JustPetRepository.storageInstance.reference

    init {
        initialEvent()
    }

    private fun initialEvent() {
        petEvent.let {
            eventNote.value = it.note
            eventSpirit = it.spirit
            eventAppetite = it.appetite
            eventImage.value = it.imageUrl
            _eventTags.value = it.eventTags
            eventWeight.value = when (it.weight) {
                null -> null
                else -> it.weight.toString()
            }
            eventTemper.value = when (it.temperature) {
                null -> null
                else -> it.temperature.toString()
            }
            eventRr.value = when (it.respiratoryRate) {
                null -> null
                else -> it.respiratoryRate.toString()
            }
            eventHr.value = when (it.heartRate) {
                null -> null
                else -> it.heartRate.toString()
            }
            eventTimestamp.value = when (it.timestamp) {
                0L -> (calendar.timeInMillis / 1000)
                else -> it.timestamp
            }
        }

        _expandStatus.value = true

        initialDateAndTimeOfEvent()
    }

    private fun initialDateAndTimeOfEvent() {
        _dateAndTimeOfEvent.value = when (petEvent.timestamp) {
            0L -> (calendar.timeInMillis / 1000).toEventDateAndTimeFormat()
            else -> petEvent.timestamp.toEventDateAndTimeFormat()
        }

        val timeList = getTimeListFromTimestamp(petEvent.timestamp)

        calendar.set(
            timeList[0].toInt(),
            timeList[1].toInt().minus(1),
            timeList[2].toInt(),
            timeList[3].split(COLON)[0].toInt(),
            timeList[3].split(COLON)[1].toInt()
        )
    }

    fun updateDateAndTimeOfEvent() {
        _dateAndTimeOfEvent.value = calendar.time.toEventDateAndTimeFormat()
        eventTimestamp.value = (calendar.timeInMillis / 1000)
    }

    fun setSpiritScore(score: Float) {
        eventSpirit = score.toDouble()
    }

    fun setAppetiteScore(score: Float) {
        eventAppetite = score.toDouble()
    }

    fun checkEventId() {
        if (petEvent.eventId == EMPTY_STRING) {

            petEvent.eventTagsIndex?.let {
                if (it.contains(5) && eventWeight.value.isNullOrEmpty()) {

                    Toast.makeText(
                        JustPetApplication.appContext,
                        getString(R.string.text_weight_empty_error),
                        Toast.LENGTH_LONG
                    ).show()

                } else {
                    if (isAllFormatValid()) {
                        postEvent()
                    } else {
                        Log.d(ERIC, "格式不對")
                    }
                }
            }
        } else {
            if (isAllFormatValid()) {
                updateEvent()
            } else {
                Log.d(ERIC, "格式不對")
            }
        }
    }

    private fun postEvent() {
        _loadStatus.value = LoadStatus.LOADING

        val timeList = getTimeListFromTimestamp(petEvent.timestamp)

        val finalEvent = petEvent.let {
            PetEvent(
                petProfile = it.petProfile,
                petId = it.petId,
                petName = it.petName,
                petSpecies = it.petSpecies,
                timestamp = (calendar.timeInMillis / 1000),
                year = timeList[0].toLong(),
                month = timeList[1].toLong(),
                dayOfMonth = timeList[2].toLong(),
                time = timeList[3],
                eventType = it.eventType,
                eventTags = it.eventTags,
                eventTagsIndex = it.eventTagsIndex,
                note = eventNote.value,
                spirit = eventSpirit,
                appetite = eventAppetite,
                weight = eventWeight.value?.toDouble(),
                temperature = eventTemper.value?.toDouble(),
                respiratoryRate = eventRr.value?.toLong(),
                heartRate = eventHr.value?.toLong()
            )
        }
        eventsReference.let {
            it.add(finalEvent)
                .addOnSuccessListener { document ->
                    postTags(document.id)
                    Log.d(ERIC, "postEvent succeeded ID : ${document.id}")
                }
                .addOnFailureListener { e ->
                    _loadStatus.value = LoadStatus.ERROR
                    Log.d(ERIC, "postEvent failed : $e")
                }
        }
    }

    private fun postTags(eventId: String) {
        petEvent.eventTags?.apply {
            eventsReference.let {
                for (tag in this) {
                    it.document(eventId).collection(TAGS).add(tag)
                        .addOnSuccessListener {
                            Log.d(ERIC, "postTags succeeded ID : ${it.id}")

                            if (eventImage.value == null) {
                                _loadStatus.value = LoadStatus.DONE

                                Toast.makeText(
                                    JustPetApplication.appContext,
                                    "新增成功",
                                    Toast.LENGTH_SHORT
                                ).show()

                                navigateToCalendar()
                            } else {
                                uploadImage(eventId)
                            }
                        }
                        .addOnFailureListener {
                            _loadStatus.value = LoadStatus.ERROR
                            Log.d(ERIC, "postTags failed : $it")
                        }
                }
            }
        }
    }

    private fun uploadImage(eventId: String) {
        eventImage.value?.let {
            val imageRef = storageReference.child(getString(R.string.text_image_path, eventId))
            imageRef.putFile(it.toUri())
                .continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let {
                            throw it
                        }
                    }
                    return@Continuation imageRef.downloadUrl
                }).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val downloadUri = task.result
                        Log.d(ERIC, "downloadUri : $downloadUri")
                        updateEventImageUrl(eventId, downloadUri)
                    }
                }.addOnFailureListener {
                    _loadStatus.value = LoadStatus.ERROR
                    Log.d(ERIC, "uploadImage failed : $it")
                }
        }
    }

    private fun updateEventImageUrl(eventId: String, downloadUri: Uri?) {
        eventsReference.let {
            it.document(eventId).update("imageUrl", downloadUri.toString())
                .addOnSuccessListener {
                    _loadStatus.value = LoadStatus.DONE
                    navigateToCalendar()
                    Log.d(ERIC, "updateEventImageUrl succeed")
                }.addOnFailureListener {
                    _loadStatus.value = LoadStatus.ERROR
                    Log.d(ERIC, "updateEventImageUrl failed : $it")
                }
        }
    }

    private fun updateEvent() {
        _loadStatus.value = LoadStatus.LOADING

        val timeList = getTimeListFromTimestamp(petEvent.timestamp)

        val finalEvent =
            mapOf(
                YEAR to timeList[0].toLong(),
                MONTH to timeList[1].toLong(),
                DAY_OF_MONTH to timeList[2].toLong(),
                TIME to timeList[3],
                TIMESTAMP to eventTimestamp.value,
                NOTE to eventNote.value,
                SPIRIT to eventSpirit,
                APPETITE to eventAppetite,
                WEIGHT to eventWeight.value?.toDouble(),
                TEMPERATURE to eventTemper.value?.toDouble(),
                RESPIRATORY_RATE to eventRr.value?.toLong(),
                HEART_RATE to eventHr.value?.toLong(),
                IMAGE_URL to eventImage.value
            )

        petEvent.eventId.let {
            eventsReference.document(it).update(finalEvent)
                .addOnSuccessListener {
                    _loadStatus.value = LoadStatus.DONE
                    navigateToCalendar()
                    Log.d(ERIC, "update succeeded")
                }.addOnFailureListener {
                    _loadStatus.value = LoadStatus.ERROR
                    Log.d(ERIC, "update failed")
                }
        }
    }

    fun getTimeListFromTimestamp(timestamp: Long): List<String> {
        return when (timestamp) {
            0L -> (calendar.timeInMillis / 1000).toTimeListFormat().split(SLASH)
            else -> petEvent.timestamp.toTimeListFormat().split(SLASH)
        }
    }

    fun isValidFormat(mutableLiveData: MutableLiveData<String>, type: Int): Boolean {
        return when {
            mutableLiveData.value == null -> true

            "[0-9]{0,2}([.][0-9]{0,2})?".toRegex().matches(mutableLiveData.value.toString()) -> true

            else -> {
                when (type) {

                    Companion.WEIGHT_TYPE ->
                        Toast.makeText(
                            JustPetApplication.appContext,
                            getString(R.string.text_weight_format_error),
                            Toast.LENGTH_LONG
                        ).show()

                    Companion.TEMPERATURE_TYPE ->
                        Toast.makeText(
                            JustPetApplication.appContext,
                            getString(R.string.text_temperature_format_error),
                            Toast.LENGTH_LONG
                        ).show()
                }

                false
            }
        }
    }

    fun isValidRateFormat(mutableLiveData: MutableLiveData<String>, rateType: Int): Boolean {
        return when {
            mutableLiveData.value == null -> true

            "[0-9]{0,3}?".toRegex().matches(mutableLiveData.value.toString()) -> true

            else -> {

                when (rateType) {

                    Companion.RESPIRATORY_RATE_TYPE ->
                        Toast.makeText(
                            JustPetApplication.appContext,
                            getString(R.string.text_respiratory_rate_format_error),
                            Toast.LENGTH_LONG
                        ).show()

                    Companion.HEART_RATE_TYPE -> {
                        Toast.makeText(
                            JustPetApplication.appContext,
                            getString(R.string.text_heart_rate_format_error),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                false
            }
        }
    }

    fun isAllFormatValid(): Boolean {
        return isValidFormat(eventWeight, Companion.WEIGHT_TYPE) &&
                isValidFormat(eventTemper, Companion.TEMPERATURE_TYPE) &&
                isValidRateFormat(eventRr, Companion.RESPIRATORY_RATE_TYPE) &&
                isValidRateFormat(eventHr, Companion.HEART_RATE_TYPE)
    }

    fun navigateToCalendar() {
        _navigateToCalendarFragment.value = true
    }

    fun navigateToCalendarCompleted() {
        _navigateToCalendarFragment.value = null
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

    fun showGallery() {
        _showGallery.value = true
    }

    fun showGalleryCompleted() {
        _showGallery.value = false
    }

    fun defaultWeight() {
        eventWeight.value = null
    }

    fun defaultTemper() {
        eventTemper.value = null
    }

    fun defaultRr() {
        eventRr.value = null
    }

    fun defaultHr() {
        eventHr.value = null
    }

    companion object {
        const val RESPIRATORY_RATE_TYPE = 0
        const val HEART_RATE_TYPE = 1
        const val WEIGHT_TYPE = 2
        const val TEMPERATURE_TYPE = 3

        //  PetEvent
        const val PET_ID = "petId"
        const val PET_NAME = "petName"
        const val PET_SPECIES = "petSpecies"
        const val TIMESTAMP = "timestamp"
        const val YEAR = "year"
        const val MONTH = "month"
        const val DAY_OF_MONTH = "dayOfMonth"
        const val TIME = "time"
        const val EVENT_TAGS_INDEX = "eventTagsIndex"
        const val NOTE = "note"
        const val SPIRIT = "spirit"
        const val APPETITE = "appetite"
        const val WEIGHT = "weight"
        const val TEMPERATURE = "temperature"
        const val RESPIRATORY_RATE = "respiratoryRate"
        const val HEART_RATE = "heartRate"
        const val IMAGE_URL = "imageUrl"

        //  EventTag
        const val TYPE = "type"
        const val INDEX = "index"
        const val TITLE = "title"
    }

}