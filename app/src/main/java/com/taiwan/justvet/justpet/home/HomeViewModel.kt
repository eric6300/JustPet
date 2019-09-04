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
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.taiwan.justvet.justpet.JustPetApplication
import kotlinx.coroutines.launch


class HomeViewModel : ViewModel() {

    val petName = MutableLiveData<String>()

    val petBirthDay = MutableLiveData<String>()

    val petIdChip = MutableLiveData<String>()

    val db = FirebaseFirestore.getInstance()

    val users = db.collection("users")

    init {
        getData()
    }

    fun getData() {
        viewModelScope.launch {
            users.whereEqualTo("email", "6300eric@gmail.com")
                .get()
                .addOnSuccessListener {
                    for (document in it) {
                        Log.d(TAG, "${document["pets"]}")
                    }
                }
                .addOnFailureListener {

                }
        }
    }

    private val _isModified = MutableLiveData<Boolean>()
    val isModified: LiveData<Boolean>
        get() = _isModified

    private val _birthdayChange = MutableLiveData<Boolean>()
    val birthdayChange: LiveData<Boolean>
        get() = _birthdayChange

    private val _navigateToAchievement = MutableLiveData<Boolean>()
    val navigateToAchievement: LiveData<Boolean>
        get() = _navigateToAchievement

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