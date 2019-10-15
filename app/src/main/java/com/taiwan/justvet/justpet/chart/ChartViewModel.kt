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
import com.taiwan.justvet.justpet.event.EventViewModel.Companion.EVENT_TAGS_INDEX
import com.taiwan.justvet.justpet.event.EventViewModel.Companion.TIMESTAMP
import com.taiwan.justvet.justpet.tag.TagType
import com.taiwan.justvet.justpet.util.toMonthOnlyFormat
import com.taiwan.justvet.justpet.util.toPetProfile
import kotlinx.coroutines.launch
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashMap

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

    private val _syndrome3MonthsDataSize = MutableLiveData<Int>()
    val syndrome3MonthsDataSize: LiveData<Int>
        get() = _syndrome3MonthsDataSize

    private val _syndrome6MonthsDataSize = MutableLiveData<Int>()
    val syndrome6MonthsDataSize: LiveData<Int>
        get() = _syndrome6MonthsDataSize

    private val _syndrome1YearDataSize = MutableLiveData<Int>()
    val syndrome1YearDataSize: LiveData<Int>
        get() = _syndrome1YearDataSize

    private val _weight3MonthsDataSize = MutableLiveData<Int>()
    val weight3MonthsDataSize: LiveData<Int>
        get() = _weight3MonthsDataSize

    private val _weight6MonthsDataSize = MutableLiveData<Int>()
    val weight6MonthsDataSize: LiveData<Int>
        get() = _weight6MonthsDataSize

    private val _weight1YearDataSize = MutableLiveData<Int>()
    val weight1YearDataSize: LiveData<Int>
        get() = _weight1YearDataSize

    init {
        UserManager.userProfile.value?.let {
            selectedEventTag = EventTag(TagType.SYNDROME.value, 100, "嘔吐")
            calculateTimestamp()
            getPetProfileData(it)
            defaultDataSize()
        }
    }

    fun calculateTimestamp() {
        val calendar = Calendar.getInstance()

        calendar.apply {
            nowTimestamp = (calendar.timeInMillis / 1000)

            set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), 1, 0, 0, 0)

            Log.d(ERIC, "date : ${calendar.time}")

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

    private fun getPetProfileData(userProfile: UserProfile) {
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
                    .whereArrayContains(EVENT_TAGS_INDEX, index)
                    .whereGreaterThan(TIMESTAMP, oneYearAgoTimestamp).get()
                    .addOnSuccessListener {

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

            calculateSyndromeDataSize(data)

            val dataMap = setLastYearMap()

            if (data.isNotEmpty()) {
                // sort data into hashMap
                data.forEach { petEvent ->
                    val dateOfEvent = petEvent.getDateOfEvent()?.toMonthOnlyFormat()

                    if (dataMap.contains(dateOfEvent)) {
                        (dataMap[dateOfEvent] as MutableList<PetEvent>).add(petEvent)
                    }
                }
            }

            setEntriesForSyndrome(dataMap)
        }
    }

    private fun calculateSyndromeDataSize(syndromeData: List<PetEvent>) {
        defaultDataSize()

        var threeMonths = 0
        var sixMonths = 0
        var oneYear = 0

        syndromeData.forEach {
            if (it.timestamp >= threeMonthsAgoTimestamp) {
                threeMonths += 1
            }
            if (it.timestamp >= sixMonthsAgoTimestamp) {
                sixMonths += 1
            }
            if (it.timestamp >= oneYearAgoTimestamp) {
                oneYear += 1
            }
        }

        _syndrome3MonthsDataSize.value = threeMonths
        _syndrome6MonthsDataSize.value = sixMonths
        _syndrome1YearDataSize.value = oneYear

    }

    private fun setLastYearMap(): LinkedHashMap<String, List<PetEvent>> {
        val calendar = Calendar.getInstance()

        //  set the calendar to next month
        calendar.set(
            Calendar.MONTH,
            calendar.get(Calendar.MONTH).plus(1)
        )

        // create hashMap of last 12 months
        val map = LinkedHashMap<String, List<PetEvent>>()
        for (i in 1..12) {
            map[calendar.time.toMonthOnlyFormat()] = mutableListOf()
            calendar.add(Calendar.MONTH, 1)
        }

        return map
    }

    private fun setEntriesForSyndrome(syndromeData: Map<String, List<PetEvent>>) {
        viewModelScope.launch {
            // Setting Data
            val entries = mutableListOf<BarEntry>()

            var index = 1f

            for (date in syndromeData.keys) {
                syndromeData[date]?.size?.let {
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
                .whereGreaterThan(TIMESTAMP, oneYearAgoTimestamp).get()
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

        calculateWeightDataSize(weightData)

        val entries = ArrayList<Entry>()
        for (event in weightData) {
            event.weight?.let { weight ->
                entries.add(Entry(event.timestamp.toFloat(), weight.toFloat()))
            }
        }

        _weightEntries.value = entries
    }

    fun defaultDataSize() {
        _syndrome3MonthsDataSize.value = null
        _syndrome6MonthsDataSize.value = null
        _syndrome1YearDataSize.value = null

        _weight3MonthsDataSize.value = null
        _weight6MonthsDataSize.value = null
        _weight1YearDataSize.value = null
    }

    private fun calculateWeightDataSize(weightData: List<PetEvent>) {
        defaultDataSize()

        var threeMonths = 0
        var sixMonths = 0
        var oneYear = 0

        weightData.forEach {
            if (it.timestamp >= threeMonthsAgoTimestamp) {
                threeMonths += 1
            }
            if (it.timestamp >= sixMonthsAgoTimestamp) {
                sixMonths += 1
            }
            if (it.timestamp >= oneYearAgoTimestamp) {
                oneYear += 1
            }
        }

        _weight3MonthsDataSize.value = threeMonths
        _weight6MonthsDataSize.value = sixMonths
        _weight1YearDataSize.value = oneYear

    }
}
