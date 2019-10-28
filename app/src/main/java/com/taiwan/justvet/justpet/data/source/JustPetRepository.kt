package com.taiwan.justvet.justpet.data.source

import com.taiwan.justvet.justpet.data.PetProfile
import com.taiwan.justvet.justpet.data.UserProfile

interface JustPetRepository {
    suspend fun getPetProfiles(userProfile: UserProfile) : List<PetProfile>
}