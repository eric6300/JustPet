package com.taiwan.justvet.justpet.tag

import android.graphics.drawable.Drawable
import android.icu.util.Calendar
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.taiwan.justvet.justpet.R
import com.taiwan.justvet.justpet.data.EventTag
import com.taiwan.justvet.justpet.data.PetEvent
import com.taiwan.justvet.justpet.data.PetProfile
import com.taiwan.justvet.justpet.data.userProfile
import com.taiwan.justvet.justpet.home.TAG
import com.taiwan.justvet.justpet.util.TagType
import com.taiwan.justvet.justpet.util.Util.getDrawable
import com.taiwan.justvet.justpet.util.Util.getString
import com.taiwan.justvet.justpet.util.timestampToDateString
import com.taiwan.justvet.justpet.util.timestampToTimeString
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class TagViewModel : ViewModel() {

    private val _navigateToEditEvent = MutableLiveData<Boolean>()
    val navigateToEditEvent: LiveData<Boolean>
        get() = _navigateToEditEvent

    private val _leaveTagDialog = MutableLiveData<Boolean>()
    val leaveTagDialog: LiveData<Boolean>
        get() = _leaveTagDialog

    private val _showDatePickerDialog = MutableLiveData<Boolean>()
    val showDatePickerDialog: LiveData<Boolean>
        get() = _showDatePickerDialog

    private val _showTimePickerDialog = MutableLiveData<Boolean>()
    val showTimePickerDialog: LiveData<Boolean>
        get() = _showTimePickerDialog

    private val _listOfTags = MutableLiveData<List<EventTag>>()
    val listOfTags: LiveData<List<EventTag>>
        get() = _listOfTags

    private val _listOfProfile = MutableLiveData<List<PetProfile>>()
    val listOfProfile: LiveData<List<PetProfile>>
        get() = _listOfProfile

    private val _currentDate = MutableLiveData<String>()
    val currentDate: LiveData<String>
        get() = _currentDate

    private val _currentTime = MutableLiveData<String>()
    val currentTime: LiveData<String>
        get() = _currentTime

    private val _currentEvent = MutableLiveData<PetEvent>()
    val currentEvent: LiveData<PetEvent>
        get() = _currentEvent

    var timeList = mutableListOf<String>()

    var selectedPetProfile: PetProfile? = null
    val petData = mutableListOf<PetProfile>()
    val eventTags = mutableListOf<EventTag>()
    val tagTitleList = mutableListOf<String>()

    private val listTagDiary = mutableListOf<EventTag>()
    private val listTagSyndrome = mutableListOf<EventTag>()
    private val listTagTreatment = mutableListOf<EventTag>()


    init {
        getPetProfileData(mockUser())
        setupDiaryTagList()
        setupSyndromeTagList()
        setupTreatmentTagList()

        showDiaryTag()

        showCurrentTime()
    }

    val db = FirebaseFirestore.getInstance()
    val pets = db.collection("pets")

    fun mockUser(): userProfile {
        val petList = ArrayList<String>()
        petList.let {
            it.add("5DjrhdAlZka29LSmOe12")
            it.add("BR1unuBGFmeioH4VpKc2")
            it.add("FeHxkWD6VwpPMtL2bZT4")
        }
        return userProfile("eric6300", "6300eric@gmail.com", petList)
    }

    fun getPetProfileData(userProfile: userProfile) {
        userProfile.pets?.let {
            viewModelScope.launch {
                for (petId in userProfile.pets) {
                    pets.document(petId).get()
                        .addOnSuccessListener { document ->
                            val petProfile = PetProfile(
                                id = document.id,
                                name = document["name"] as String?,
                                species = document["species"] as Long?,
                                gender = document["gender"] as Long?,
                                neutered = document["neutered"] as Boolean?,
                                birthDay = document["birthDay"] as String?,
                                idNumber = document["idNumber"] as String?,
                                owner = document["owner"] as String?
                            )
                            petData.add(petProfile)
                            _listOfProfile.value = petData
                        }
                        .addOnFailureListener {
                            Log.d(TAG, "Failed")
                        }
                }
            }
        }
    }

    fun getProfilePosition(position: Int) {
        selectedPetProfile = petData[position]
    }

    private fun showCurrentTime() {
        val timeStamp = System.currentTimeMillis()
        timeStamp.let {
            _currentDate.value = it.timestampToDateString()
            _currentTime.value = it.timestampToTimeString()
        }
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
            it.add(EventTag(TagType.DIARY, 0, "吃飯"))
            it.add(EventTag(TagType.DIARY, 1, "洗澡"))
            it.add(EventTag(TagType.DIARY, 2, "散步"))
            it.add(EventTag(TagType.DIARY, 3, "剪指甲"))
            it.add(EventTag(TagType.DIARY, 4, "剃毛"))
            it.add(EventTag(TagType.DIARY, 5, "量體重"))
            it.add(EventTag(TagType.DIARY, 6, "其他"))
        }
    }

    private fun setupSyndromeTagList() {
        listTagSyndrome.let {
            it.add(EventTag(TagType.SYNDROME, 100, "嘔吐"))
            it.add(EventTag(TagType.SYNDROME, 101, "下痢"))
            it.add(EventTag(TagType.SYNDROME, 102, "咳嗽"))
            it.add(EventTag(TagType.SYNDROME, 103, "打噴嚏"))
            it.add(EventTag(TagType.SYNDROME, 104, "搔癢"))
            it.add(EventTag(TagType.SYNDROME, 105, "癲癇"))
            it.add(EventTag(TagType.SYNDROME, 106, "昏倒"))
            it.add(EventTag(TagType.SYNDROME, 107, "排尿異常"))
            it.add(EventTag(TagType.SYNDROME, 108, "其他"))
        }
    }

    private fun setupTreatmentTagList() {
        listTagTreatment.let {
            it.add(EventTag(TagType.TREATMENT, 200, "除蚤"))
            it.add(EventTag(TagType.TREATMENT, 201, "驅蟲"))
            it.add(EventTag(TagType.TREATMENT, 202, "心絲蟲"))
            it.add(EventTag(TagType.TREATMENT, 203, "皮下注射"))
            it.add(EventTag(TagType.TREATMENT, 204, "血糖紀錄"))
            it.add(EventTag(TagType.TREATMENT, 205, "口服藥"))
            it.add(EventTag(TagType.TREATMENT, 206, "外用藥"))
            it.add(EventTag(TagType.TREATMENT, 207, "眼藥/耳藥"))
            it.add(EventTag(TagType.TREATMENT, 208, "其他"))
        }
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

    fun getCurrentDateAndTime(calendar: Calendar) {
        timeList.addAll(
            SimpleDateFormat(
                getString(R.string.timelist_format),
                Locale.TAIWAN
            ).format(calendar.time).split("/")
        )
    }

    fun navigateToEditEvent() {
        _currentEvent.value = PetEvent(
            petId = selectedPetProfile?.id,
            timeStamp = System.currentTimeMillis(),
            year = timeList[0].toInt(),
            month = timeList[1].toInt(),
            dayOfMonth = timeList[2].toInt(),
            timeString = timeList[3],
            eventTags = eventTags,
            tagTitleList = tagTitleList
        )
        _navigateToEditEvent.value = true
    }

    fun navigateToEditEventCompleted() {
        _navigateToEditEvent.value = null
    }

    fun leaveTagDialog() {
        _leaveTagDialog.value = true
    }

    fun getIconDrawable(index: Int): Drawable? {
        return when (index) {
            0 -> getDrawable(R.drawable.ic_food)
            1 -> getDrawable(R.drawable.ic_shower)
            2 -> getDrawable(R.drawable.ic_walking)
            3 -> getDrawable(R.drawable.ic_nail_trimming)
            4 -> getDrawable(R.drawable.ic_grooming)
            5 -> getDrawable(R.drawable.ic_weighting)
            else -> getDrawable(R.drawable.ic_synrige)
        }
    }

    fun updateDate(calendar: Calendar) {
        _currentDate.value = SimpleDateFormat(
            getString(R.string.date_format),
            Locale.TAIWAN
        ).format(calendar.time)
    }

    fun updateTime(calendar: Calendar) {
        _currentTime.value = SimpleDateFormat(
            getString(R.string.time_format),
            Locale.TAIWAN
        ).format(calendar.time)
    }

    fun makeTagList() {
        val arrayOfList = arrayListOf<List<EventTag>>()
        arrayOfList.add(listTagDiary)
        arrayOfList.add(listTagSyndrome)
        arrayOfList.add(listTagTreatment)
        viewModelScope.let {
            for (list in arrayOfList) {
                for (tag in list) {
                    if (tag.isSelected == true) {
                        eventTags.add(tag)
                        tagTitleList.add(tag.title)
                        Log.d(TAG, "$tag")
                    }
                }
            }
            navigateToEditEvent()
        }
    }


    fun quickSave() {

    }
}