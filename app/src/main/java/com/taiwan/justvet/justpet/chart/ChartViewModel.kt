package com.taiwan.justvet.justpet.chart

import android.icu.util.Calendar
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.google.firebase.firestore.FirebaseFirestore
import com.taiwan.justvet.justpet.*
import com.taiwan.justvet.justpet.data.EventTag
import com.taiwan.justvet.justpet.data.PetEvent
import com.taiwan.justvet.justpet.data.PetProfile
import com.taiwan.justvet.justpet.data.UserProfile
import com.taiwan.justvet.justpet.tag.TagType
import com.taiwan.justvet.justpet.util.toPetProfile
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ChartViewModel : ViewModel() {
    private val _petList = MutableLiveData<List<PetProfile>>()
    val petList: LiveData<List<PetProfile>>
        get() = _petList

    private val _selectedPetProfile = MutableLiveData<PetProfile>()
    val selectedPetProfile: LiveData<PetProfile>
        get() = _selectedPetProfile

    private val _weightEntries = MutableLiveData<List<Entry>>()
    val weightEntries: LiveData<List<Entry>>
        get() = _weightEntries

    private val _syndromeEntries = MutableLiveData<List<BarEntry>>()
    val syndromeEntries: LiveData<List<BarEntry>>
        get() = _syndromeEntries

    var selectedEventTag: EventTag? = null

    val petsReference = FirebaseFirestore.getInstance().collection(PETS)

    var nowTimestamp = 0L
    var threeMonthsAgoTimestamp = 0L
    var sixMonthsAgoTimestamp = 0L
    var oneYearAgoTimestamp = 0L

    init {
        UserManager.userProfile.value?.let {
            selectedEventTag = EventTag(TagType.SYNDROME.value, 100, "嘔吐")
            calculateTimestamp()
            getPetProfileData(it)
        }
    }

    fun calculateTimestamp() {
        val calendar = Calendar.getInstance()

        calendar.apply {
            nowTimestamp = (calendar.timeInMillis / 1000)

            set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), 1, 0, 0, 0)

            add(Calendar.MONTH, -3)
            threeMonthsAgoTimestamp = (timeInMillis / 1000)

            add(Calendar.MONTH, -3)
            sixMonthsAgoTimestamp = (timeInMillis / 1000)

            add(Calendar.MONTH, -6)
            oneYearAgoTimestamp = (timeInMillis / 1000)
        }
    }

    fun getProfileByPosition(position: Int) {
        _selectedPetProfile.value = _petList.value?.get(position)
    }

    fun getPetProfileData(userProfile: UserProfile) {
        userProfile.pets?.let { pets ->

            val list = mutableListOf<PetProfile>()

            fun getNextProfile(index: Int) {
                if (index == pets.size) {
                    _petList.value = list.sortedBy { it.profileId }
                    return
                }

                petsReference.document(pets[index]).get()
                    .addOnSuccessListener { document ->
                        list.add(document.toPetProfile())
                        getNextProfile(index.plus(1))
                    }
                    .addOnFailureListener {
                        getNextProfile(index.plus(1))
                        Log.d(ERIC, "ChartViewModel getPetList() failed : $it")
                    }
            }

            getNextProfile(0)
        }
    }

    fun getSyndromeData(petProfile: PetProfile) {
        petProfile.profileId?.let {
            selectedEventTag?.index?.let { index ->
                petsReference.document(it).collection(EVENTS)
                    .whereArrayContains("eventTagsIndex", index)
                    .whereGreaterThan("timestamp", oneYearAgoTimestamp).get()
                    .addOnSuccessListener {
                        Log.d(ERIC, "one year : $oneYearAgoTimestamp")

                        if (it.size() > 0) {
                            val data = mutableListOf<PetEvent>()

                            for (item in it.documents) {
                                val event = item.toObject(PetEvent::class.java)
                                event?.let {
                                    data.add(it)
                                }
                            }

                            sortSyndromeData(data)  // get 12 months sorted syndrome data
                        } else {
                            sortSyndromeData(emptyList())
                        }
                    }.addOnFailureListener {
                        sortSyndromeData(emptyList())
                        Log.d(ERIC, "getSyndromeEntries() failed : $it")
                    }
            }
        }
    }

    private fun sortSyndromeData(
        data: List<PetEvent>
    ) {
        viewModelScope.launch {
            val calendar = Calendar.getInstance()

            //  set the calendar to first day of next month
            calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH.plus(1)), 1, 0, 0, 0)

            // create hashMap of last 12 months
            val dataMap = HashMap<Date, List<PetEvent>>()

            for (i in 1..12) {
                calendar.add(Calendar.MONTH, -1)
                dataMap[calendar.time] = mutableListOf()
            }

            if (data.isNotEmpty()) {
                // sort data into hashMap
                data.forEach { petEvent ->
                    val dateOfEvent = getDateOfEvent(petEvent, calendar)
                    if (dataMap.contains(dateOfEvent)) {
                        (dataMap[dateOfEvent] as MutableList<PetEvent>).add(petEvent)
                    } else {
                        val newList = ArrayList<PetEvent>()
                        newList.add(petEvent)
                        dateOfEvent?.let { dataMap[dateOfEvent] = newList }
                    }
                }
            }
            val sortedMap = dataMap.toSortedMap()
            setEntriesForSyndrome(sortedMap)
        }
    }

    fun getDateOfEvent(
        petEvent: PetEvent,
        calendar: Calendar
    ): Date? {
        val calendarClone = calendar.clone() as Calendar
        calendarClone.set(Calendar.YEAR, petEvent.year.toInt())
        calendarClone.set(Calendar.MONTH, petEvent.month.toInt().minus(1))
        return calendarClone.time
    }

    fun setEntriesForSyndrome(syndromeData: Map<Date, List<PetEvent>>) {
        viewModelScope.launch {
            // Setting Data
            val entries = mutableListOf<BarEntry>()

            var threeMonths = 0
            var sixMonths = 0
            var oneYear = 0
            var index = 1f

            for (date in syndromeData.keys) {

                syndromeData[date]?.size?.let {
                    if (index in 1f..12f) {
                        oneYear += it
                    }
                    if (index in 7f..12f) {
                        sixMonths += it
                    }
                    if (index in 10f..12f) {
                        threeMonths += it
                    }
                    entries.add(BarEntry(index, it.toFloat()))
                    index += 1f
                }
            }

            _syndromeEntries.value = entries
        }
    }

    fun getWeightData(petProfile: PetProfile) {
        petProfile.profileId?.let {
            petsReference.document(it).collection(EVENTS)
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

    }
}
