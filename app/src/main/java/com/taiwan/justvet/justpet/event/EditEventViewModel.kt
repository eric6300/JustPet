package com.taiwan.justvet.justpet.event

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.taiwan.justvet.justpet.data.EventTag
import com.taiwan.justvet.justpet.util.TagType

class EditEventViewModel : ViewModel() {

    private val _navigateToCalendar = MutableLiveData<Boolean>()
    val navigateToCalendar: LiveData<Boolean>
        get() = _navigateToCalendar

    private val _expandStatus = MutableLiveData<Boolean>()
    val expandStatus: LiveData<Boolean>
        get() = _expandStatus

    private val _listOfTags = MutableLiveData<List<EventTag>>()
    val listOfTags: LiveData<List<EventTag>>
        get() = _listOfTags

    init {
        mockData()
    }

    fun mockData() {
        val listTagSyndrome = mutableListOf<EventTag>()
        listTagSyndrome.let {
            it.add(EventTag(TagType.SYNDROME, 100, "嘔吐"))
            it.add(EventTag(TagType.SYNDROME, 101, "下痢"))
            it.add(EventTag(TagType.SYNDROME, 102, "咳嗽"))
        }

        _listOfTags.value = listTagSyndrome
    }

    fun navigateToCalendar() {
        _navigateToCalendar.value = true
    }

    fun navigateToCalendarCompleted() {
        _navigateToCalendar.value = null
    }

    fun expandAdvanceMenu() {
        _expandStatus.value = _expandStatus.value != true
    }

}