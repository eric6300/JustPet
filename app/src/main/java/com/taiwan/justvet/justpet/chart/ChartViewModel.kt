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

    private val _syndromeData = MutableLiveData<Map<Date, ArrayList<PetEvent>>>()
    val syndromeData: LiveData<Map<Date, ArrayList<PetEvent>>>
        get() = _syndromeData

    val petProfileData = mutableListOf<PetProfile>()
    var selectedEventTag: EventTag? = null

    val database = FirebaseFirestore.getInstance()
    val petsRef = database.collection(PETS)

    val calendar = Calendar.getInstance()
//    var oneMonthTimestamp: Long = 0
//    var threeMonthsTimestamp: Long = 0
//    var sixMonthsTimestamp: Long = 0
    var oneYearTimestamp: Long = 0

    var sortedSyndromeDataMap: SortedMap<Date, ArrayList<PetEvent>>? = null

    init {
        UserManager.userProfile.value?.let {
            selectedEventTag = EventTag(TagType.SYNDROME.value, 100, "嘔吐")
            getPetProfileData(it)
        }
        calculateTimestamp()
    }

    fun calculateTimestamp() {
//        calendar.add(Calendar.MONTH, -1)
//        oneMonthTimestamp = calendar.timeInMillis
//
//        calendar.add(Calendar.MONTH, -2)
//        threeMonthsTimestamp = calendar.timeInMillis
//
//        calendar.add(Calendar.MONTH, -3)
//        sixMonthsTimestamp = calendar.timeInMillis

        calendar.add(Calendar.MONTH, -12)
        oneYearTimestamp = calendar.timeInMillis

        // return to default date
        calendar.add(Calendar.MONTH, 12)
        Log.d(TAG, "date (now) : ${calendar.time}")
//        Log.d(TAG, "timestamp (1 mon ago) : $oneMonthTimestamp")
//        Log.d(TAG, "timestamp (3 mon ago) : $threeMonthsTimestamp")
//        Log.d(TAG, "timestamp (6 mon ago) : $sixMonthsTimestamp")
        Log.d(TAG, "timestamp (12 mon ago) : $oneYearTimestamp")
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
                            Log.d(TAG, "ChartViewModel getPetProfileData() succeeded, petId : ${petProfile.profileId}")
                        }
                        .addOnFailureListener {
                            Log.d(TAG, "ChartViewModel getPetProfileData() failed : $it")
                        }
                }
            }
        }
    }

    fun getSyndromeData(petProfile: PetProfile) {
        petProfile.profileId?.let {
            selectedEventTag?.index?.let { index ->
                petsRef.document(it).collection(EVENTS).whereArrayContains("eventTagsIndex", index)
                    .whereGreaterThan("timestamp", oneYearTimestamp).get()
                    .addOnSuccessListener {
                        if (it.size() > 0) {
                            val data = mutableListOf<PetEvent>()

                            Log.d(
                                TAG,
                                "${petProfile.name} has ${it.size()} event(s) containing tag of ${selectedEventTag?.title}"
                            )

                            for (item in it.documents) {
                                val event = item.toObject(PetEvent::class.java)
                                event?.let {
                                    data.add(it)
                                }
                            }

                            _eventData.value = data

                            sortSyndromeData(12)
                        } else {
                            Log.d(TAG, "${petProfile.name} doesn't have event contains tag of vomit")
                        }
                    }.addOnFailureListener {
                        Log.d(TAG, "getSyndromeData() failed : $it")
                    }
            }
        }
    }

    fun sortSyndromeData(months: Int) {
        val dataMap = HashMap<Date, ArrayList<PetEvent>>()

        // create hashMap of last 12 months by year/month
        for (i in 0..months.minus(1)) {
//            Log.d(TAG, "${calendar.time}")
            dataMap[calendar.time] = arrayListOf<PetEvent>()
            calendar.add(Calendar.MONTH, -1)
        }

        // sort data into hashMap
        eventData.value?.forEach {
            val dateOfEvent = getDateOfEvent(it)
            if (dataMap.contains(dateOfEvent)) {
                (dataMap[dateOfEvent] as ArrayList<PetEvent>).add(it)
            } else {
                val newList = ArrayList<PetEvent>()
                newList.add(it)
                dateOfEvent?.let {
                    dataMap[dateOfEvent] = newList
                }
            }
        }

        // save sorted data for filter usage
        sortedSyndromeDataMap = dataMap.toSortedMap()

        // display syndrome data for Bar chart
        _syndromeData.value = sortedSyndromeDataMap

    }

    fun getDateOfEvent(petEvent: PetEvent): Date? {
        calendar.set(Calendar.YEAR, petEvent.year.toInt())
        calendar.set(Calendar.MONTH, petEvent.month.toInt().minus(1))
        return calendar.time
    }
}
