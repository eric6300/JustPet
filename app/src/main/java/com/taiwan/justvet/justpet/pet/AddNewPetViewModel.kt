package com.taiwan.justvet.justpet.pet

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
import com.taiwan.justvet.justpet.home.HomeViewModel.Companion.IMAGE
import com.taiwan.justvet.justpet.util.LoadStatus
import com.taiwan.justvet.justpet.util.Util.getString

class AddNewPetViewModel : ViewModel() {

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

    val firebase = FirebaseFirestore.getInstance()
    private val usersReference = firebase.collection(USERS)
    private val petsReference = firebase.collection(PETS)
    private val storageReference = FirebaseStorage.getInstance().reference

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
        _loadStatus.value = LoadStatus.LOADING

        UserManager.userProfile.value?.let { userProfile ->
            petBirthday.value?.let {
                val timeList = it.split(SLASH)
                calendar.set(
                    timeList[0].toInt(), //  year
                    timeList[1].toInt().minus(1),  //  month
                    timeList[2].toInt())  //  dayOfMonth
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
            ).addOnSuccessListener {
                Log.d(ERIC, "addNewPet() succeeded")
                updatePetsOfUser(it.id)
            }.addOnFailureListener {
                _loadStatus.value = LoadStatus.ERROR
                Log.d(ERIC, "addNewPet() failed : $it")
            }
        }
    }

    private fun updatePetsOfUser(petId: String) {
        _loadStatus.value = LoadStatus.LOADING

        UserManager.userProfile.value?.let { userProfile ->
            userProfile.profileId?.let { profileId ->
                usersReference.document(profileId).update(PETS, FieldValue.arrayUnion(petId))
                    .addOnSuccessListener {
                        when (petImage.value) {
                            null -> {  //  no new pet image
                                refreshUserProfile(petId)
                                _loadStatus.value = LoadStatus.DONE
                            }
                            else -> {  //  new pet image to upload
                                uploadPetImage(petId)
                            }
                        }
                        Log.d(ERIC, "updatePetsOfUser() succeeded")
                    }
                    .addOnFailureListener {
                        _loadStatus.value = LoadStatus.ERROR
                        Log.d(ERIC, "updatePetsOfUser() failed : $it")
                    }
            }
        }
    }

    private fun uploadPetImage(petId: String) {
        _loadStatus.value = LoadStatus.LOADING

        petImage.value?.let {
            val imageRef =
                storageReference.child(getString(R.string.text_image_path, petId))
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
                    _loadStatus.value = LoadStatus.ERROR
                    Log.d(ERIC, "uploadImage failed : $it")
                }
        }
    }

    private fun updateProfileImageUrl(petId: String, downloadUri: Uri?) {
        _loadStatus.value = LoadStatus.LOADING
        UserManager.userProfile.value?.let { userProfile ->
            petsReference.let {
                it.document(petId).update(IMAGE, downloadUri.toString())
                    .addOnSuccessListener {
                        refreshUserProfile(petId)
                        _loadStatus.value = LoadStatus.DONE
                        Log.d(ERIC, "updateEventImageUrl succeed")
                    }.addOnFailureListener {
                        _loadStatus.value = LoadStatus.ERROR
                        Log.d(ERIC, "updateEventImageUrl failed : $it")
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
                    pets = newPets
                )
            )

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