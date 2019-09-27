package com.taiwan.justvet.justpet.tag

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.taiwan.justvet.justpet.data.PetEvent

class TagViewModelFactory(
    private val petEvent: PetEvent
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TagViewModel::class.java)) {
            return TagViewModel(petEvent) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}