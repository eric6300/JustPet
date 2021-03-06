package com.taiwan.justvet.justpet.data.source.remote

import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.taiwan.justvet.justpet.*
import com.taiwan.justvet.justpet.UserManager.userProfile
import com.taiwan.justvet.justpet.data.JustPetRepository
import com.taiwan.justvet.justpet.data.PetEvent
import com.taiwan.justvet.justpet.data.PetProfile
import com.taiwan.justvet.justpet.data.UserProfile
import com.taiwan.justvet.justpet.data.source.JustPetDataSource
import com.taiwan.justvet.justpet.event.EventViewModel
import com.taiwan.justvet.justpet.event.EventViewModel.Companion.EVENT_TAGS_INDEX
import com.taiwan.justvet.justpet.event.EventViewModel.Companion.TIMESTAMP
import com.taiwan.justvet.justpet.ext.toPetProfile
import com.taiwan.justvet.justpet.home.HomeViewModel
import com.taiwan.justvet.justpet.util.LoadStatus
import com.taiwan.justvet.justpet.util.Util
import kotlinx.coroutines.tasks.await

object JustPetRemoteDataSource : JustPetDataSource {

    val firestoreInstance: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    val storageInstance: FirebaseStorage by lazy { FirebaseStorage.getInstance() }

    private val usersReference = firestoreInstance.collection(USERS)
    private val petsReference = firestoreInstance.collection(PETS)
    private val inviteReference = firestoreInstance.collection(INVITES)
    private val storageReference = storageInstance.reference

    override suspend fun addNewPetProfile(petProfile: PetProfile): String {
        val result = try {
            petsReference.add(petProfile).await()
        } catch (e: FirebaseFirestoreException) {
            Log.d(ERIC, "repository addNewPetProfile error: $e")
            null
        }

        //  return petId
        return when (result) {
            null -> EMPTY_STRING
            else -> result.id
        }
    }

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

                        Log.d(ERIC, "repository getPetProfiles error: $e")

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

        val result = try {
            petsReference.document(profileId).collection(EVENTS)
                .whereGreaterThan(TIMESTAMP, timestamp).get().await()
        } catch (e: FirebaseFirestoreException) {
            Log.d(ERIC, "repository getPetEvents error: $e")
            null
        }

        if (result == null) {

            return emptyList()

        } else {

            return when (result.size()) {

                0 -> emptyList()

                else -> {

                    val eventListFromFirebase = mutableListOf<PetEvent>()

                    for (item in result.documents) {

                        item.toObject(PetEvent::class.java)?.let { event ->

                            eventListFromFirebase.add(event)

                        }
                    }

                    eventListFromFirebase.sortedBy { it.timestamp }

                }
            }
        }
    }

    override suspend fun getSyndromeEvents(
        petId: String,
        tagIndex: Long,
        timestamp: Long
    ): List<PetEvent> {

        val result = try {
            petsReference.document(petId).collection(EVENTS)
                .whereArrayContains(EVENT_TAGS_INDEX, tagIndex)
                .whereGreaterThan(TIMESTAMP, timestamp).get().await()
        } catch (e: FirebaseFirestoreException) {
            Log.d(ERIC, "repository getSyndromeEvents error: $e")
            null
        }

        if (result == null) {

            return emptyList()

        } else {
            return when (result.size()) {

                0 -> emptyList()

                else -> {

                    val eventListFromFirebase = mutableListOf<PetEvent>()

                    for (item in result.documents) {

                        item.toObject(PetEvent::class.java)?.let { event ->

                            eventListFromFirebase.add(event)

                        }
                    }

                    eventListFromFirebase.sortedBy { it.timestamp }
                }
            }
        }
    }

    override suspend fun getWeightEvents(petId: String, timestamp: Long): List<PetEvent> {

        val result = try {

            petsReference.document(petId).collection(EVENTS)
                .whereGreaterThan(TIMESTAMP, timestamp).get().await()

        } catch (e: FirebaseFirestoreException) {

            Log.d(ERIC, "repository getWeightEvents error: $e")
            null

        }

        if (result == null) {

            return emptyList()

        } else {
            return when (result.size()) {

                0 -> emptyList()

                else -> {

                    val eventListFromFirebase = mutableListOf<PetEvent>()

                    for (item in result.documents) {

                        item.toObject(PetEvent::class.java)?.let { event ->
                            eventListFromFirebase.add(event)
                        }

                    }

                    eventListFromFirebase.filter {
                        (it.weight != null) && (it.weight > 0)
                    }
                }
            }
        }
    }

    override suspend fun updatePetProfile(petId: String, updateDataMap: Map<String, Any?>): LoadStatus {

        val result = try {
            petsReference.document(petId).update(updateDataMap).await()
        } catch (e: FirebaseFirestoreException) {
            false
        }

        return when (result) {
            false -> LoadStatus.FAILURE
            else -> LoadStatus.SUCCESS
        }
    }

    override suspend fun uploadPetProfileImage(petId: String, imageUri: String): String {

        val imageReference =
            storageReference.child(Util.getString(R.string.text_profile_path, petId))

        val result = try {

            imageReference.putFile(imageUri.toUri())
                .continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let {
                            Log.d(ERIC, "repository uploadPetProfileImage exception: $it")
                        }
                    }
                    return@Continuation imageReference.downloadUrl
                }).await()

        } catch (e: FirebaseFirestoreException) {
            Log.d(ERIC, "repository uploadPetProfileImage error: $e")
            null
        }

        //  return downloadUrl
        return result?.toString() ?: EMPTY_STRING
    }

    override suspend fun updatePetProfileImageUrl(petId: String, downloadUrl: String): LoadStatus {

        val result = try {
            petsReference.document(petId).update(HomeViewModel.IMAGE, downloadUrl).await()
        } catch (e: FirebaseFirestoreException) {
            Log.d(ERIC, "repository updatePetProfileImageUrl error: $e")
            false
        }

        return when (result) {
            false -> LoadStatus.FAILURE
            else -> LoadStatus.SUCCESS
        }
    }

    override suspend fun updatePetsOfUserProfile(userID: String, petId: String): LoadStatus {
        val result = try {
            usersReference.document(userID).update(PETS, FieldValue.arrayUnion(petId)).await()
        } catch (e: FirebaseFirestoreException) {
            Log.d(ERIC, "repository updatePetsOfUserProfile error: $e")
            false
        }

        return when (result) {
            false -> LoadStatus.FAILURE
            else -> LoadStatus.SUCCESS
        }
    }

    override suspend fun uploadPetEventImage(eventId: String, imageUri: String): String {

        val imageRef = storageReference.child(Util.getString(R.string.text_image_path, eventId))

        val result = try {

            imageRef.putFile(imageUri.toUri())
                .continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let {
                            throw it
                        }
                    }
                    return@Continuation imageRef.downloadUrl
                }).await()

        } catch (e: FirebaseFirestoreException) {
            Log.d(ERIC, "repository uploadPetEventImage error: $e")
            null
        }

        //  return downloadUrl
        return result?.toString() ?: EMPTY_STRING
    }

    override suspend fun updatePetEventImageUrl(eventId: String, downloadUrl: String): LoadStatus {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
