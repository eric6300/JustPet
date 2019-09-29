package com.taiwan.justvet.justpet.tool

import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.taiwan.justvet.justpet.JustPetApplication

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

    fun comingSoon() {
        Toast.makeText(JustPetApplication.appContext, "Coming soon", Toast.LENGTH_LONG).show()
    }

}