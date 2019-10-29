package com.taiwan.justvet.justpet.data.source

import com.taiwan.justvet.justpet.data.PetEvent
import com.taiwan.justvet.justpet.data.PetProfile
import com.taiwan.justvet.justpet.data.UserProfile
import com.taiwan.justvet.justpet.util.LoadStatus

interface JustPetRepository {
    suspend fun addNewPetProfile(petProfile: PetProfile): String

    suspend fun getPetProfiles(userProfile: UserProfile): List<PetProfile>

    suspend fun getPetEvents(petProfile: PetProfile, timestamp: Long): List<PetEvent>

    suspend fun getSyndromeEvents(
        petId: String,
        tagIndex: Long,
        timestamp: Long
    ): List<PetEvent>

    suspend fun getWeightEvents(petId: String, timestamp: Long): List<PetEvent>

    suspend fun updatePetProfile(petId: String, updateDataMap: Map<String, Any?>): LoadStatus

    suspend fun uploadPetProfileImage(petId: String, imageUri: String): String

    suspend fun updatePetProfileImageUrl(petId: String, downloadUrl: String): LoadStatus

    suspend fun updatePetsOfUserProfile(userID: String, petId: String): LoadStatus

    suspend fun uploadPetEventImage(eventId: String, imageUri: String): String

    suspend fun updatePetEventImageUrl(eventId: String, downloadUrl: String): LoadStatus
}