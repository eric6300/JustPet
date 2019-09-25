package com.taiwan.justvet.justpet.chart

import android.icu.util.Calendar
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.data.Entry
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

    private val _weightEntries = MutableLiveData<List<Entry>>()
    val weightEntries: LiveData<List<Entry>>
        get() = _weightEntries

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


    var nowTimestamp: Long = 0
    var threeMonthsAgoTimestamp: Long = 0
    var sixMonthsAgoTimestamp: Long = 0
    var oneYearAgoTimestamp: Long = 0

    var sortedSyndromeDataMap: SortedMap<Date, ArrayList<PetEvent>>? = null

    private var threeMonthsSyndrome = 0
    private var sixMonthsSyndrome = 0
    private var oneYearSyndrome = 0
    val threeMonthsWeight = MutableLiveData<Int>()
    val sixMonthsWeight = MutableLiveData<Int>()
    val oneYearWeight = MutableLiveData<Int>()

    init {
        UserManager.userProfile.value?.let {
            selectedEventTag = EventTag(TagType.SYNDROME.value, 100, "嘔吐")
            getPetProfileData(it)
        }
        calculateTimestamp()
    }

    fun calculateTimestamp() {
        val calendar = Calendar.getInstance()
        calendar.apply {
            nowTimestamp = (calendar.timeInMillis / 1000)

            this.add(Calendar.MONTH, -3)
            threeMonthsAgoTimestamp = (calendar.timeInMillis / 1000)

            this.add(Calendar.MONTH, -3)
            sixMonthsAgoTimestamp = (calendar.timeInMillis / 1000)

            this.add(Calendar.MONTH, -6)
            oneYearAgoTimestamp = (calendar.timeInMillis / 1000)
        }
    }

    fun getProfileByPosition(position: Int) {
        _selectedProfile.value = _listOfProfile.value?.get(position)
    }

    fun getPetProfileData(userProfile: UserProfile) {
        userProfile.pets?.let {
            viewModelScope.launch {
                var index = 0
                for (petId in it) {
                    petsRef.document(petId).get()
                        .addOnSuccessListener { document ->
                            val petProfile = PetProfile(
                                profileId = document.id,
                                name = document["name"] as String?,
                                species = document["species"] as Long?,
                                gender = document["gender"] as Long?,
                                neutered = document["neutered"] as Boolean?,
                                birthday = document["birthday"] as Long?,
                                idNumber = document["idNumber"] as String?,
                                owner = document["owner"] as String?,
                                ownerEmail = document["ownerEmail"] as String?,
                                family = document["family"] as List<String>?,
                                image = document["image"] as String?
                            )
                            petProfileData.add(petProfile)
                            index++
                            if (index == it.size) {
                                _listOfProfile.value = petProfileData.sortedBy { it.profileId }
                            }
                        }
                        .addOnFailureListener {
                            Log.d(ERIC, "ChartViewModel getPetProfileData() failed : $it")
                        }
                }
            }
        }
    }

    fun getSyndromeData(petProfile: PetProfile) {
        petProfile.profileId?.let {
            selectedEventTag?.index?.let { index ->
                petsRef.document(it).collection(EVENTS).whereArrayContains("eventTagsIndex", index)
                    .whereGreaterThan("timestamp", oneYearAgoTimestamp).get()
                    .addOnSuccessListener {
                        if (it.size() > 0) {
                            val data = mutableListOf<PetEvent>()

                            for (item in it.documents) {
                                val event = item.toObject(PetEvent::class.java)
                                event?.let {
                                    data.add(it)
                                }
                            }

                            _eventData.value = data
                            // get 12 months sorted syndrome data
                            sortSyndromeData(12)
                        } else {
                            _eventData.value = emptyList()
                            sortSyndromeData(12)
                        }
                    }.addOnFailureListener {
                        _eventData.value = emptyList()
                        sortSyndromeData(12)
                        Log.d(ERIC, "getSyndromeData() failed : $it")
                    }
            }
        }
    }

    private fun sortSyndromeData(months: Int) {
        val calendar = Calendar.getInstance()

        val dataMap = HashMap<Date, ArrayList<PetEvent>>()

        // create hashMap of last 12 months by year/month
        for (i in 1..months) {
            dataMap[calendar.time] = arrayListOf<PetEvent>()
            calendar.add(Calendar.MONTH, -1)
        }

        // sort data into hashMap
        eventData.value?.forEach { petEvent ->
            val dateOfEvent = getDateOfEvent(petEvent, calendar)

            if (dataMap.contains(dateOfEvent)) {
                (dataMap[dateOfEvent] as ArrayList<PetEvent>).add(petEvent)
            } else {
                val newList = ArrayList<PetEvent>()
                newList.add(petEvent)
                dateOfEvent?.let { dataMap[dateOfEvent] = newList }
            }
        }

        // save sorted data for filter usage
        sortedSyndromeDataMap = dataMap.toSortedMap()

        // display syndrome data for Bar chart
        _syndromeData.value = sortedSyndromeDataMap
    }

    fun getDateOfEvent(
        petEvent: PetEvent,
        calendar: Calendar
    ): Date? {
        val calendar2 = calendar.clone() as Calendar
        calendar2.set(Calendar.YEAR, petEvent.year.toInt())
        calendar2.set(Calendar.MONTH, petEvent.month.toInt().minus(1))
        return calendar2.time
    }

    fun getWeightData(petProfile: PetProfile) {
        petProfile.profileId?.let {
            petsRef.document(it).collection(EVENTS)
                .whereGreaterThan("timestamp", oneYearAgoTimestamp).get()
                .addOnSuccessListener {
                    if (it.size() > 0) {
                        val data = mutableListOf<PetEvent>()

                        for (item in it.documents) {
                            val event = item.toObject(PetEvent::class.java)
                            event?.let {
                                data.add(event)
                            }
                        }

                        // Setting Data
                        val weightData = data.filter {
                            it.weight != null
                        }

                        setEntriesForWeight(weightData)

                    } else {
                        setEntriesForWeight(emptyList())
                        Log.d(ERIC, "${petProfile.name} doesn't have event contains tag of weight")
                    }
                }.addOnFailureListener {
                    setEntriesForWeight(emptyList())
                    Log.d(ERIC, "getWeightData() failed : $it")
                }
        }
    }

    private fun setEntriesForWeight(weightData: List<PetEvent>) {

        filterWeightDataSize(weightData)

        val entries = ArrayList<Entry>()
        for (event in weightData) {
            event.timestamp?.let { timestamp ->
                event.weight?.let { weight ->
                    entries.add(Entry(timestamp.toFloat(), weight.toFloat()))
                }
            }
        }

        _weightEntries.value = entries
    }

    private fun filterWeightDataSize(weightData: List<PetEvent>) {
        var threeMonths = 0
        var sixMonths = 0
        var oneYear = 0

        weightData.forEach {
            it.timestamp?.let { timestamp ->
                if (timestamp >= threeMonthsAgoTimestamp) {
                    threeMonths += 1
                }
                if (timestamp >= sixMonthsAgoTimestamp) {
                    sixMonths += 1
                }
                if (timestamp >= oneYearAgoTimestamp) {
                    oneYear += 1
                }
            }
        }

        Log.d(ERIC, "3 : $threeMonths")
        Log.d(ERIC, "6 : $sixMonths")
        Log.d(ERIC, "12 : $oneYear")

        oneYearWeight.value = oneYear
        sixMonthsWeight.value = sixMonths
        threeMonthsWeight.value = threeMonths

    }
}
