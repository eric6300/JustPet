package com.taiwan.justvet.justpet.data.source.remote

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.storage.FirebaseStorage
import com.taiwan.justvet.justpet.*
import com.taiwan.justvet.justpet.UserManager.userProfile
import com.taiwan.justvet.justpet.data.JustPetRepository
import com.taiwan.justvet.justpet.data.PetEvent
import com.taiwan.justvet.justpet.data.PetProfile
import com.taiwan.justvet.justpet.data.UserProfile
import com.taiwan.justvet.justpet.data.source.JustPetDataSource
import com.taiwan.justvet.justpet.event.EventViewModel
import com.taiwan.justvet.justpet.ext.toPetProfile
import com.taiwan.justvet.justpet.util.LoadStatus
import kotlinx.coroutines.tasks.await

object JustPetRemoteDataSource : JustPetDataSource {

    val firestoreInstance: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    val storageInstance: FirebaseStorage by lazy { FirebaseStorage.getInstance() }

    private val usersReference = firestoreInstance.collection(USERS)
    private val petsReference = firestoreInstance.collection(PETS)
    private val inviteReference = firestoreInstance.collection(INVITES)

    override suspend fun getPetProfiles(userProfile: UserProfile): List<PetProfile> {

        when (userProfile.pets?.size) {
            0 -> {
                Log.d(ERIC, "pets size = zero")
                return emptyList()
            }
            else -> {
                val pets = userProfile.pets ?: emptyList()
                val petListFromFirebase = mutableListOf<PetProfile>()

                for (index in 0 until pets.size) {
                    val result = try {

                        petsReference.document(pets[index]).get().await()

                    } catch (e: FirebaseFirestoreException) {

                        Log.d(ERIC, "error: $e")

                        continue
                    }

                    petListFromFirebase.add(result.toPetProfile())

                }

                return petListFromFirebase.sortedBy { it.profileId }

            }
        }
    }

    override suspend fun getPetEvents(
        petProfile: PetProfile,
        timestamp: Long
    ): List<PetEvent> {

        val profileId = petProfile.profileId ?: EMPTY_STRING

        val result = petsReference.document(profileId).collection(EVENTS)
            .whereGreaterThan(EventViewModel.TIMESTAMP, timestamp).get().await()

        if (result == null) {

            return emptyList()

        } else {

            return when (result.size()) {

                0 -> emptyList()

                else -> {

                    val eventListFromFirebase = mutableListOf<PetEvent>()

                    for (index in 0 until result.size()) {
                        result.documents[index].toObject(PetEvent::class.java)?.let { event ->
                            eventListFromFirebase.add(event)
                        }
                    }

                    eventListFromFirebase.sortedBy { it.timestamp }

                }
            }
        }
    }
}