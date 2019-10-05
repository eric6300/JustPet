package com.taiwan.justvet.justpet.tool

import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.taiwan.justvet.justpet.JustPetApplication

class ToolViewModel : ViewModel() {

    private val _navigateToBreathFragment = MutableLiveData<Boolean>()
    val navigateToBreathFragment: LiveData<Boolean>
        get() = _navigateToBreathFragment
    
    fun navigateToBreathFragment() {
        _navigateToBreathFragment.value = true
    }

    fun navigateToBreathFragmentCompleted() {
        _navigateToBreathFragment.value = false
    }

    fun comingSoon() {
        Toast.makeText(JustPetApplication.appContext, "Coming soon", Toast.LENGTH_LONG).show()
    }

}