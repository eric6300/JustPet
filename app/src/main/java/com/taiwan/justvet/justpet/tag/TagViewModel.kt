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
import com.taiwan.justvet.justpet.util.TagType
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
                            petData.sortBy { it.profileId }
                            _listOfProfile.value = petData
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
            it.add(EventTag(TagType.DIARY.value, 0, "吃飯"))
            it.add(EventTag(TagType.DIARY.value, 1, "洗澡"))
            it.add(EventTag(TagType.DIARY.value, 2, "散步"))
            it.add(EventTag(TagType.DIARY.value, 3, "剪指甲"))
            it.add(EventTag(TagType.DIARY.value, 4, "剃毛"))
            it.add(EventTag(TagType.DIARY.value, 5, "量體重"))
            it.add(EventTag(TagType.DIARY.value, 6, "其他日常"))
        }
    }

    private fun setupSyndromeTagList() {
        listTagSyndrome.let {
            it.add(EventTag(TagType.SYNDROME.value, 100, "嘔吐"))
            it.add(EventTag(TagType.SYNDROME.value, 101, "下痢"))
            it.add(EventTag(TagType.SYNDROME.value, 102, "咳嗽"))
            it.add(EventTag(TagType.SYNDROME.value, 103, "打噴嚏"))
            it.add(EventTag(TagType.SYNDROME.value, 104, "搔癢"))
            it.add(EventTag(TagType.SYNDROME.value, 105, "癲癇"))
            it.add(EventTag(TagType.SYNDROME.value, 106, "昏倒"))
            it.add(EventTag(TagType.SYNDROME.value, 107, "排尿異常"))
            it.add(EventTag(TagType.SYNDROME.value, 108, "其他症狀"))
        }
    }

    private fun setupTreatmentTagList() {
        listTagTreatment.let {
            it.add(EventTag(TagType.TREATMENT.value, 200, "除蚤"))
            it.add(EventTag(TagType.TREATMENT.value, 201, "驅蟲"))
            it.add(EventTag(TagType.TREATMENT.value, 202, "心絲蟲藥"))
            it.add(EventTag(TagType.TREATMENT.value, 203, "皮下注射"))
            it.add(EventTag(TagType.TREATMENT.value, 204, "血糖紀錄"))
            it.add(EventTag(TagType.TREATMENT.value, 205, "口服藥"))
            it.add(EventTag(TagType.TREATMENT.value, 206, "外用藥"))
            it.add(EventTag(TagType.TREATMENT.value, 207, "滴劑"))
            it.add(EventTag(TagType.TREATMENT.value, 208, "疫苗注射"))
            it.add(EventTag(TagType.TREATMENT.value, 209, "健康檢查"))
            it.add(EventTag(TagType.TREATMENT.value, 210, "其他醫療"))
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