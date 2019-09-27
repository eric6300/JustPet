package com.taiwan.justvet.justpet.tool

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ToolViewModel : ViewModel() {

    private val _navigateToBreath = MutableLiveData<Boolean>()
    val navigateToBreath: LiveData<Boolean>
        get() = _navigateToBreath
    
    fun navigateToBreath() {
        _navigateToBreath.value = true
    }

    fun navigateToBreathCompleted() {
        _navigateToBreath.value = false
    }

}