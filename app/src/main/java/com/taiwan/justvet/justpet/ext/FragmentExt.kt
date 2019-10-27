package com.taiwan.justvet.justpet.ext

import androidx.fragment.app.Fragment
import com.taiwan.justvet.justpet.JustPetApplication
import com.taiwan.justvet.justpet.factory.ViewModelFactory

fun Fragment.getVmFactory(): ViewModelFactory {
    val repository = (requireContext().applicationContext as JustPetApplication).justPetRepository
    return ViewModelFactory(repository)
}