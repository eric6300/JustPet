package com.taiwan.justvet.justpet

import android.app.Application
import android.content.Context
import com.taiwan.justvet.justpet.data.source.JustPetRepository
import com.taiwan.justvet.justpet.util.ServiceLocator
import kotlin.properties.Delegates

class JustPetApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        instance = this
    }

    val justPetRepository: JustPetRepository
        get() = ServiceLocator.provideTasksRepository(this)

    companion object {
        lateinit var appContext: Context
        var instance: JustPetApplication by Delegates.notNull()
    }
}