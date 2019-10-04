package com.taiwan.justvet.justpet.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.icu.util.Calendar
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.core.net.toUri
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.taiwan.justvet.justpet.*
import com.taiwan.justvet.justpet.data.*
import com.taiwan.justvet.justpet.family.EMPTY_STRING
import com.taiwan.justvet.justpet.pet.IMAGE
import com.taiwan.justvet.justpet.pet.SLASH
import com.taiwan.justvet.justpet.tag.TagType
import com.taiwan.justvet.justpet.util.*
import com.taiwan.justvet.justpet.util.Util.getString
import java.util.*


class HomeViewModel : ViewModel() {
    private val _petList = MutableLiveData<List<PetProfile>>()
    val petList: LiveData<List<PetProfile>>
        get() = _petList

    private val _selectedPetProfile = MutableLiveData<PetProfile>()
    val selectedPetProfile: LiveData<PetProfile>
        get() = _selectedPetProfile

    private val _notificationList = MutableLiveData<List<EventNotification>>()
    val notificationList: LiveData<List<EventNotification>>
        get() = _notificationList

    private val _isPetProfileModified = MutableLiveData<Boolean>()
    val isPetProfileModified: LiveData<Boolean>
        get() = _isPetProfileModified

    private val _isBirthdayChanged = MutableLiveData<Boolean>()
    val isBirthdayChanged: LiveData<Boolean>
        get() = _isBirthdayChanged

    private val _navigateToFamilyDialog = MutableLiveData<PetProfile>()
    val navigateToFamilyDialog: LiveData<PetProfile>
        get() = _navigateToFamilyDialog

    private val _navigateToHomeFragment = MutableLiveData<Boolean>()
    val navigateToHomeFragment: LiveData<Boolean>
        get() = _navigateToHomeFragment

    private val _navigateToNewPetDialog = MutableLiveData<Boolean>()
    val navigateToNewPetDialog: LiveData<Boolean>
        get() = _navigateToNewPetDialog

    private val _showGallery = MutableLiveData<Boolean>()
    val showGallery: LiveData<Boolean>
        get() = _showGallery

    private val _showDatePicker = MutableLiveData<Boolean>()
    val showDatePicker: LiveData<Boolean>
        get() = _showDatePicker

    private val _eventList = MutableLiveData<List<PetEvent>>()
    val eventList: LiveData<List<PetEvent>>
        get() = _eventList

    private val _loadStatus = MutableLiveData<LoadStatus>()
    val loadStatus: LiveData<LoadStatus>
        get() = _loadStatus

    val tagVomit = EventTag(TagType.SYNDROME.value, 100, "嘔吐")

    val tagVaccine = EventTag(TagType.TREATMENT.value, 208, "疫苗注射")

    val tagWeight = EventTag(TagType.DIARY.value, 5, "量體重")

    val petName = MutableLiveData<String>()
    val petBirthdayString = MutableLiveData<String>()
    val petIdNumber = MutableLiveData<String>()
    val petSpecies = MutableLiveData<Long>()
    val petGender = MutableLiveData<Long>()
    val petImage = MutableLiveData<String>()

    var oneMonthAgoTimestamp = 0L
    var threeMonthsAgoTimestamp = 0L
    var sixMonthsAgoTimestamp = 0L
    var oneYearAgoTimestamp = 0L

    val petsReference = FirebaseFirestore.getInstance().collection(PETS)
    val storageReference = FirebaseStorage.getInstance().reference

    init {
        calculateTimestamp()
        UserManager.userProfile.value?.let { userProfile ->
            getPetProfileData(userProfile)
        }
    }

    fun calculateTimestamp() {
        val calendar = Calendar.getInstance(Locale.getDefault())

        calendar.add(Calendar.MONTH, -1)
        oneMonthAgoTimestamp = (calendar.timeInMillis / 1000)

        calendar.add(Calendar.MONTH, -2)
        threeMonthsAgoTimestamp = (calendar.timeInMillis / 1000)

        calendar.add(Calendar.MONTH, -3)
        sixMonthsAgoTimestamp = (calendar.timeInMillis / 1000)

        calendar.add(Calendar.MONTH, -6)
        oneYearAgoTimestamp = (calendar.timeInMillis / 1000)
    }

    fun getPetProfileData(userProfile: UserProfile) {

        if (userProfile.pets?.size != 0) {

            val petListFromFirebase = mutableListOf<PetProfile>()

            userProfile.pets?.let { pets ->

                fun getNextPetProfile(index: Int) {

                    if (index == pets.size) { // already get all pet data from firebase
                        _petList.value = petListFromFirebase.sortedBy { it.profileId }
                        return
                    }

                    petsReference.document(pets[index]).get()
                        .addOnSuccessListener { document ->

                            petListFromFirebase.add(document.toPetProfile())

                            getNextPetProfile(index.plus(1))
                            Log.d(ERIC, "getPetProfileData() succeeded: ${document.id}")
                        }
                        .addOnFailureListener {

                            getNextPetProfile(index.plus(1))
                            Log.d(ERIC, "getPetProfileData() failed: $it")
                        }
                }

                getNextPetProfile(0) //  get first pet profile
            }
        } else {
            _petList.value = mutableListOf()
            Log.d(ERIC, "viewModel petList = zero")
        }
    }

    fun selectPetProfile(index: Int) {
        _selectedPetProfile.value = _petList.value?.let {
            Log.d(ERIC, "selected pet profile id : ${it[index].profileId}")
            it[index]
        }
    }

    fun showPetProfile(petProfile: PetProfile) {
        petBirthdayString.value = petProfile.birthday.toDateString()
        petName.value = petProfile.name
        petIdNumber.value = petProfile.idNumber
        petSpecies.value = petProfile.species
        petGender.value = petProfile.gender
        petImage.value = null
    }

    fun getPetEvents(petProfile: PetProfile) {
        petProfile.profileId?.let {
            petsReference.document(it).collection(EVENTS)
                .whereGreaterThan(TIMESTAMP, oneYearAgoTimestamp).get()
                .addOnSuccessListener { events ->

                    if (events.size() > 0) {

                        val list = mutableListOf<PetEvent>()

                        fun getNextEvent(index: Int) {

                            if (index == events.size()) {
                                _eventList.value = list
                                return
                            }

                            events.documents[index].toObject(PetEvent::class.java)?.let { it ->
                                Log.d(ERIC, "$it")
                                list.add(it)
                            }

                            getNextEvent(index.plus(1))
                        }

                        getNextEvent(0)  // get first pet event

                    } else {
                        _eventList.value = mutableListOf()
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
            _selectedPetProfile.value?.let {
                EventNotification(
                    type = 2, title = "這個月已經${tagVomit.title} ${vomit.size} 次囉", eventTags = tags,
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
            _selectedPetProfile.value?.let {
                EventNotification(
                    type = 1, title = getString(R.string.text_vaccine_invalid), eventTags = tags,
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
            _selectedPetProfile.value?.let {
                EventNotification(
                    type = 0, title = "該幫 ${it.name} 量體重囉！", eventTags = tags,
                    eventTagsIndex = tagsIndex, petProfile = it
                )
            }?.let { notificationList.add(it) }
        }

        if (notificationList.isEmpty()) {
            _selectedPetProfile.value?.let {
                notificationList.add(
                    EventNotification(
                        type = -1,
                        title = getString(R.string.text_no_event_notification),
                        eventTags = emptyList(),
                        eventTagsIndex = emptyList(),
                        petProfile = it
                    )
                )
            }

            _notificationList.value = notificationList

        } else {
            _notificationList.value = notificationList.sortedBy {
                it.type
            }
        }
    }

    fun getPetBirthdayForDatePicker(): List<Int> {

        var year = 0
        var month = 0
        var dayOfMonth = 0

        when (petBirthdayString.value) {
            null -> {
                // show today in date picker
                val calendar = Calendar.getInstance(Locale.getDefault())
                year = calendar.get(Calendar.YEAR)
                month = calendar.get(Calendar.MONTH)
                dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
            }
            else -> {
                // show pet birthday in date picker
                petBirthdayString.value?.let {
                    val timeList = it.split(SLASH)
                    year = timeList[0].toInt()
                    month = timeList[1].toInt().minus(1)
                    dayOfMonth = timeList[2].toInt()
                }
            }
        }
        return listOf(year, month, dayOfMonth)
    }

    fun setPetBirthday(date: Date) {
        petBirthdayString.value = date.toDateFormat()
    }

    fun updatePetProfile() {
        when (petName.value) {
            null, EMPTY_STRING -> {
                Toast.makeText(
                    JustPetApplication.appContext,
                    Util.getString(R.string.text_name_empty_error),
                    Toast.LENGTH_SHORT
                ).show()
            }
            else -> {
                _loadStatus.value = LoadStatus.LOADING

                selectedPetProfile.value?.profileId?.let { petId ->
                    petsReference.document(petId).update(
                        mapOf(
                            NAME to petName.value,
                            BIRTHDAY to petBirthdayString.value?.toTimestamp(),
                            ID_NUMBER to petIdNumber.value,
                            SPECIES to petSpecies.value,
                            GENDER to petGender.value
                        )
                    ).addOnSuccessListener {
                        if (petImage.value != null) {
                            uploadImage(petId)
                        } else {
                            modifyPetProfileCompleted()
                            navigateToHomeFragment()
                            _loadStatus.value = LoadStatus.DONE
                        }
                        Log.d(ERIC, "updatePetProfile() succeeded ")
                    }.addOnFailureListener {
                        _loadStatus.value = LoadStatus.ERROR
                        Log.d(ERIC, "updatePetProfile() failed : $it")
                    }
                }
            }
        }
    }

    private fun uploadImage(petId: String) {
        petImage.value?.let {
            if (it.startsWith(HTTPS)) {
                navigateToHomeFragment()
                modifyPetProfileCompleted()
                _loadStatus.value = LoadStatus.DONE
            } else {
                val imageRef = storageReference.child(getString(R.string.text_profile_path, petId))

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
                            updateProfileImageUrl(petId, task.result)
                        }
                    }.addOnFailureListener {
                        _loadStatus.value = LoadStatus.ERROR
                        Log.d(ERIC, "uploadImage failed : $it")
                    }
            }
        }
    }

    private fun updateProfileImageUrl(petId: String, downloadUri: Uri?) {
        petsReference.document(petId).update(IMAGE, downloadUri.toString())
            .addOnSuccessListener {
                modifyPetProfileCompleted()
                navigateToHomeFragment()
                _loadStatus.value = LoadStatus.DONE
                Log.d(ERIC, "updateProfileImageUrl succeed")
            }.addOnFailureListener {
                _loadStatus.value = LoadStatus.ERROR
                Log.d(ERIC, "updateProfileImageUrl failed : $it")
            }

    }

    fun modifyPetProfile() {
        _isPetProfileModified.value = true
    }

    private fun modifyPetProfileCompleted() {
        _isPetProfileModified.value = false
        Toast.makeText(
            JustPetApplication.appContext,
            getString(R.string.text_profile_edit_success),
            Toast.LENGTH_LONG
        ).show()
    }

    fun modifyPetProfileCancelled() {
        _isPetProfileModified.value = false
        returnDefaultPetProfile()
    }

    private fun returnDefaultPetProfile() {
        _selectedPetProfile.value?.let {
            petName.value = it.name
            petIdNumber.value = it.idNumber
            petBirthdayString.value = it.birthday?.toDateString()
            petSpecies.value = it.species
            petGender.value = it.gender
        }
    }

    fun showDatePicker() {
        _showDatePicker.value = true
    }

    fun showDatePickerCompleted() {
        _showDatePicker.value = false
    }

    fun birthdayChanged() {
        _isBirthdayChanged.value = true
    }

    fun birthdayChangedCompleted() {
        _isBirthdayChanged.value = false
    }

    fun navigateToFamilyDialog(petProfile: PetProfile) {
        _navigateToFamilyDialog.value = petProfile
    }

    fun navigateToFamilyDialogCompleted() {
        _navigateToFamilyDialog.value = null
    }

    private fun navigateToHomeFragment() {
        _navigateToHomeFragment.value = true
    }

    fun navigateToHomeFragmentCompleted() {
        _navigateToHomeFragment.value = false
    }

    fun changeSpecies(species: Long) {
        petSpecies.value = species
    }

    fun changeGender(gender: Long) {
        petGender.value = gender
    }

    fun showGallery() {
        _showGallery.value = true
    }

    fun showGalleryCompleted() {
        _showGallery.value = false
    }

    fun navigateToNewPetDialog() {
        _navigateToNewPetDialog.value = true
    }

    fun navigateToNewPetDialogCompleted() {
        _navigateToNewPetDialog.value = false
    }

}