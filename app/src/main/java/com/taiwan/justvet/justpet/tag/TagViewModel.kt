package com.taiwan.justvet.justpet.tag

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class TagViewModel : ViewModel() {

    private val _navigateToEditEvent = MutableLiveData<Boolean>()
    val navigateToEditEvent: LiveData<Boolean>
        get() = _navigateToEditEvent

    fun navigateToEditEvent() {
        _navigateToEditEvent.value = true
    }

    fun navigateToEditEventCompleted() {
        _navigateToEditEvent.value = null
    }

}