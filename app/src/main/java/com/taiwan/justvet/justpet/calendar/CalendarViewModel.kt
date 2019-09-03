package com.taiwan.justvet.justpet.calendar

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.taiwan.justvet.justpet.data.PetEvent

class CalendarViewModel : ViewModel() {

    private val _listOfEvents = MutableLiveData<List<PetEvent>>()
    val listOfEvents: LiveData<List<PetEvent>>
        get() = _listOfEvents

    init {
        mockData()
    }

    fun mockData() {
        val list = mutableListOf<PetEvent>()

        list.let {
            it.add(PetEvent())
            it.add(PetEvent())
            it.add(PetEvent())
        }

        _listOfEvents.value = list
    }


}