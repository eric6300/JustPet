package com.taiwan.justvet.justpet.chart

import android.icu.util.Calendar
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.taiwan.justvet.justpet.EVENTS
import com.taiwan.justvet.justpet.PETS
import com.taiwan.justvet.justpet.TAG
import com.taiwan.justvet.justpet.UserManager
import com.taiwan.justvet.justpet.data.EventTag
import com.taiwan.justvet.justpet.data.PetEvent
import com.taiwan.justvet.justpet.data.PetProfile
import com.taiwan.justvet.justpet.data.UserProfile
import com.taiwan.justvet.justpet.util.TagType
import kotlinx.coroutines.launch

class ChartViewModel : ViewModel() {

    private val _listOfProfile = MutableLiveData<List<PetProfile>>()
    val listOfProfile: LiveData<List<PetProfile>>
        get() = _listOfProfile

    private val _selectedProfile = MutableLiveData<PetProfile>()
    val selectedProfile: LiveData<PetProfile>
        get() = _selectedProfile

    private val _eventData = MutableLiveData<List<PetEvent>>()
    val eventData: LiveData<List<PetEvent>>
        get() = _eventData

    val petData = mutableListOf<PetProfile>()
    var selectedEventTag: EventTag? = null

    val database = FirebaseFirestore.getInstance()
    val pets = database.collection(PETS)

    val calendar = Calendar.getInstance()
    var oneMonthTimestamp: Long = 0
    var threeMonthsTimestamp: Long = 0
    var sixMonthsTimestamp: Long = 0
    var oneYearTimestamp: Long = 0

    init {
        UserManager.userProfile.value?.let {
            selectedEventTag = EventTag(TagType.SYNDROME.value, 100, "嘔吐")
            getPetProfileData(it)
        }
        calculateTimestamp()
    }

    fun calculateTimestamp() {
        calendar.add(Calendar.MONTH, -1)
        oneMonthTimestamp = calendar.timeInMillis

        calendar.add(Calendar.MONTH, -2)
        threeMonthsTimestamp = calendar.timeInMillis

        calendar.add(Calendar.MONTH, -3)
        sixMonthsTimestamp = calendar.timeInMillis

        calendar.add(Calendar.MONTH, -6)
        oneYearTimestamp = calendar.timeInMillis
    }

    fun getProfileByPosition(position: Int) {
        _selectedProfile.value = petData[position]
    }

    fun getPetProfileData(userProfile: UserProfile) {
        userProfile.pets?.let {
            viewModelScope.launch {
                for (petId in it) {
                    pets.document(petId).get()
                        .addOnSuccessListener { document ->
                            val petProfile = PetProfile(
                                profileId = document.id,
                                name = document["name"] as String?,
                                species = document["species"] as Long?,
                                gender = document["gender"] as Long?,
                                neutered = document["neutered"] as Boolean?,
                                birthDay = document["birthDay"] as String?,
                                idNumber = document["idNumber"] as String?,
                                owner = document["owner"] as String?
                            )
                            petData.add(petProfile)
                            petData.sortBy { it.profileId }
                            _listOfProfile.value = petData
                            Log.d(TAG, "ChartViewModel getPetProfileData() succeeded")
                        }
                        .addOnFailureListener {
                            Log.d(TAG, "ChartViewModel getPetProfileData() failed : $it")
                        }
                }
            }
        }
    }

    fun getChartData(petProfile: PetProfile) {
        petProfile.profileId?.let {
            selectedEventTag?.index?.let { index ->
                pets.document(it).collection(EVENTS).whereArrayContains("eventTagsIndex", index)
                    .whereGreaterThan("timestamp" , oneYearTimestamp).get()
                    .addOnSuccessListener {
                        if (it.size() > 0) {
                            val data = mutableListOf<PetEvent>()

                            Log.d(TAG, "${it.size()} event(s) contain tag of ${selectedEventTag?.title}")

                            for (item in it.documents) {
                                val event = item.toObject(PetEvent::class.java)
                                event?.let {
                                    data.add(it)
                                }
                            }

                            Log.d(TAG, "one year data : $data")

                            _eventData.value = data
                        } else {
                            Log.d(TAG, "no event contains tag of vomit")
                        }
                    }.addOnFailureListener {
                        Log.d(TAG, "getChartData() failed : $it")
                    }
            }
        }
    }


}