package com.taiwan.justvet.justpet.data.source

import com.taiwan.justvet.justpet.data.PetEvent
import com.taiwan.justvet.justpet.data.PetProfile
import com.taiwan.justvet.justpet.data.UserProfile

interface JustPetDataSource {
    suspend fun getPetProfiles(userProfile: UserProfile): List<PetProfile>

    suspend fun getPetEvents(petProfile: PetProfile, timestamp: Long): List<PetEvent>

    suspend fun getSyndromeEvents(
        profileId: String,
        tagIndex: Long,
        timestamp: Long
    ): List<PetEvent>

    suspend fun getWeightEvents(profileId: String, timestamp: Long): List<PetEvent>

    suspend fun uploadPetProfileImage(imageUri: String, petId: String): String
}