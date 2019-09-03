package com.taiwan.justvet.justpet.calendar

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.taiwan.justvet.justpet.data.PetEvent

class CalendarViewModel : ViewModel() {

    private val _data = MutableLiveData<List<PetEvent>>()
    val data: LiveData<List<PetEvent>>
        get() = _data

    private val _listOfEvents = MutableLiveData<List<PetEvent>>()
    val listOfEvents: LiveData<List<PetEvent>>
        get() = _listOfEvents

    init {
        mockData()
    }

    fun mockData() {
        val list = mutableListOf<PetEvent>()

        list.let {
            it.add(PetEvent(dateString = "2019/8/13", timeString = "15:29"))
            it.add(PetEvent(dateString = "2019/9/15", timeString = "09:19"))
            it.add(PetEvent(dateString = "2019/9/28", timeString = "21:55"))
        }

        _listOfEvents.value = list
    }
}