package com.taiwan.justvet.justpet.ext

import android.app.Activity
import com.taiwan.justvet.justpet.JustPetApplication
import com.taiwan.justvet.justpet.factory.ViewModelFactory

fun Activity.getVmFactory(): ViewModelFactory {
    val repository = (applicationContext as JustPetApplication).justPetRepository
    return ViewModelFactory(repository)
}