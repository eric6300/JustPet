package com.taiwan.justvet.justpet.calendar

import androidx.lifecycle.*
import com.taiwan.justvet.justpet.data.EventTag
import com.taiwan.justvet.justpet.data.PetEvent
import com.taiwan.justvet.justpet.data.PetProfile
import com.taiwan.justvet.justpet.util.TagType
import kotlinx.coroutines.launch

class CalendarViewModel : ViewModel() {

    // get month data from firebase
    private val _data = MutableLiveData<List<PetEvent>>()
    val data: LiveData<List<PetEvent>>
        get() = _data

    private val _filterListOfEvents = MutableLiveData<List<PetEvent>>()
    val filterListOfEvents: LiveData<List<PetEvent>>
        get() = _filterListOfEvents

    private val _decorateListOfEvents = MutableLiveData<List<PetEvent>>()
    val decorateListOfEvents: LiveData<List<PetEvent>>
        get() = _decorateListOfEvents

    init {
        mockData()
    }

    fun mockData() {
        val list = mutableListOf<PetEvent>()
        val tagList = mutableListOf<EventTag>()
        val petProfile = PetProfile(
            idNumber = "900123123795226",
            id = "123123",
            birthDay = "2011/6/6",
            name = "mei",
            gender = 0,
            neutered = false,
            owner = "asdfasdf",
            species = 0
        )

        tagList.let {
            it.add(EventTag(TagType.DIARY, 0, "散步"))
            it.add(EventTag(TagType.DIARY, 0, "吃飯"))
            it.add(EventTag(TagType.DIARY, 0, "打咚咚"))
        }

        list.let {
            it.add(
                PetEvent(
                    year = 2019,
                    month = 9,
                    dayOfMonth = 12,
                    timeString = "15:29",
                    eventTags = tagList,
                    petProfile = petProfile,
                    timeStamp = 123456
                )
            )
            it.add(PetEvent(year = 2019, month = 9, dayOfMonth = 3, timeString = "15:15", petProfile = petProfile,
                timeStamp = 123456))
            it.add(
                PetEvent(
                    year = 2019,
                    month = 9,
                    dayOfMonth = 3,
                    timeString = "15:29",
                    eventTags = tagList,
                    petProfile = petProfile,
                    timeStamp = 123456
                )
            )
            it.add(PetEvent(year = 2019, month = 9, dayOfMonth = 15, timeString = "09:19", petProfile = petProfile,
                timeStamp = 123456))
            it.add(PetEvent(year = 2019, month = 9, dayOfMonth = 16, timeString = "21:55", petProfile = petProfile,
                timeStamp = 123456))
            it.add(PetEvent(year = 2019, month = 7, dayOfMonth = 25, timeString = "21:55", petProfile = petProfile,
                timeStamp = 123456))
            it.add(
                PetEvent(
                    year = 2019,
                    month = 8,
                    dayOfMonth = 18,
                    timeString = "21:55",
                    eventTags = tagList,
                    petProfile = petProfile,
                    timeStamp = 123456
                )
            )
            it.add(PetEvent(year = 2019, month = 10, dayOfMonth = 16, timeString = "21:55",
                petProfile=petProfile,
                timeStamp = 123456))
            it.add(PetEvent(year = 2019, month = 9, dayOfMonth = 17, timeString = "21:55",petProfile = petProfile,
                timeStamp = 123456))
        }

        _data.value = list
    }

    fun eventFilter(year: Int, month: Int, dayOfMonth: Int?) {
        _data.value?.let {
            if (dayOfMonth != null) {
                viewModelScope.launch {
                    val newList = it.filter { event ->
                        (event.year == year) && (event.month == month) && (event.dayOfMonth == dayOfMonth)
                    }
                    _filterListOfEvents.value = newList
                }
            } else {
                viewModelScope.launch {
                    // Get decoration list of the month
                    val newList = _data.value?.filter { event ->
                        (event.year == year) && (event.month == month)
                    }
                    _decorateListOfEvents.value = newList
                }
            }
        }
    }
}