package com.taiwan.justvet.justpet.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.app.DatePickerDialog
import android.icu.util.Calendar
import android.net.Uri
import android.util.Log
import android.view.View
import androidx.core.net.toUri
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.taiwan.justvet.justpet.*
import com.taiwan.justvet.justpet.UserManager.userProfile
import com.taiwan.justvet.justpet.data.*
import com.taiwan.justvet.justpet.util.timestampToDateString


class HomeViewModel() : ViewModel() {

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

    private val _navigateToAchievement = MutableLiveData<PetProfile>()
    val navigateToAchievement: LiveData<PetProfile>
        get() = _navigateToAchievement

    private val _navigateToNewPet = MutableLiveData<Boolean>()
    val navigateToNewPet: LiveData<Boolean>
        get() = _navigateToNewPet

    private val _startGallery = MutableLiveData<Boolean>()
    val startGallery: LiveData<Boolean>
        get() = _startGallery

    private val _eventsList = MutableLiveData<List<PetEvent>>()
    val eventsList: LiveData<List<PetEvent>>
        get() = _eventsList

    private val _inviteList = MutableLiveData<List<Invite>>()
    val inviteList: LiveData<List<Invite>>
        get() = _inviteList


    val petName = MutableLiveData<String>()
    val petBirthday = MutableLiveData<String>()
    val petIdNumber = MutableLiveData<String>()
    val petSpecies = MutableLiveData<Long>()
    val petGender = MutableLiveData<Long>()
    val petImage = MutableLiveData<String>()

    val calendar = Calendar.getInstance()
    var oneMonthAgoTimestamp: Long = 0
    var threeMonthsAgoTimestamp: Long = 0
    var sixMonthsAgoTimestamp: Long = 0
    var oneYearAgoTimestamp: Long = 0

    var year: Int = 0
    var month: Int = 0
    var dayOfMonth: Int = 0

    val firebase = FirebaseFirestore.getInstance()
    val petsReference = firebase.collection(PETS)
    val userReference = firebase.collection(USERS)
    val inviteReference = firebase.collection("invite")

    val storageReference = FirebaseStorage.getInstance().reference

    init {
        calculateTimestamp()
        UserManager.userProfile.value?.let { userProfile ->
            getPetProfileData(userProfile)
            Log.d(ERIC, "initial")
        }
    }

    fun calculateTimestamp() {
        calendar.add(Calendar.MONTH, -1)
        oneMonthAgoTimestamp = (calendar.timeInMillis / 1000)
        calendar.add(Calendar.MONTH, -2)
        threeMonthsAgoTimestamp = (calendar.timeInMillis / 1000)
        calendar.add(Calendar.MONTH, -3)
        sixMonthsAgoTimestamp = (calendar.timeInMillis / 1000)
        calendar.add(Calendar.MONTH, -6)
        oneYearAgoTimestamp = (calendar.timeInMillis / 1000)
        calendar.add(Calendar.MONTH, 12)
    }

    fun checkInvite() {
        userProfile.value?.let { userProfile ->
            inviteReference
                .whereEqualTo("inviteeEmail", userProfile.email)
                .get()
                .addOnSuccessListener { it ->
                    if (it.isEmpty) {
                        Log.d(ERIC, "no invite")
                    } else {
                        val list = mutableListOf<Invite>()
                        it.documents.forEach {
                            list.add(
                                Invite(
                                    inviteId = it.id,
                                    petId = it["petId"] as String?,
                                    petName = it["petName"] as String?,
                                    inviteeEmail = it["inviteeEmail"] as String?,
                                    inviterName = it["inviterName"] as String?,
                                    inviterEmail = it["inviterEmail"] as String?
                                )
                            )
                        }
                        _inviteList.value = list
                        Log.d(ERIC, "invite list : $list")
                    }
                }.addOnFailureListener {
                    Log.d(ERIC, "checkInvite() failed : $it")
                }
        }
    }

    fun showInvite(inviteList: MutableList<Invite>) {
        if (inviteList.isNotEmpty()) {
            _inviteList.value = inviteList
        } else {
            _inviteList.value = null
            Log.d(ERIC, "inviteList is empty")
        }
    }

    fun confirmInvite(invite: Invite) {
        UserManager.userProfile.value?.let { userProfile ->
            userReference.whereEqualTo("uid", userProfile.uid).get()
                .addOnSuccessListener {

                    val newPetList = mutableListOf<String>()
                    userProfile.pets?.let { newPetList.addAll(it) }
                    invite.petId?.let { newPetList.add(it) }

                    val newUserProfile = UserProfile(
                        profileId = userProfile.profileId,
                        uid = userProfile.uid,
                        email = userProfile.email,
                        pets = newPetList,
                        displayName = userProfile.displayName,
                        photoUrl = userProfile.photoUrl
                    )

                    updateUserProfile(invite)
                    updatePetProfileFamily(invite.petId)

                    UserManager.refreshUserProfile(newUserProfile)

                }.addOnFailureListener {
                    Log.d(ERIC, "confirmInvite() failed : $it")
                }
        }
    }

    private fun updatePetProfileFamily(petId: String?) {
        petId?.let {
            petsReference.document(it)
                .update("family", FieldValue.arrayUnion(userProfile.value?.email))
                .addOnSuccessListener {
                    Log.d(ERIC, "updatePetProfileFamily succeeded")
                }.addOnFailureListener {
                    Log.d(ERIC, "updatePetProfileFamily failed : $it")
                }
        }
    }

    private fun updateUserProfile(invite: Invite) {
        userProfile.value?.profileId?.let {
            userReference.document(it)
                .update("pets", FieldValue.arrayUnion(invite.petId))
                .addOnSuccessListener {
                    deleteInvite(invite)
                }.addOnFailureListener {
                    Log.d(ERIC, "updateUserProfile() failed")
                }
        }
    }

    private fun deleteInvite(invite: Invite) {
        invite.inviteId?.let {
            inviteReference.document(it).delete()
                .addOnSuccessListener {
                    Log.d(ERIC, "deleteInvite() succeeded")
                }.addOnFailureListener {
                    Log.d(ERIC, "deleteInvite() failed")
                }
        }
    }

    fun getPetProfileData(userProfile: UserProfile) {
        val petData = mutableListOf<PetProfile>()
        if (userProfile.pets?.size != 0) {
            userProfile.pets?.forEach {
                petsReference.document(it).get()
                    .addOnSuccessListener { profile ->
                        petData.add(
                            PetProfile(
                                profileId = profile.id,
                                name = profile["name"] as String?,
                                species = profile["species"] as Long?,
                                gender = profile["gender"] as Long?,
                                neutered = profile["neutered"] as Boolean?,
                                birthday = profile["birthday"] as Long?,
                                idNumber = profile["idNumber"] as String?,
                                owner = profile["owner"] as String?,
                                ownerEmail = profile["ownerEmail"] as String?,
                                family = profile["family"] as List<String>?,
                                image = profile["image"] as String?
                            )
                        )
                        petData.sortBy { it.profileId }
                        _petList.value = petData
                        Log.d(ERIC, "getPetProfileData() succeeded")
                        Log.d(ERIC, "getPetProfileData : $petData")
                    }
                    .addOnFailureListener {
                        Log.d(ERIC, "getPetProfileData() failed: $it")
                    }
            }
        } else {
            _petList.value = petData
            Log.d(ERIC, "viewModel petList = zero")
        }
    }

    fun selectPetProfile(index: Int) {
        _selectedPet.value = _petList.value?.let {
            Log.d(ERIC, "selected pet profile id : ${it[index].profileId}")
            it[index]
        }
    }

    fun showPetProfile(petProfile: PetProfile) {
        petName.value = petProfile.name
        petIdNumber.value = petProfile.idNumber
        petBirthday.value = petProfile.birthday?.timestampToDateString()
        petSpecies.value = petProfile.species
        petGender.value = petProfile.gender
        petImage.value = null

        petProfile.birthday?.let {
            calendar.timeInMillis = it * 1000
        }

    }

    fun getPetEvents(petProfile: PetProfile) {
        petProfile.profileId?.let {
            petsReference.document(it).collection(EVENTS)
                .whereGreaterThan("timestamp", oneYearAgoTimestamp).get()
                .addOnSuccessListener {
                    val list = mutableListOf<PetEvent>()

                    // return to default
                    _eventsList.value = list

                    if (it.size() > 0) {
                        it.documents.forEach {
                            val event = it.toObject(PetEvent::class.java)
                            event?.let { event ->
                                list.add(event)
                            }
                            _eventsList.value = list
                        }
                    }
                }.addOnFailureListener {
                    Log.d(ERIC, "getPetEvents() failed : $it")
                }
        }
    }

    fun filterForNotification(eventList: List<PetEvent>) {
        val notificationList = mutableListOf<EventNotification>()

        val vomit = mutableListOf<PetEvent>()
        val vaccine = mutableListOf<PetEvent>()
        val weight = mutableListOf<PetEvent>()


        eventList.forEach { event ->
            event.timestamp?.let { timestamp ->

                // filter for syndrome tags
                event.eventTagsIndex?.let { tags ->
                    if ((timestamp > oneMonthAgoTimestamp) && tags.contains(100)) {
                        vomit.add(event)
                    }
                    if ((timestamp > oneYearAgoTimestamp) && tags.contains(208)) {
                        vaccine.add(event)
                    }
                }

                // filter for weight measurement
                event.weight?.let {
                    if (timestamp > threeMonthsAgoTimestamp) {
                        weight.add(event)
                    }
                }
            }
        }
        if (vomit.size > 1) {
            notificationList.add(EventNotification(2, "這個月已經嘔吐 ${vomit.size} 次囉"))
        }
        if (vaccine.size == 0) {
            notificationList.add(EventNotification(1, "今年都還沒有打疫苗喔！"))
        }
        if (weight.size == 0) {
            notificationList.add(EventNotification(0, "該幫 ${selectedPet.value?.name} 量體重囉！"))
        }
        _notificationList.value = notificationList.sortedBy {
            it.type
        }
    }

    fun datePicker(view: View) {
        if (petBirthday.value == null) {
            year = calendar.get(Calendar.YEAR)
            month = calendar.get(Calendar.MONTH)
            dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        } else {
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
                calendar.set(year, month, dayOfMonth, 0, 0, 0)
                birthdayChange()
            }, year, month, dayOfMonth
        ).show()
    }

    fun modifyPetProfile() {
        _isModified.value = true
    }

    fun updatePetProfile() {
        val finalProfile = mapOf(
            "name" to petName.value,
            "birthday" to (calendar.timeInMillis / 1000),
            "idNumber" to petIdNumber.value,
            "species" to petSpecies.value,
            "gender" to petGender.value
        )

        selectedPet.value?.profileId?.let { profileId ->
            petsReference.document(profileId).update(finalProfile)
                .addOnSuccessListener {
                    if (petImage.value != null) {
                        uploadImage(profileId)
                    } else {
                        modifyCompleted()
                        refreshPetProfile()
                    }
                    Log.d(ERIC, "updatePetProfile() succeeded ")
                }.addOnFailureListener {
                    Log.d(ERIC, "updatePetProfile() failed : $it")
                }
        }
    }

    fun uploadImage(profileId: String) {
        petImage.value?.let {
            if (it.startsWith("https")) {
                modifyCompleted()
                refreshPetProfile()
            } else {
                val imageRef = storageReference.child("profile/$profileId")
                Log.d(ERIC, "uri : ${it.toUri()}")
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
                            updateProfileImageUrl(profileId, downloadUri)
                        }
                    }.addOnFailureListener {
                        Log.d(ERIC, "uploadImage failed : $it")
                    }
            }
        }
    }

    fun updateProfileImageUrl(profileId: String, downloadUri: Uri?) {
        petsReference.document(profileId).update("image", downloadUri.toString())
            .addOnSuccessListener {
                modifyCompleted()
                refreshPetProfile()
                Log.d(ERIC, "updateProfileImageUrl succeed")
            }.addOnFailureListener {
                Log.d(ERIC, "updateProfileImageUrl failed : $it")
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
            petBirthday.value = it.birthday?.timestampToDateString()
            petSpecies.value = it.species
            petGender.value = it.gender
        }
    }

    fun birthdayChange() {
        _birthdayChange.value = true
    }

    fun birthdayChangeCompleted() {
        _birthdayChange.value = false
    }

    fun navigateToAchievement(petProfile: PetProfile) {
        _navigateToAchievement.value = petProfile
    }

    fun navigateToAchievementCompleted() {
        _navigateToAchievement.value = null
    }

    private fun refreshPetProfile() {
        userProfile.value?.let { userProfile ->
            getPetProfileData(userProfile)
        }
    }

    fun changeSpecies(species: Long) {
        petSpecies.value = species
    }

    fun changeGender(gender: Long) {
        petGender.value = gender
    }

    fun startGallery() {
        _startGallery.value = true
    }

    fun startGalleryCompleted() {
        _startGallery.value = false
    }

    fun navigateToNewPet() {
        _navigateToNewPet.value = true
    }

    fun navigateToNewPetCompleted() {
        _navigateToNewPet.value = false
    }

}