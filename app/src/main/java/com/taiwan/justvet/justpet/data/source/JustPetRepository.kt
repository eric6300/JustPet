package com.taiwan.justvet.justpet.data.source

import com.taiwan.justvet.justpet.data.PetProfile

interface JustPetRepository {
    suspend fun getPetProfiles() : List<PetProfile>
}