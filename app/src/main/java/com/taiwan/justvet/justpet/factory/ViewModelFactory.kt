package com.taiwan.justvet.justpet.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.taiwan.justvet.justpet.MainViewModel
import com.taiwan.justvet.justpet.data.source.JustPetRepository
import com.taiwan.justvet.justpet.home.HomeViewModel

class ViewModelFactory constructor(
    private val justPetRepository: JustPetRepository
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>) =
        with(modelClass) {
            when {
                isAssignableFrom(MainViewModel::class.java) ->
                    MainViewModel(justPetRepository)

                else ->
                    throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        } as T
}