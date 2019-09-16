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
import com.taiwan.justvet.justpet.util.timestampToDateString


class HomeViewModel : ViewModel() {

    private val _petList = MutableLiveData<List<PetProfile>>()
    val petList: LiveData<List<PetProfile>>
        get() = _petList

    private val _selectedPet = MutableLiveData<PetProfile>()
    val selectedPet: LiveData<PetProfile>
        get() = _selectedPet

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

    private val _refreshPetProfile = MutableLiveData<Boolean>()
    val refreshPetProfile: LiveData<Boolean>
        get() = _refreshPetProfile

    val petName = MutableLiveData<String>()
    val petBirthday = MutableLiveData<String>()
    val petIdNumber = MutableLiveData<String>()

    val calendar = Calendar.getInstance()

    var year: Int = 0
    var month: Int = 0
    var dayOfMonth: Int = 0

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
                                    birthDay = item["birthDay"] as Long?,
                                    idNumber = item["idNumber"] as String?,
                                    owner = item["owner"] as String?
                                )
                            )
                        }
                        petData.sortBy { it.profileId }
                        _petList.value = petData
                        Log.d(ERIC, "getPetProfileData() succeeded")
                    }
                    .addOnFailureListener {
                        Log.d(ERIC, "getPetProfileData() failed: $it")
                    }

            }
        }
    }

    fun getPetEventData(index: Int) {
        _selectedPet.value = _petList.value?.let {
            Log.d(ERIC, "${it[index].profileId}")
            it[index]
        }

        when (index) {
            0 -> dataOne()
            1 -> dataTwo()
            2 -> dataThree()
        }
    }

    fun showPetProfile(petProfile: PetProfile) {
        petName.value = petProfile.name
        petIdNumber.value = petProfile.idNumber
        petBirthday.value = petProfile.birthDay?.timestampToDateString()
        Log.d(ERIC, "name:${petName.value}  id:${petIdNumber.value}  birthDay:${petBirthday.value}")
    }

    fun datePicker(view: View) {
        if (petBirthday.value == null) {
            year = calendar.get(Calendar.YEAR)
            month = calendar.get(Calendar.MONTH)
            dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        }  else {
            petBirthday.value?.let {
                val timeList = it.split("/")
                year = timeList[0].toInt()
                month = timeList[1].toInt().minus(1)
                dayOfMonth = timeList[2].toInt()
            }
        }
        DatePickerDialog(
            view.context,
            DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                petBirthday.value = "$year/${month.plus(1)}/$dayOfMonth"
                _birthdayChange.value = true
            }, year, month, dayOfMonth
        ).show()
    }

    fun modifyPetProfile() {
        _isModified.value = true
    }

    fun updatePetProfile() {
        petBirthday.value?.let {
            val timeList = it.split("/")
            year = timeList[0].toInt()
            month = timeList[1].toInt().minus(1)
            dayOfMonth = timeList[2].toInt()
        }

        val finalProfile = mapOf(
            "name" to petName.value,
            "birthDay" to (calendar.timeInMillis / 1000),
            "idNumber" to petIdNumber.value
        )

        selectedPet.value?.profileId?.let {
            pets.document(it).update(finalProfile)
                .addOnSuccessListener {
                    modifyCompleted()
                    refreshPetProfile()
                    Log.d(ERIC, "updatePetProfile() succeeded ")
                }.addOnFailureListener {
                    Log.d(ERIC, "updatePetProfile() failed : $it")
                }
        }
    }

    fun modifyCompleted() {
        _isModified.value = false
    }

    fun modifyCancelled() {
        _isModified.value = false
        _selectedPet.value?.let {
            petName.value = it.name
            petIdNumber.value = it.idNumber
            petBirthday.value = it.birthDay?.timestampToDateString()
        }
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

    fun refreshPetProfile() {
        userProfile.value?.let { userProfile ->
            userProfile.pets?.apply {
                if (this.isNotEmpty()) {
                    getPetProfileData(userProfile)
                }
            }
        }
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