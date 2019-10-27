package com.taiwan.justvet.justpet.util

import android.content.Context
import androidx.annotation.VisibleForTesting
import com.taiwan.justvet.justpet.data.source.DefaultJustPetRepository
import com.taiwan.justvet.justpet.data.source.JustPetDataSource
import com.taiwan.justvet.justpet.data.source.JustPetRepository
import com.taiwan.justvet.justpet.data.source.remote.JustPetRemoteDataSource

object ServiceLocator {

    @Volatile
    var justPetRepository: JustPetRepository? = null
        @VisibleForTesting set

    fun provideTasksRepository(context: Context): JustPetRepository {
        synchronized(this) {
            return justPetRepository
                ?: justPetRepository
                ?: createJustPetRepository(context)
        }
    }

    private fun createJustPetRepository(context: Context): JustPetRepository {
        return DefaultJustPetRepository(JustPetRemoteDataSource)
    }

}