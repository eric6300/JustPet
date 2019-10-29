package com.taiwan.justvet.justpet.pet

import android.app.DatePickerDialog
import android.icu.util.Calendar
import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.UploadTask
import com.taiwan.justvet.justpet.*
import com.taiwan.justvet.justpet.data.JustPetRepository
import com.taiwan.justvet.justpet.data.PetProfile
import com.taiwan.justvet.justpet.data.UserProfile
import com.taiwan.justvet.justpet.home.HomeViewModel.Companion.IMAGE
import com.taiwan.justvet.justpet.util.LoadStatus
import com.taiwan.justvet.justpet.util.Util.getString
import kotlinx.coroutines.launch

class AddNewPetViewModel(private val justPetRepository: com.taiwan.justvet.justpet.data.source.JustPetRepository) :
    ViewModel() {

    private val _leaveDialog = MutableLiveData<Boolean>()
    val leaveDialog: LiveData<Boolean>
        get() = _leaveDialog

    private val _loadStatus = MutableLiveData<LoadStatus>()
    val loadStatus: LiveData<LoadStatus>
        get() = _loadStatus

    private val _showGallery = MutableLiveData<Boolean>()
    val showGallery: LiveData<Boolean>
        get() = _showGallery

    private val _errorName = MutableLiveData<String>()
    val errorName: LiveData<String>
        get() = _errorName

    private val _errorBirthday = MutableLiveData<String>()
    val errorBirthday: LiveData<String>
        get() = _errorBirthday

    private val calendar = Calendar.getInstance()
    private val year = calendar.get(Calendar.YEAR)
    private val month = calendar.get(Calendar.MONTH)
    private val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

    val petName = MutableLiveData<String>()
    val petSpecies = MutableLiveData<Long>()
    val petGender = MutableLiveData<Long>()
    val petBirthday = MutableLiveData<String>()
    val petIdNumber = MutableLiveData<String>()
    val petImage = MutableLiveData<String>()

    init {
        petSpecies.value = PetSpecies.CAT.value
        petGender.value = PetGender.FEMALE.value
    }

    fun selectSpecies(species: Long) {
        petSpecies.value = species
    }

    fun selectGender(gender: Long) {
        petGender.value = gender
    }

    fun showDatePicker(view: View) {
        DatePickerDialog(
            view.context,
            DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                petBirthday.value = JustPetApplication.appContext.getString(
                    R.string.text_birthday_format,
                    year,
                    month.plus(1),
                    dayOfMonth
                )
                calendar.set(year, month, dayOfMonth, 0, 0, 0)
            }, year, month, dayOfMonth
        ).show()
    }

    fun checkProfileText() {
        var index = 0
        if (petName.value.isNullOrEmpty()) {
            _errorName.value = getString(R.string.text_empty_name_error)
            index++
        }
        if (petBirthday.value.isNullOrEmpty()) {
            _errorBirthday.value = getString(R.string.text_empty_birthday_error)
            index++
        }
        if (index == 0) {
            addNewPet()
        }
    }

    private fun addNewPet() {
        UserManager.userProfile.value?.let { userProfile ->

            viewModelScope.launch {

                _loadStatus.value = LoadStatus.LOADING

                petBirthday.value?.let {
                    val timeList = it.split(SLASH)
                    calendar.set(
                        timeList[0].toInt(), //  year
                        timeList[1].toInt().minus(1),  //  month
                        timeList[2].toInt()  //  dayOfMonth
                    )
                }

                val result = justPetRepository.addNewPetProfile(
                    PetProfile(
                        name = petName.value,
                        species = petSpecies.value,
                        gender = petGender.value,
                        idNumber = petIdNumber.value,
                        birthday = (calendar.timeInMillis / 1000),
                        owner = userProfile.profileId,
                        ownerEmail = userProfile.email
                    )
                )

                when (result) {
                    EMPTY_STRING -> {
                        _loadStatus.value = LoadStatus.ERROR

                        //  TODO : Error Handle
                    }
                    else -> {
                        updatePetsOfUser(result)
                    }
                }
            }
        }
    }

    private fun updatePetsOfUser(petId: String) {
        UserManager.userProfile.value?.profileId?.let { userId ->

            viewModelScope.launch {

                _loadStatus.value = LoadStatus.LOADING

                when (justPetRepository.updatePetsOfUserProfile(userId, petId)) {

                    LoadStatus.SUCCESS -> {

                        when (petImage.value) {

                            null -> {  //  no image of new pet

                                refreshUserProfile(petId)

                                _loadStatus.value = LoadStatus.DONE

                            }

                            else -> {  //  upload new pet image

                                uploadPetProfileImage(petId, petImage.value!!)

                            }
                        }
                    }
                    else -> {
                        _loadStatus.value = LoadStatus.ERROR

                        //  TODO : Error Handle
                    }
                }
            }
        }
    }

    private fun uploadPetProfileImage(petId: String, imageUri: String) {
        viewModelScope.launch {

            _loadStatus.value = LoadStatus.LOADING

            when (val downloadUrl = justPetRepository.uploadPetProfileImage(petId, imageUri)) {

                EMPTY_STRING -> {

                    _loadStatus.value = LoadStatus.ERROR

                    //  TODO Error Handle
                }

                else -> {

                    updateProfileImageUrl(petId, downloadUrl)

                }
            }
        }
    }

    private fun updateProfileImageUrl(petId: String, downloadUrl: String) {

        viewModelScope.launch {

            _loadStatus.value = LoadStatus.LOADING

            when (justPetRepository.updatePetProfileImageUrl(petId, downloadUrl)) {
                LoadStatus.SUCCESS -> {

                    refreshUserProfile(petId)
                    _loadStatus.value = LoadStatus.DONE

                }
                else -> {
                    _loadStatus.value = LoadStatus.ERROR

                    //  TODO  Error Handle
                }
            }
        }
    }

    private fun refreshUserProfile(petId: String) {
        UserManager.userProfile.value?.let { userProfile ->

            val newPets = arrayListOf<String>()

            userProfile.pets?.let {
                newPets.addAll(it)
            }

            newPets.add(petId)

            UserManager.refreshUserProfile(
                UserProfile(
                    profileId = userProfile.profileId,
                    uid = userProfile.uid,
                    email = userProfile.email,
                    displayName = userProfile.displayName,
                    pets = newPets
                )
            )

            Toast.makeText(
                JustPetApplication.appContext,
                getString(R.string.text_add_new_pet_profile_success),
                Toast.LENGTH_SHORT
            ).show()

            leaveDialog()
        }
    }

    fun showGallery() {
        _showGallery.value = true
    }

    fun showGalleryCompleted() {
        _showGallery.value = false
    }

    fun leaveDialog() {
        _leaveDialog.value = true
    }

    fun leaveDialogCompleted() {
        _leaveDialog.value = false
    }

}