package com.taiwan.justvet.justpet.data.source

import com.taiwan.justvet.justpet.data.PetProfile

class DefaultJustPetRepository(
    private val justPetRemoteDataSource: JustPetDataSource
) : JustPetRepository {

    override suspend fun getPetProfiles(): List<PetProfile> {
        return justPetRemoteDataSource.getPetProfiles()
    }

}