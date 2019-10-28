package com.taiwan.justvet.justpet.data.source

import com.taiwan.justvet.justpet.data.PetProfile
import com.taiwan.justvet.justpet.data.UserProfile

interface JustPetDataSource {
    suspend fun getPetProfiles(userProfile: UserProfile) : List<PetProfile>
}