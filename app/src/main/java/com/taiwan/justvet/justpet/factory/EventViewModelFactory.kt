package com.taiwan.justvet.justpet.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.taiwan.justvet.justpet.data.PetEvent
import com.taiwan.justvet.justpet.data.source.JustPetRepository
import com.taiwan.justvet.justpet.event.EventViewModel

class EventViewModelFactory constructor(
    private val justPetRepository: JustPetRepository,
    private val petEvent: PetEvent
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel> create(modelClass: Class<T>) =
        with(modelClass) {
            when {
                isAssignableFrom(EventViewModel::class.java) ->
                    EventViewModel(justPetRepository, petEvent)
                else ->
                    throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        } as T
}
