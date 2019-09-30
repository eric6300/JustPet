package com.taiwan.justvet.justpet.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.app.DatePickerDialog
import android.icu.util.Calendar
import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.Toast
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
import com.taiwan.justvet.justpet.util.TagType
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

    private val _navigateToAchievement = MutableLiveData<PetProfile>()
    val navigateToAchievement: LiveData<PetProfile>
        get() = _navigateToAchievement

    private val _navigateToHome = MutableLiveData<Boolean>()
    val navigateToHome: LiveData<Boolean>
        get() = _navigateToHome

    private val _navigateToNewPet = MutableLiveData<Boolean>()
    val navigateToNewPet: LiveData<Boolean>
        get() = _navigateToNewPet

    private val _startGallery = MutableLiveData<Boolean>()
    val startGallery: LiveData<Boolean>
        get() = _startGallery

    private val _eventsList = MutableLiveData<List<PetEvent>>()
    val eventsList: LiveData<List<PetEvent>>
        get() = _eventsList

    private val _loadStatus = MutableLiveData<LoadApiStatus>()
    val loadStatus: LiveData<LoadApiStatus>
        get() = _loadStatus

    val tagVomit = EventTag(TagType.SYNDROME.value, 100, "嘔吐")
    val tagDiarrhea = EventTag(TagType.SYNDROME.value, 101, "下痢")
    val tagCough = EventTag(TagType.SYNDROME.value, 102, "咳嗽")
    val tagSneeze = EventTag(TagType.SYNDROME.value, 103, "打噴嚏")
    val tagItchy = EventTag(TagType.SYNDROME.value, 104, "搔癢")
    val tagSeizure = EventTag(TagType.SYNDROME.value, 105, "癲癇")
    val tagCollapse = EventTag(TagType.SYNDROME.value, 106, "昏倒")
    val tagAbnormalUrine = EventTag(TagType.SYNDROME.value, 107, "排尿異常")

    val tagEctoPrevent = EventTag(TagType.TREATMENT.value, 200, "除蚤")
    val tagEndoPrevent = EventTag(TagType.TREATMENT.value, 201, "驅蟲")
    val tagHeartWormPrevent = EventTag(TagType.TREATMENT.value, 202, "心絲蟲藥")
    val tagHealthExam = EventTag(TagType.TREATMENT.value, 209, "健康檢查")
    val tagVaccine = EventTag(TagType.TREATMENT.value, 208, "疫苗注射")

    val tagWeight = EventTag(TagType.DIARY.value, 5, "量體重")

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
    val inviteReference = firebase.collection("invites")

    val storageReference = FirebaseStorage.getInstance().reference

    init {
        calculateTimestamp()
        UserManager.userProfile.value?.let { userProfile ->
            getPetProfileData(userProfile)
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

    fun getPetProfileData(userProfile: UserProfile) {
        val petData = mutableListOf<PetProfile>()
        if (userProfile.pets?.size != 0) {
            userProfile.pets?.let {
                var index = 1
                for (petId in it) {
                    petsReference.document(petId).get()
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

                            if (index == it.size) {
                                _petList.value = petData.sortedBy { it.profileId }
                            }

                            index++
                        }
                        .addOnFailureListener {
                            Log.d(ERIC, "getPetProfileData() failed: $it")
                        }
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
                    // vomit
                    if ((timestamp > oneMonthAgoTimestamp) && tags.contains(tagVomit.index)) {
                        vomit.add(event)
                    }
                    // vaccine
                    if ((timestamp > oneYearAgoTimestamp) && tags.contains(tagVaccine.index)) {
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

        // Vomiting
        if (vomit.size > 1) {
            val tags = arrayListOf<EventTag>()
            val tagsIndex = arrayListOf<Long>()
            tags.add(tagVomit)
            tagVomit.index?.let { tagsIndex.add(it) }
            _selectedPet.value?.let {
                EventNotification(type = 2, title = "這個月已經${tagVomit.title} ${vomit.size} 次囉",eventTags = tags,
                    eventTagsIndex = tagsIndex, petProfile = it
                )
            }?.let { notificationList.add(it) }
        }
        // Vaccine
        if (vaccine.size == 0) {
            val tags = arrayListOf<EventTag>()
            val tagsIndex = arrayListOf<Long>()
            tags.add(tagVaccine)
            tagVaccine.index?.let { tagsIndex.add(it) }
            _selectedPet.value?.let {
                EventNotification(type = 1, title = "今年都還沒有打疫苗喔！",eventTags = tags,
                    eventTagsIndex = tagsIndex, petProfile = it
                )
            }?.let { notificationList.add(it) }
        }
        // Weight measure
        if (weight.size == 0) {
            val tags = arrayListOf<EventTag>()
            val tagsIndex = arrayListOf<Long>()
            tags.add(tagWeight)
            tagWeight.index?.let { tagsIndex.add(it) }
            _selectedPet.value?.let {
                EventNotification(type = 0, title = "該幫 ${it.name} 量體重囉！",eventTags = tags,
                    eventTagsIndex = tagsIndex, petProfile = it
                )
            }?.let { notificationList.add(it) }
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
        if (petName.value.isNullOrEmpty()) {
            Toast.makeText(JustPetApplication.appContext, "名字不可為空白", Toast.LENGTH_SHORT).show()
        } else {
            _loadStatus.value = LoadApiStatus.LOADING
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
                            navigateToHome()
                            _loadStatus.value = LoadApiStatus.DONE
                        }
                        Log.d(ERIC, "updatePetProfile() succeeded ")
                    }.addOnFailureListener {
                        _loadStatus.value = LoadApiStatus.ERROR
                        Log.d(ERIC, "updatePetProfile() failed : ${it.toString()}")
                    }
            }
        }
    }

    private fun uploadImage(profileId: String) {
        petImage.value?.let {
            if (it.startsWith("https")) {
                navigateToHome()
                modifyCompleted()
                _loadStatus.value = LoadApiStatus.DONE
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
                        _loadStatus.value = LoadApiStatus.ERROR
                        Log.d(ERIC, "uploadImage failed : $it")
                    }
            }
        }
    }

    private fun updateProfileImageUrl(profileId: String, downloadUri: Uri?) {
        petsReference.document(profileId).update("image", downloadUri.toString())
            .addOnSuccessListener {
                navigateToHome()
                modifyCompleted()
                _loadStatus.value = LoadApiStatus.DONE
                Log.d(ERIC, "updateProfileImageUrl succeed")
            }.addOnFailureListener {
                _loadStatus.value = LoadApiStatus.ERROR
                Log.d(ERIC, "updateProfileImageUrl failed : $it")
            }

    }

    private fun modifyCompleted() {
        Toast.makeText(JustPetApplication.appContext, "修改寵物資料成功！", Toast.LENGTH_LONG).show()
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

    private fun navigateToHome() {
        _navigateToHome.value = true
    }

    fun navigateToHomeCompleted() {
        _navigateToHome.value = false
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