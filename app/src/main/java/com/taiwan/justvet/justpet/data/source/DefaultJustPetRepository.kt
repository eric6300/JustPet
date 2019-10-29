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

    override suspend fun getSyndromeEvents(
        profileId: String,
        tagIndex: Long,
        timestamp: Long
    ): List<PetEvent> {
        return justPetRemoteDataSource.getSyndromeEvents(profileId, tagIndex, timestamp)
    }

    override suspend fun getWeightEvents(profileId: String, timestamp: Long): List<PetEvent> {
        return justPetRemoteDataSource.getWeightEvents(profileId, timestamp)
    }

    override suspend fun uploadPetProfileImage(imageUri: String, petId: String): String {
        return justPetRemoteDataSource.uploadPetProfileImage(imageUri, petId)
    }

    override suspend fun updatePetProfileImageUrl(petId: String, downloadUrl: String): Boolean {
        return justPetRemoteDataSource.updatePetProfileImageUrl(petId, downloadUrl)
    }
}