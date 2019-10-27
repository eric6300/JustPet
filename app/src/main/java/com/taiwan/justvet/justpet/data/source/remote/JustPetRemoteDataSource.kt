package com.taiwan.justvet.justpet.data.source.remote

import com.taiwan.justvet.justpet.data.PetProfile
import com.taiwan.justvet.justpet.data.source.JustPetDataSource

object JustPetRemoteDataSource : JustPetDataSource {

    override suspend fun getPetProfiles(): List<PetProfile> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}