package com.taiwan.justvet.justpet.chart

import android.icu.util.Calendar
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.taiwan.justvet.justpet.*
import com.taiwan.justvet.justpet.data.*
import com.taiwan.justvet.justpet.data.source.JustPetRepository
import com.taiwan.justvet.justpet.tag.TagType
import com.taiwan.justvet.justpet.util.Util.calculateSyndromeDataSize
import com.taiwan.justvet.justpet.ext.toMonthOnlyFormat
import com.taiwan.justvet.justpet.util.LoadStatus
import kotlinx.coroutines.launch
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashMap

class ChartViewModel(val justPetRepository: JustPetRepository) : ViewModel() {

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

    private val _loadStatus = MutableLiveData<LoadStatus>()
    val loadStatus: LiveData<LoadStatus>
        get() = _loadStatus

    private var selectedEventTag: EventTag? = null

    private var nowTimestamp = 0L
    private var threeMonthsAgoTimestamp = 0L
    private var sixMonthsAgoTimestamp = 0L
    private var oneYearAgoTimestamp = 0L

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
            getPetProfiles(it)
            defaultSyndromeDataSize()
            defaultWeightDataSize()
        }
    }

    private fun calculateTimestamp() {
        val calendar = Calendar.getInstance()

        calendar.apply {
            nowTimestamp = (calendar.timeInMillis / 1000)

            set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), 1, 0, 0, 0)

            add(Calendar.MONTH, -2)
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

    private fun getPetProfiles(userProfile: UserProfile) {
        viewModelScope.launch {

            _loadStatus.value = LoadStatus.LOADING

            val pets = justPetRepository.getPetProfiles(userProfile)

            _loadStatus.value = LoadStatus.DONE

            _petList.value = pets
        }
    }

    fun getSyndromeEvents(petProfile: PetProfile) {

        viewModelScope.launch {

            val syndromeEvents =
                justPetRepository.getSyndromeEvents(
                    petProfile.profileId ?: EMPTY_STRING,
                    selectedEventTag?.index ?: -1,
                    oneYearAgoTimestamp
                )

            sortSyndromeEvents(syndromeEvents)

        }
    }

    private fun sortSyndromeEvents(data: List<PetEvent>) {
        viewModelScope.launch {

            defaultSyndromeDataSize()

            setSyndromeDataSize(
                calculateSyndromeDataSize(
                    data,
                    threeMonthsAgoTimestamp,
                    sixMonthsAgoTimestamp,
                    oneYearAgoTimestamp
                )
            )

            val dataMap = getLastYearMap()

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

    private fun setSyndromeDataSize(list: List<Int>) {
        _syndrome3MonthsDataSize.value = list[0]
        _syndrome6MonthsDataSize.value = list[1]
        _syndrome1YearDataSize.value = list[2]
    }

    private fun getLastYearMap(): LinkedHashMap<String, List<PetEvent>> {
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

        viewModelScope.launch {

            val weightEvents =
                justPetRepository.getWeightEvents(
                    petProfile.profileId ?: EMPTY_STRING,
                    oneYearAgoTimestamp
                )

            setEntriesForWeight(weightEvents)

        }

    }

    private fun setEntriesForWeight(weightData: List<PetEvent>) {

        defaultWeightDataSize()

        calculateWeightDataSize(weightData)

        val entries = ArrayList<Entry>()
        for (event in weightData) {
            event.weight?.let { weight ->
                entries.add(Entry(event.timestamp.toFloat(), weight.toFloat()))
            }
        }

        _weightEntries.value = entries
    }

    private fun defaultSyndromeDataSize() {
        _syndrome3MonthsDataSize.value = null
        _syndrome6MonthsDataSize.value = null
        _syndrome1YearDataSize.value = null
    }

    private fun defaultWeightDataSize() {
        _weight3MonthsDataSize.value = null
        _weight6MonthsDataSize.value = null
        _weight1YearDataSize.value = null
    }

    private fun calculateWeightDataSize(weightData: List<PetEvent>) {

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

    fun getNowTimestamp(): Long {
        return nowTimestamp
    }

    fun getThreeMonthsAgoTimestamp(): Long {
        return threeMonthsAgoTimestamp
    }

    fun getSixMonthsAgoTimestamp(): Long {
        return sixMonthsAgoTimestamp
    }

    fun getOneYearAgoTimestamp(): Long {
        return oneYearAgoTimestamp
    }
}
