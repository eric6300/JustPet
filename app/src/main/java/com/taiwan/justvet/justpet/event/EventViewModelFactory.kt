package com.taiwan.justvet.justpet.event

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.taiwan.justvet.justpet.data.PetEvent

class EventViewModelFactory(
    private val petEvent: PetEvent
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EventViewModel::class.java)) {
            return EventViewModel(petEvent) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
