package com.taiwan.justvet.justpet.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.taiwan.justvet.justpet.data.PetProfile
import android.app.DatePickerDialog
import android.icu.util.Calendar
import android.util.Log
import android.view.View
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import com.taiwan.justvet.justpet.data.userProfile


class HomeViewModel : ViewModel() {

    private val _petList = MutableLiveData<List<PetProfile>>()
    val petList: LiveData<List<PetProfile>>
        get() = _petList

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

    val db = FirebaseFirestore.getInstance()

    val pets = db.collection("pets")

    init {
        getPetData(mockUser())
    }

    fun mockUser(): userProfile {
        val petList = ArrayList<String>()
        petList.let {
            it.add("5DjrhdAlZka29LSmOe12")
            it.add("BR1unuBGFmeioH4VpKc2")
            it.add("FeHxkWD6VwpPMtL2bZT4")
        }
        return userProfile("eric6300", "6300eric@gmail.com", petList)
    }

    fun getPetData(userProfile: userProfile) {
        userProfile.pets?.let {
            val petData = mutableListOf<PetProfile>()
            viewModelScope.launch {
                for (petId in userProfile.pets) {
                    pets.document(petId).get()
                        .addOnSuccessListener { document ->
                            val petProfile = PetProfile(
                                name = document["name"] as String?,
                                species = document["species"] as Long?,
                                gender = document["gender"] as Long?,
                                neutered = document["neutered"] as Boolean?,
                                birthDay = document["birthDay"] as String?,
                                idNumber = document["idNumber"] as String?,
                                owner = document["owner"] as String?
                            )
                            petData.add(petProfile)
                            _petList.value = petData
//                            Log.d(
//                                TAG,
//                                "name:${petProfile.name} species:${petProfile.species} gender:${petProfile.gender}"
//                            )
//                            Log.d(
//                                TAG,
//                                "neutered:${petProfile.neutered} birthDay:${petProfile.birthDay} idNumber:${petProfile.idNumber} owner:${petProfile.owner}"
//                            )
                        }
                        .addOnFailureListener {
                            Log.d(TAG, "Failed")
                        }
                }
            }
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

}