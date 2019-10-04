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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ServerTimestamp
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.taiwan.justvet.justpet.*
import com.taiwan.justvet.justpet.data.EventTag
import com.taiwan.justvet.justpet.data.PetEvent
import com.taiwan.justvet.justpet.family.EMPTY_STRING
import com.taiwan.justvet.justpet.pet.COLON
import com.taiwan.justvet.justpet.pet.SLASH
import com.taiwan.justvet.justpet.util.*
import com.taiwan.justvet.justpet.util.Util.getString
import java.text.SimpleDateFormat
import java.util.*

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

    val calendar = Calendar.getInstance()

    val eventsReference =
        FirebaseFirestore.getInstance().collection(PETS).document(petEvent.petId).collection(EVENTS)

    val storageReference = FirebaseStorage.getInstance().reference

    init {
        initialEvent()
    }

    private fun initialEvent() {
        petEvent.let {
            eventNote.value = it.note
            eventSpirit = it.spirit
            eventAppetite = it.appetite
            eventWeight.value = it.weight
            eventTemper.value = it.temperature
            eventRr.value = it.respiratoryRate
            eventHr.value = it.heartRate
            eventImage.value = it.imageUrl
            _eventTags.value = it.eventTags

            if (it.timestamp == 0L) {  // navigate from tag dialog for add an event
                eventTimestamp.value = (calendar.timeInMillis / 1000)
            } else {  // navigate from calendar fragment for edit the event
                eventTimestamp.value = it.timestamp
            }
        }

        _expandStatus.value = true

        initialDateAndTimeOfEvent()
    }

    fun initialDateAndTimeOfEvent() {
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

                    postEvent()

                }
            }
        } else {
            updateEvent()
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
                weight = eventWeight.value,
                temperature = eventTemper.value,
                respiratoryRate = eventRr.value,
                heartRate = eventHr.value
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
                "year" to timeList[0].toLong(),
                "month" to timeList[1].toLong(),
                "dayOfMonth" to timeList[2].toLong(),
                "time" to timeList[3],
                "timestamp" to eventTimestamp.value,
                "note" to eventNote.value,
                "spirit" to eventSpirit,
                "appetite" to eventAppetite,
                "weight" to eventWeight.value,
                "temperature" to eventTemper.value,
                "respiratoryRate" to eventRr.value,
                "heartRate" to eventHr.value,
                "imageUrl" to eventImage.value
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

}