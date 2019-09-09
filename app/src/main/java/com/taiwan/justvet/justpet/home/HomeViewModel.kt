package com.taiwan.justvet.justpet.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.taiwan.justvet.justpet.data.PetProfile
import android.app.DatePickerDialog
import android.icu.util.Calendar
import android.util.Log
import android.view.View
import com.google.firebase.firestore.FirebaseFirestore
import com.taiwan.justvet.justpet.*
import com.taiwan.justvet.justpet.UserManager.userProfile
import com.taiwan.justvet.justpet.data.EventNotification
import com.taiwan.justvet.justpet.data.UserProfile


class HomeViewModel : ViewModel() {

    private val _petList = MutableLiveData<List<PetProfile>>()
    val petList: LiveData<List<PetProfile>>
        get() = _petList

    private val _notificationList = MutableLiveData<List<EventNotification>>()
    val notificationList: LiveData<List<EventNotification>>
        get() = _notificationList

    private val _isModified = MutableLiveData<Boolean>()
    val isModified: LiveData<Boolean>
        get() = _isModified

    private val _birthdayChange = MutableLiveData<Boolean>()
    val birthdayChange: LiveData<Boolean>
        get() = _birthdayChange

    private val _navigateToAchievement = MutableLiveData<Boolean>()
    val navigateToAchievement: LiveData<Boolean>
        get() = _navigateToAchievement

    val petName = MutableLiveData<String>()
    val petBirthDay = MutableLiveData<String>()
    val petIdChip = MutableLiveData<String>()

    val firebase = FirebaseFirestore.getInstance()
    val pets = firebase.collection(PETS)

    init {
        userProfile.value?.let { userProfile ->
            userProfile.pets?.apply {
                if (this.isNotEmpty()) {
                    getPetProfileData(userProfile)
                }
            }
        }
    }

    fun getPetProfileData(userProfile: UserProfile) {
        userProfile.pets?.let {
            if (it.isNotEmpty()) {
                pets.whereEqualTo("owner", userProfile.profileId).get()
                    .addOnSuccessListener { list ->
                        val petData = mutableListOf<PetProfile>()
                        for (item in list) {
                            petData.add(
                                PetProfile(
                                    profileId = item.id,
                                    name = item["name"] as String?,
                                    species = item["species"] as Long?,
                                    gender = item["gender"] as Long?,
                                    neutered = item["neutered"] as Boolean?,
                                    birthDay = item["birthDay"] as String?,
                                    idNumber = item["idNumber"] as String?,
                                    owner = item["owner"] as String?
                                )
                            )
                        }
                        _petList.value = petData
                    }
                    .addOnFailureListener {
                        Log.d(TAG, "Failed")
                    }

            }
        }
    }

    fun getPetEventData(index: Int) {
        when (index) {
            0 -> dataOne()
            1 -> dataTwo()
            2 -> dataThree()
        }
    }

    fun datePicker(view: View) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        DatePickerDialog(
            view.context,
            DatePickerDialog.OnDateSetListener { view, year, month, day ->
                val dateTime = "$year 年 $month 月 $day 日"
                petBirthDay.value = dateTime
                _birthdayChange.value = true
            }, year, month, day
        ).show()
    }

    fun modifyPetProfile(petProfile: PetProfile) {
        petName.value = petProfile.name
        petIdChip.value = petProfile.idNumber
        _isModified.value = true
        Log.d(TAG, "modified pet profile")
    }

    fun modifyCompleted() {
        // update to firebase in the future
        Log.d(
            TAG,
            "Pet Name : ${petName.value} Pet BirthDay : ${petBirthDay.value} Pet IdChip : ${petIdChip.value}"
        )
        _isModified.value = false
    }

    fun modifyCancelled() {
        _isModified.value = false
    }

    fun birthdayChangeCompleted() {
        _birthdayChange.value = false
    }

    fun navigateToAchievement() {
        _navigateToAchievement.value = true
    }

    fun navigateToAchievementCompleted() {
        _navigateToAchievement.value = null
    }

    fun dataOne() {
        val eventList = mutableListOf<EventNotification>()
        eventList.add(EventNotification(type = 0, title = "年度健康檢查還剩 15 天", timeStamp = null))
        eventList.add(EventNotification(type = 1, title = "除蚤滴劑要記得點喔!", timeStamp = null))
        eventList.add(EventNotification(type = 2, title = "這四週內已經吐了三次喔!", timeStamp = null))
        _notificationList.value = eventList
    }

    fun dataTwo() {
        val eventList = mutableListOf<EventNotification>()
        eventList.add(EventNotification(type = 1, title = "要記得吃心絲蟲預防藥喔!", timeStamp = null))
        _notificationList.value = eventList
    }

    fun dataThree() {
        val eventList = mutableListOf<EventNotification>()
        eventList.add(EventNotification(type = 1, title = "今天要記得回診、拿藥喔!", timeStamp = null))
        _notificationList.value = eventList
    }

}