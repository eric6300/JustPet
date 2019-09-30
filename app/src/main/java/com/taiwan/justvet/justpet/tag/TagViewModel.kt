package com.taiwan.justvet.justpet.tag

import android.graphics.drawable.Drawable
import android.icu.util.Calendar
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.taiwan.justvet.justpet.*
import com.taiwan.justvet.justpet.data.EventTag
import com.taiwan.justvet.justpet.data.PetEvent
import com.taiwan.justvet.justpet.data.PetProfile
import com.taiwan.justvet.justpet.data.UserProfile
import com.taiwan.justvet.justpet.util.LoadApiStatus
import com.taiwan.justvet.justpet.util.TagType
import com.taiwan.justvet.justpet.util.Util
import com.taiwan.justvet.justpet.util.Util.getDrawable
import com.taiwan.justvet.justpet.util.Util.getString
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class TagViewModel(val petEvent: PetEvent) : ViewModel() {

    private val _navigateToEditEvent = MutableLiveData<Boolean>()
    val navigateToEditEvent: LiveData<Boolean>
        get() = _navigateToEditEvent

    private val _navigateToCalendar = MutableLiveData<Boolean>()
    val navigateToCalendar: LiveData<Boolean>
        get() = _navigateToCalendar

    private val _leaveTagDialog = MutableLiveData<Boolean>()
    val leaveTagDialog: LiveData<Boolean>
        get() = _leaveTagDialog

    private val _listOfTags = MutableLiveData<List<EventTag>>()
    val listOfTags: LiveData<List<EventTag>>
        get() = _listOfTags

    private val _listOfProfile = MutableLiveData<List<PetProfile>>()
    val listOfProfile: LiveData<List<PetProfile>>
        get() = _listOfProfile

    private val _loadStatus = MutableLiveData<LoadApiStatus>()
    val loadStatus: LiveData<LoadApiStatus>
        get() = _loadStatus

    private val _currentEvent = MutableLiveData<PetEvent>()
    val currentEvent: LiveData<PetEvent>
        get() = _currentEvent

    val calendar = Calendar.getInstance()
    var selectedPetProfile: PetProfile? = null
    val petData = mutableListOf<PetProfile>()
    val eventTags = mutableListOf<EventTag>()
    val eventTagsIndex = mutableListOf<Long>()

    private val listTagDiary = mutableListOf<EventTag>()
    private val listTagSyndrome = mutableListOf<EventTag>()
    private val listTagTreatment = mutableListOf<EventTag>()

    init {
        UserManager.userProfile.value?.let {
            getPetProfileData(it)
        }

        setupDiaryTagList()
        setupSyndromeTagList()
        setupTreatmentTagList()

        showDiaryTag()

    }

    val database = FirebaseFirestore.getInstance()
    val pets = database.collection(PETS)

    fun getPetProfileData(userProfile: UserProfile) {
        userProfile.pets?.let {
            viewModelScope.launch {
                var index = 1
                for (petId in it) {
                    pets.document(petId).get()
                        .addOnSuccessListener { document ->
                            val petProfile = PetProfile(
                                profileId = document.id,
                                name = document["name"] as String?,
                                species = document["species"] as Long?,
                                gender = document["gender"] as Long?,
                                neutered = document["neutered"] as Boolean?,
                                birthday = document["birthday"] as Long?,
                                idNumber = document["idNumber"] as String?,
                                owner = document["owner"] as String?,
                                ownerEmail = document["ownerEmail"] as String?,
                                family = document["family"] as List<String>?,
                                image = document["image"] as String?
                            )
                            petData.add(petProfile)
                            if (index == it.size) {
                                petData.sortBy { it.profileId }
                                _listOfProfile.value = petData
                            }
                            index++
                            Log.d(ERIC, "TagViewModel getPetProfileData() succeeded")
                        }
                        .addOnFailureListener {
                            Log.d(ERIC, "TagViewModel getPetProfileData() failed : $it")
                        }
                }
            }
        }
    }

    fun getProfileByPosition(position: Int) {
        selectedPetProfile = petData[position]
    }


    fun showDiaryTag() {
        _listOfTags.value = listTagDiary
    }

    fun showSyndromeTag() {
        _listOfTags.value = listTagSyndrome
    }

    fun showTreatmentTag() {
        _listOfTags.value = listTagTreatment
    }

    private fun setupDiaryTagList() {
        listTagDiary.let {
            it.add(EventTag(TagType.DIARY.value, 0, getString(R.string.text_eating)))
            it.add(EventTag(TagType.DIARY.value, 1, getString(R.string.text_shower)))
            it.add(EventTag(TagType.DIARY.value, 2, getString(R.string.text_walking)))
            it.add(EventTag(TagType.DIARY.value, 3, getString(R.string.text_nail_trimming)))
            it.add(EventTag(TagType.DIARY.value, 4, getString(R.string.text_hair_trimming)))
            it.add(EventTag(TagType.DIARY.value, 5, getString(R.string.text_weight_measure)))
            it.add(EventTag(TagType.DIARY.value, 6, getString(R.string.text_other_daily)))
        }
    }

    private fun setupSyndromeTagList() {
        listTagSyndrome.let {
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
        listTagTreatment.let {
            it.add(EventTag(TagType.TREATMENT.value, 200, getString(R.string.text_ecto_prevention)))
            it.add(EventTag(TagType.TREATMENT.value, 201, getString(R.string.text_endo_prevention)))
            it.add(EventTag(TagType.TREATMENT.value, 202, getString(R.string.text_heart_worm_prevention)))
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

    private fun navigateToEditEvent() {

        selectedPetProfile?.let {
            _currentEvent.value = PetEvent(
                petProfile = it,
                petId = it.profileId,
                petName = it.name,
                petSpecies = it.species,
                eventTags = eventTags,
                eventTagsIndex = eventTagsIndex,
                respiratoryRate = petEvent.respiratoryRate,
                heartRate = petEvent.heartRate
            )
            _navigateToEditEvent.value = true
            _loadStatus.value = LoadApiStatus.DONE
        }
    }

    fun navigateToEditEventCompleted() {
        _navigateToEditEvent.value = false
    }

    fun navigateToCalendarCompleted() {
        _navigateToCalendar.value = false
    }

    fun leaveTagDialog() {
        _leaveTagDialog.value = true
    }

    fun getIconDrawable(index: Long): Drawable? {
        return when (index) {
            0L -> getDrawable(R.drawable.ic_food)
            1L -> getDrawable(R.drawable.ic_shower)
            2L -> getDrawable(R.drawable.ic_walking)
            3L -> getDrawable(R.drawable.ic_nail_trimming)
            4L -> getDrawable(R.drawable.ic_grooming)
            5L -> getDrawable(R.drawable.ic_weighting)
            200L -> getDrawable(R.drawable.ic_tick)
            201L -> getDrawable(R.drawable.ic_medicine)
            202L -> getDrawable(R.drawable.ic_heart)
            203L -> getDrawable(R.drawable.ic_synrige)
            204L -> getDrawable(R.drawable.ic_blood_test)
            205L -> getDrawable(R.drawable.ic_medicine)
            206L -> getDrawable(R.drawable.ic_ointment)
            207L -> getDrawable(R.drawable.ic_eye_drops)
            208L -> getDrawable(R.drawable.ic_synrige)
            209L -> getDrawable(R.drawable.ic_blood_test)
            else -> getDrawable(R.drawable.ic_others)
        }
    }

    fun makeTagList() {
        _loadStatus.value = LoadApiStatus.LOADING
        val arrayOfList = arrayListOf<List<EventTag>>()
        arrayOfList.add(listTagDiary)
        arrayOfList.add(listTagSyndrome)
        arrayOfList.add(listTagTreatment)
        viewModelScope.let {
            for (list in arrayOfList) {
                for (tag in list) {
                    tag.title?.let {
                        if (tag.isSelected == true) {
                            eventTags.add(tag)
                            tag.index?.let { index -> eventTagsIndex.add(index) }
                        }
                    }
                }
            }
            if (eventTags.size == 0) {
                _loadStatus.value = LoadApiStatus.ERROR
                Toast.makeText(JustPetApplication.appContext, "請至少選擇一個分類", Toast.LENGTH_LONG).show()
            } else {
                navigateToEditEvent()
            }
        }
    }


    fun quickSave() {
        _loadStatus.value = LoadApiStatus.LOADING
        val arrayOfList = arrayListOf<List<EventTag>>()
        arrayOfList.add(listTagDiary)
        arrayOfList.add(listTagSyndrome)
        arrayOfList.add(listTagTreatment)
        viewModelScope.let {
            for (list in arrayOfList) {
                for (tag in list) {
                    tag.title?.let {
                        if (tag.isSelected == true) {
                            eventTags.add(tag)
                            tag.index?.let { index -> eventTagsIndex.add(index) }
                        }
                    }
                }
            }
            if (eventTags.size == 0) {
                _loadStatus.value = LoadApiStatus.ERROR
                Toast.makeText(JustPetApplication.appContext, "請至少選擇一個分類", Toast.LENGTH_LONG).show()
            } else {
                postEvent()
            }
        }
    }

    private fun postEvent() {
        _loadStatus.value = LoadApiStatus.LOADING
        // get selected time and date string list
        val timeList = SimpleDateFormat(
            getString(R.string.timelist_format),
            Locale.TAIWAN
        ).format(calendar.time).split("/")

        selectedPetProfile?.let {
            it.profileId?.apply {
                pets.document(this).collection(EVENTS).add(
                    PetEvent(
                        petProfile = it,
                        petId = it.profileId,
                        petName = it.name,
                        petSpecies = it.species,
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
                ).addOnSuccessListener { documentReference ->
                    postTags(documentReference.id)
                    Log.d(ERIC, "TagViewModel quickSave() succeeded")
                }.addOnFailureListener { e ->
                    _loadStatus.value = LoadApiStatus.ERROR
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
                        pets.document(it).collection(EVENTS)
                            .document(eventId).collection(TAGS).add(item)
                            .addOnSuccessListener {
                                Log.d(ERIC, "TagViewModel postTags() succeeded")
                            }.addOnFailureListener { e ->
                                Log.d(ERIC, "TagViewModel postTags() failed: $e")
                            }
                        if (index == eventTags.size) {
                            _loadStatus.value = LoadApiStatus.DONE
                            _navigateToCalendar.value = true
                            Toast.makeText(JustPetApplication.appContext, "新增成功！", Toast.LENGTH_LONG).show()
                        }
                        index++
                    }
                }
            }
        }
    }
}