package com.taiwan.justvet.justpet.profile

import android.app.DatePickerDialog
import android.icu.util.Calendar
import android.net.Uri
import android.util.Log
import android.view.View
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.taiwan.justvet.justpet.*
import com.taiwan.justvet.justpet.data.PetProfile
import com.taiwan.justvet.justpet.data.UserProfile

class PetProfileViewModel : ViewModel() {

    private val _navigateToHomeFragment = MutableLiveData<Boolean>()
    val navigateToHomeFragment: LiveData<Boolean>
        get() = _navigateToHomeFragment

    private val _leaveDialog = MutableLiveData<Boolean>()
    val leaveDialog: LiveData<Boolean>
        get() = _leaveDialog

    private val _loadStatus = MutableLiveData<LoadApiStatus>()
    val loadStatus: LiveData<LoadApiStatus>
        get() = _loadStatus

    private val _errorName = MutableLiveData<String>()
    val errorName: LiveData<String>
        get() = _errorName

    private val _errorBirthday = MutableLiveData<String>()
    val errorBirthday: LiveData<String>
        get() = _errorBirthday

    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

    val petName = MutableLiveData<String>()
    val petSpecies = MutableLiveData<Long>()
    val petGender = MutableLiveData<Long>()
    val petBirthday = MutableLiveData<String>()
    val petIdNumber = MutableLiveData<String>()
    val petImage = MutableLiveData<String>()

    val firebase = FirebaseFirestore.getInstance()
    val users = firebase.collection(USERS)
    val petsReference = firebase.collection(PETS)
    val eventsReference = firebase.collection(EVENTS)
    val storageReference = FirebaseStorage.getInstance().reference

    init {
        petSpecies.value = 0
        petGender.value = 0
    }

    fun selectSpecies(species: Long) {
        petSpecies.value = species
    }

    fun selectGender(gender: Long) {
        petGender.value = gender
    }

    fun datePicker(view: View) {
        DatePickerDialog(
            view.context,
            DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                petBirthday.value = "$year/${month.plus(1)}/$dayOfMonth"
                calendar.set(year, month, dayOfMonth, 0, 0, 0)
            }, year, month, dayOfMonth
        ).show()
    }

    fun checkProfileText() {
        var index = 0
        if (petName.value.isNullOrEmpty()) {
            _errorName.value = "請輸入名字"
            index++
        }
        if (petBirthday.value.isNullOrEmpty()) {
            _errorBirthday.value = "請輸入出生日期"
            index++
        }
        if (index == 0) {
            newPetProfile()
        }
    }

    private fun newPetProfile() {
        _loadStatus.value = LoadApiStatus.LOADING
        UserManager.userProfile.value?.let { userProfile ->
            petBirthday.value?.let {
                val timeList = it.split("/")
                calendar.set(timeList[0].toInt(), timeList[1].toInt().minus(1), timeList[2].toInt())
            }

            petsReference.add(
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
                .addOnSuccessListener {
                    Log.d(ERIC, "newPetProfile() succeeded")
                    updatePetsOfUser(it.id)
                }
                .addOnFailureListener {
                    _loadStatus.value = LoadApiStatus.ERROR
                    Log.d(ERIC, "newPetProfile() failed : $it")
                }
        }
    }

    private fun updatePetsOfUser(petId: String) {
        UserManager.userProfile.value?.let { userProfile ->
            userProfile.profileId?.let { profileId ->
                users.document(profileId).update(PETS, FieldValue.arrayUnion(petId))
                    .addOnSuccessListener {

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
                                pets = newPets
                            )
                        )
                        uploadPetImage(petId)
                        Log.d(ERIC, "updatePetsOfUser() succeeded")
                    }
                    .addOnFailureListener {
                        _loadStatus.value = LoadApiStatus.ERROR
                        Log.d(ERIC, "updatePetsOfUser() failed : $it")
                    }
            }
        }
    }

    private fun uploadPetImage(petId: String) {
        if (petImage.value != null) {
            petImage.value?.let {
                val imageRef = storageReference.child("profile/$petId")
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
                            updateProfileImageUrl(petId, downloadUri)
                        }
                    }.addOnFailureListener {
                        _loadStatus.value = LoadApiStatus.ERROR
                        Log.d(ERIC, "uploadImage failed : $it")
                    }
            }
        } else {
            _loadStatus.value = LoadApiStatus.DONE
            navigateToHomeFragment()
        }
    }

    fun updateProfileImageUrl(petId: String, downloadUri: Uri?) {
        petsReference.let {
            it.document(petId).update("image", downloadUri.toString())
                .addOnSuccessListener {
                    _loadStatus.value = LoadApiStatus.DONE
                    navigateToHomeFragment()
                    Log.d(ERIC, "updateEventImageUrl succeed")
                }.addOnFailureListener {
                    _loadStatus.value = LoadApiStatus.ERROR
                    Log.d(ERIC, "updateEventImageUrl failed : $it")
                }
        }
    }

    fun navigateToHomeFragment() {
        _navigateToHomeFragment.value = true
    }

    fun navigateToHomeFragmentCompleted() {
        _navigateToHomeFragment.value = false
    }

    fun leaveDialog() {
        _leaveDialog.value = true
    }

    fun leaveDialogCompleted() {
        _leaveDialog.value = false
    }

}