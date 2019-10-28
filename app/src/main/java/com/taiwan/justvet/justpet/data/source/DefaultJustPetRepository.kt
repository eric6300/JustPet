package com.taiwan.justvet.justpet.data.source

import com.taiwan.justvet.justpet.data.PetEvent
import com.taiwan.justvet.justpet.data.PetProfile
import com.taiwan.justvet.justpet.data.UserProfile

class DefaultJustPetRepository(
    private val justPetRemoteDataSource: JustPetDataSource
) : JustPetRepository {

    override suspend fun getPetProfiles(userProfile: UserProfile): List<PetProfile> {
        return justPetRemoteDataSource.getPetProfiles(userProfile)
    }

    override suspend fun getPetEvents(petProfile: PetProfile, timestamp: Long): List<PetEvent> {
        return justPetRemoteDataSource.getPetEvents(petProfile, timestamp)
    }

}