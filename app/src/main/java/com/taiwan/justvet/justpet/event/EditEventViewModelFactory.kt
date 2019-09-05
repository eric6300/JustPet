package com.taiwan.justvet.justpet.event

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.taiwan.justvet.justpet.data.PetEvent

class EditEventViewModelFactory(
    private val petEvent: PetEvent
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditEventViewModel::class.java)) {
            return EditEventViewModel(petEvent) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
