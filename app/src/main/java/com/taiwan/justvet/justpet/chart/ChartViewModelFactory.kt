package com.taiwan.justvet.justpet.chart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.taiwan.justvet.justpet.data.JustPetRepository

class ChartViewModelFactory(
    private val justPetRepository: JustPetRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {

        if (modelClass.isAssignableFrom(ChartViewModel::class.java)) {
            return ChartViewModel(justPetRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}