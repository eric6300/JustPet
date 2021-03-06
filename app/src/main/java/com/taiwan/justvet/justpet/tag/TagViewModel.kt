package com.taiwan.justvet.justpet.tag

import android.graphics.drawable.Drawable
import android.icu.util.Calendar
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taiwan.justvet.justpet.*
import com.taiwan.justvet.justpet.data.*
import com.taiwan.justvet.justpet.pet.PetSpecies
import com.taiwan.justvet.justpet.util.LoadStatus
import com.taiwan.justvet.justpet.util.Util
import com.taiwan.justvet.justpet.util.Util.getString
import com.taiwan.justvet.justpet.ext.toTimeListFormat
import com.taiwan.justvet.justpet.ext.toPetProfile
import kotlinx.coroutines.launch

class TagViewModel(val petEvent: PetEvent) : ViewModel() {

    private val _navigateToEventFragment = MutableLiveData<PetEvent>()
    val navigateToEventFragment: LiveData<PetEvent>
        get() = _navigateToEventFragment

    private val _navigateToCalendarFragment = MutableLiveData<Boolean>()
    val navigateToCalendarFragment: LiveData<Boolean>
        get() = _navigateToCalendarFragment

    private val _leaveDialog = MutableLiveData<Boolean>()
    val leaveDialog: LiveData<Boolean>
        get() = _leaveDialog

    private val _listOfTag = MutableLiveData<List<EventTag>>()
    val listOfTag: LiveData<List<EventTag>>
        get() = _listOfTag

    private val _petList = MutableLiveData<List<PetProfile>>()
    val petList: LiveData<List<PetProfile>>
        get() = _petList

    private val _loadStatus = MutableLiveData<LoadStatus>()
    val loadStatus: LiveData<LoadStatus>
        get() = _loadStatus

    var selectedPetProfile: PetProfile? = null
    val eventTags = mutableListOf<EventTag>()
    val eventTagsIndex = mutableListOf<Long>()

    private val diaryTags = mutableListOf<EventTag>()
    private val syndromeTags = mutableListOf<EventTag>()
    private val treatmentTags = mutableListOf<EventTag>()

    private val petsReference = JustPetRepository.firestoreInstance.collection(PETS)

    init {
        UserManager.userProfile.value?.let {
            getPetProfileData(it)
        }

        setupDiaryTagList()
        setupSyndromeTagList()
        setupTreatmentTagList()

        showEventTags(0)

    }

    private fun getPetProfileData(userProfile: UserProfile) {

        if (userProfile.pets?.size != 0) {

            _loadStatus.value = LoadStatus.LOADING

            val petListFromFirebase = mutableListOf<PetProfile>()

            userProfile.pets?.let { pets ->


                fun getNextPetProfile(index: Int) {

                    if (index == pets.size) { // already get all pet data from firebase
                        _petList.value = petListFromFirebase.sortedBy { it.profileId }
                        _loadStatus.value = LoadStatus.DONE
                        return
                    }

                    petsReference.document(pets[index]).get()
                        .addOnSuccessListener { document ->

                            petListFromFirebase.add(document.toPetProfile())

                            getNextPetProfile(index.plus(1))

                        }
                        .addOnFailureListener {

                            getNextPetProfile(index.plus(1))

                        }
                }

                getNextPetProfile(0) //  get first pet profile

            }
        }
    }

    fun getPetProfileByPosition(position: Int) {
        _petList.value?.let {
            selectedPetProfile = it[position]
        }
    }

    private fun setupDiaryTagList() {
        diaryTags.let {
            it.add(EventTag(TagType.DIARY.value, 0, getString(R.string.text_eating)))
            it.add(EventTag(TagType.DIARY.value, 1, getString(R.string.text_shower)))
            it.add(EventTag(TagType.DIARY.value, 2, getString(R.string.text_walking)))
            it.add(EventTag(TagType.DIARY.value, 3, getString(R.string.text_nail_trimming)))
            it.add(EventTag(TagType.DIARY.value, 4, getString(R.string.text_hair_trimming)))
            it.add(EventTag(TagType.DIARY.value, 5, getString(R.string.text_weight_measure)))
            it.add(EventTag(TagType.DIARY.value, 6, getString(R.string.text_other_diary)))
        }
    }

    private fun setupSyndromeTagList() {
        syndromeTags.let {
            it.add(EventTag(TagType.SYNDROME.value, 100, getString(R.string.text_vomit)))
            it.add(EventTag(TagType.SYNDROME.value, 101, getString(R.string.text_diarrhea)))
            it.add(EventTag(TagType.SYNDROME.value, 102, getString(R.string.text_coughing)))
            it.add(EventTag(TagType.SYNDROME.value, 103, getString(R.string.text_sneezing)))
            it.add(EventTag(TagType.SYNDROME.value, 104, getString(R.string.text_itchy)))
            it.add(EventTag(TagType.SYNDROME.value, 105, getString(R.string.text_seizure)))
            it.add(EventTag(TagType.SYNDROME.value, 106, getString(R.string.text_collapse)))
            it.add(EventTag(TagType.SYNDROME.value, 107, getString(R.string.text_urine_abnormal)))
            it.add(EventTag(TagType.SYNDROME.value, 108, getString(R.string.text_other_syndrome)))
        }
    }

    private fun setupTreatmentTagList() {
        treatmentTags.let {
            it.add(EventTag(TagType.TREATMENT.value, 200, getString(R.string.text_ecto_prevention)))
            it.add(EventTag(TagType.TREATMENT.value, 201, getString(R.string.text_endo_prevention)))
            it.add(
                EventTag(
                    TagType.TREATMENT.value,
                    202,
                    getString(R.string.text_heart_worm_prevention)
                )
            )
            it.add(EventTag(TagType.TREATMENT.value, 203, getString(R.string.text_sc_injection)))
            it.add(EventTag(TagType.TREATMENT.value, 204, getString(R.string.text_blood_glucose)))
            it.add(EventTag(TagType.TREATMENT.value, 205, getString(R.string.text_oral_med)))
            it.add(EventTag(TagType.TREATMENT.value, 206, getString(R.string.text_external_med)))
            it.add(EventTag(TagType.TREATMENT.value, 207, getString(R.string.text_drop)))
            it.add(EventTag(TagType.TREATMENT.value, 208, getString(R.string.text_vaccine)))
            it.add(EventTag(TagType.TREATMENT.value, 209, getString(R.string.text_health_exam)))
            it.add(EventTag(TagType.TREATMENT.value, 210, getString(R.string.text_other_treatment)))
        }
    }

    fun showEventTags(index: Int) {
        _listOfTag.value = when (index) {
            TagType.DIARY.index -> {
                diaryTags
            }
            TagType.SYNDROME.index -> {
                syndromeTags
            }
            TagType.TREATMENT.index -> {
                treatmentTags
            }
            else -> {
                null
            }
        }
    }

    fun getIconDrawable(index: Long): Drawable? {
        return Util.getIconDrawable(index)
    }

    fun getSelectedTagsForEvent(eventSaveType: EventSaveType) {
        val arrayOfList = arrayListOf<List<EventTag>>()

        arrayOfList.let {
            it.add(diaryTags)
            it.add(syndromeTags)
            it.add(treatmentTags)
        }

        for (list in arrayOfList) {
            for (tag in list) {
                if (tag.isSelected) {
                    eventTags.add(tag)
                    tag.index?.let { index ->
                        eventTagsIndex.add(index)
                    }
                }
            }
        }

        saveEventBy(eventSaveType)
    }

    fun saveEventBy(eventSaveType: EventSaveType) {
        _loadStatus.value = LoadStatus.LOADING

        when (eventTags.size) {
            0 -> {
                Toast.makeText(
                    JustPetApplication.appContext,
                    getString(R.string.text_tag_empty_error),
                    Toast.LENGTH_LONG
                ).show()
                _loadStatus.value = LoadStatus.ERROR
            }
            else -> {
                when (eventSaveType) {
                    EventSaveType.DETAIL -> navigateToEventFragment()
                    EventSaveType.QUICK -> postEventToFirebase()
                }
            }
        }
    }

    private fun navigateToEventFragment() {
        selectedPetProfile?.let {
            _navigateToEventFragment.value = PetEvent(
                petProfile = it,
                petId = it.profileId ?: EMPTY_STRING,
                petName = it.name ?: EMPTY_STRING,
                petSpecies = it.species ?: PetSpecies.CAT.value,
                eventTags = eventTags,
                eventTagsIndex = eventTagsIndex,
                respiratoryRate = petEvent.respiratoryRate,
                heartRate = petEvent.heartRate
            )
            _loadStatus.value = LoadStatus.DONE
        }
    }

    fun navigateToEventFragmentCompleted() {
        _navigateToEventFragment.value = null
    }

    private fun postEventToFirebase() {
        _loadStatus.value = LoadStatus.LOADING

        val calendar = Calendar.getInstance()

        val timeList = calendar.time.toTimeListFormat().split(SLASH)

        selectedPetProfile?.let {
            it.profileId?.apply {
                petsReference.document(this).collection(EVENTS).add(
                    PetEvent(
                        petProfile = it,
                        petId = this,
                        petName = it.name ?: EMPTY_STRING,
                        petSpecies = it.species ?: PetSpecies.CAT.value,
                        timestamp = (calendar.timeInMillis / 1000),
                        year = timeList[0].toLong(),
                        month = timeList[1].toLong(),
                        dayOfMonth = timeList[2].toLong(),
                        time = timeList[3],
                        eventTags = eventTags,
                        eventTagsIndex = eventTagsIndex,
                        respiratoryRate = petEvent.respiratoryRate,
                        heartRate = petEvent.heartRate
                    )
                ).addOnSuccessListener { document ->
                    postTags(document.id)
                    Log.d(ERIC, "TagViewModel quickSave() succeeded")
                }.addOnFailureListener { e ->
                    _loadStatus.value = LoadStatus.ERROR
                    Log.d(ERIC, "TagViewModel quickSave() failed: $e")
                }
            }
        }
    }

    private fun postTags(eventId: String) {
        selectedPetProfile?.apply {
            this.profileId?.let {
                viewModelScope.launch {
                    var index = 1
                    for (item in eventTags) {
                        petsReference.document(it).collection(EVENTS)
                            .document(eventId).collection(TAGS).add(item)
                            .addOnSuccessListener {
                                when (index) {
                                    eventTags.size -> {
                                        Toast.makeText(
                                            JustPetApplication.appContext,
                                            getString(R.string.text_event_save_success),
                                            Toast.LENGTH_LONG
                                        ).show()
                                        navigateToCalendar()
                                        _loadStatus.value = LoadStatus.DONE
                                        Log.d(ERIC, "TagViewModel postTags() succeeded")
                                    }
                                    else -> {
                                        index++
                                    }
                                }
                                Log.d(ERIC, "TagViewModel postTags() succeeded")
                            }.addOnFailureListener { e ->
                                when (index) {
                                    eventTags.size -> {
                                        Toast.makeText(
                                            JustPetApplication.appContext,
                                            getString(R.string.text_event_save_success),
                                            Toast.LENGTH_LONG
                                        ).show()
                                        navigateToCalendar()
                                        _loadStatus.value = LoadStatus.DONE
                                    }
                                    else -> {
                                        index++
                                    }
                                }
                                Log.d(ERIC, "TagViewModel postTags() failed: $e")
                            }
                    }
                }
            }
        }
    }

    private fun navigateToCalendar() {
        _navigateToCalendarFragment.value = true
    }

    fun navigateToCalendarCompleted() {
        _navigateToCalendarFragment.value = false
    }

    fun leaveDialog() {
        _leaveDialog.value = true
    }

    fun leaveDialogCompleted() {
        _leaveDialog.value = false
    }
}