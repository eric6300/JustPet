package com.taiwan.justvet.justpet.data.source

import com.taiwan.justvet.justpet.data.PetEvent
import com.taiwan.justvet.justpet.data.PetProfile
import com.taiwan.justvet.justpet.data.UserProfile
import com.taiwan.justvet.justpet.util.LoadStatus

class DefaultJustPetRepository(
    private val justPetRemoteDataSource: JustPetDataSource
) : JustPetRepository {

    override suspend fun addNewPetProfile(petProfile: PetProfile): String {
        return justPetRemoteDataSource.addNewPetProfile(petProfile)
    }

    override suspend fun getPetProfiles(userProfile: UserProfile): List<PetProfile> {
        return justPetRemoteDataSource.getPetProfiles(userProfile)
    }

    override suspend fun getPetEvents(petProfile: PetProfile, timestamp: Long): List<PetEvent> {
        return justPetRemoteDataSource.getPetEvents(petProfile, timestamp)
    }

    override suspend fun getSyndromeEvents(
        petId: String,
        tagIndex: Long,
        timestamp: Long
    ): List<PetEvent> {
        return justPetRemoteDataSource.getSyndromeEvents(petId, tagIndex, timestamp)
    }

    override suspend fun getWeightEvents(petId: String, timestamp: Long): List<PetEvent> {
        return justPetRemoteDataSource.getWeightEvents(petId, timestamp)
    }

    override suspend fun updatePetProfile(petId: String, updateDataMap: Map<String, Any?>): LoadStatus {
        return justPetRemoteDataSource.updatePetProfile(petId, updateDataMap)
    }

    override suspend fun uploadPetProfileImage(petId: String, imageUri: String): String {
        return justPetRemoteDataSource.uploadPetProfileImage(petId, imageUri)
    }

    override suspend fun updatePetProfileImageUrl(petId: String, downloadUrl: String): LoadStatus {
        return justPetRemoteDataSource.updatePetProfileImageUrl(petId, downloadUrl)
    }

    override suspend fun updatePetsOfUserProfile(userID: String, petId: String): LoadStatus {
        return justPetRemoteDataSource.updatePetsOfUserProfile(userID, petId)
    }
}