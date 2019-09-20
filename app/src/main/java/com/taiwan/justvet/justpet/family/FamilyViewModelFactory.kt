package com.taiwan.justvet.justpet.family

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.taiwan.justvet.justpet.data.PetProfile

class FamilyViewModelFactory(
    private val petProfile: PetProfile
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FamilyViewModel::class.java)) {
            return FamilyViewModel(petProfile) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}