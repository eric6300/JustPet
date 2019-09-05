package com.taiwan.justvet.justpet.event

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.taiwan.justvet.justpet.R
import com.taiwan.justvet.justpet.data.EventTag
import com.taiwan.justvet.justpet.data.PetEvent
import com.taiwan.justvet.justpet.home.TAG
import com.taiwan.justvet.justpet.util.TagType
import com.taiwan.justvet.justpet.util.Util
import com.taiwan.justvet.justpet.util.Util.getString
import java.text.SimpleDateFormat
import java.util.*

class EditEventViewModel(val petEvent: PetEvent) : ViewModel() {

    private val _navigateToCalendar = MutableLiveData<Boolean>()
    val navigateToCalendar: LiveData<Boolean>
        get() = _navigateToCalendar

    private val _expandStatus = MutableLiveData<Boolean>()
    val expandStatus: LiveData<Boolean>
        get() = _expandStatus

    private val _listOfTags = MutableLiveData<List<EventTag>>()
    val listOfTags: LiveData<List<EventTag>>
        get() = _listOfTags

    private val _dateAndTime = MutableLiveData<String>()
    val dateAndTime: LiveData<String>
        get() = _dateAndTime

    init {
        setEventTags()
        setDateAndTime()
    }

    private fun setEventTags() {
        _listOfTags.value = petEvent.eventTags
    }

    private fun setDateAndTime() {
        val formatter = SimpleDateFormat(getString(R.string.date_time_format), Locale.TAIWAN)
        val dateAndTimeString = formatter.format(Date(petEvent.timeStamp))
        _dateAndTime.value = dateAndTimeString
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