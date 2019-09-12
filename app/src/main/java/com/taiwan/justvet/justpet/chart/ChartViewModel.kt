package com.taiwan.justvet.justpet.chart

import android.icu.util.Calendar
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.taiwan.justvet.justpet.*
import com.taiwan.justvet.justpet.data.EventTag
import com.taiwan.justvet.justpet.data.PetEvent
import com.taiwan.justvet.justpet.data.PetProfile
import com.taiwan.justvet.justpet.data.UserProfile
import com.taiwan.justvet.justpet.util.TagType
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

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

    val petProfileData = mutableListOf<PetProfile>()
    var selectedEventTag: EventTag? = null

    val database = FirebaseFirestore.getInstance()
    val petsRef = database.collection(PETS)

    val calendar = Calendar.getInstance()
    var oneMonthTimestamp: Long = 0
    var threeMonthsTimestamp: Long = 0
    var sixMonthsTimestamp: Long = 0
    var oneYearTimestamp: Long = 0

    val eventDataMap = HashMap<Date, ArrayList<PetEvent>>()

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

        calendar.add(Calendar.MONTH, 12)
        Log.d(TAG, "${calendar.time}")
    }

    fun getProfileByPosition(position: Int) {
        _selectedProfile.value = petProfileData[position]
    }

    fun getPetProfileData(userProfile: UserProfile) {
        userProfile.pets?.let {
            viewModelScope.launch {
                for (petId in it) {
                    petsRef.document(petId).get()
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
                            petProfileData.add(petProfile)
                            petProfileData.sortBy { it.profileId }
                            _listOfProfile.value = petProfileData
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
                petsRef.document(it).collection(EVENTS).whereArrayContains("eventTagsIndex", index)
                    .whereGreaterThan("timestamp", oneYearTimestamp).get()
                    .addOnSuccessListener {
                        if (it.size() > 0) {
                            val data = mutableListOf<PetEvent>()

                            Log.d(
                                TAG,
                                "${it.size()} event(s) contain tag of ${selectedEventTag?.title}"
                            )

                            for (item in it.documents) {
                                val event = item.toObject(PetEvent::class.java)
                                event?.let {
                                    data.add(it)
                                }
                            }

                            Log.d(TAG, "one year data : $data")

                            _eventData.value = data

                            sortDataByMonths(12)
                        } else {
                            Log.d(TAG, "no event contains tag of vomit")
                        }
                    }.addOnFailureListener {
                        Log.d(TAG, "getChartData() failed : $it")
                    }
            }
        }
    }

    fun sortDataByMonths(months: Int) {
        // create hashMap of last 12 months by year/month
        for (i in 0..months.minus(1)) {
            Log.d(TAG, "${calendar.time}")
            eventDataMap[calendar.time] = arrayListOf<PetEvent>()
            calendar.add(Calendar.MONTH, -1)
        }

        // sort data into hashMap
        eventData.value?.forEach {
            val dateOfEvent = getDateOf(it)
            if (eventDataMap.contains(dateOfEvent)) {
                (eventDataMap[dateOfEvent] as ArrayList<PetEvent>).add(it)
            } else {
                val newList = ArrayList<PetEvent>()
                newList.add(it)
                dateOfEvent?.let {
                    eventDataMap[dateOfEvent] = newList
                }
            }
        }

        val sortedMap = eventDataMap.toSortedMap()

        for (key in sortedMap.keys) {
            Log.d(TAG, "$key" + " : ${eventDataMap[key]?.size}")
        }
    }

    fun getDateOf(petEvent: PetEvent): Date? {
        calendar.set(Calendar.YEAR, petEvent.year.toInt())
        calendar.set(Calendar.MONTH, petEvent.month.toInt().minus(1))
        Log.d(TAG, "${petEvent.timestamp}, ${petEvent.year} , ${petEvent.month}")
        return calendar.time
    }
}
