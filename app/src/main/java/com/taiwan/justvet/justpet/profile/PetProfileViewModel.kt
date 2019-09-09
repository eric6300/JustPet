package com.taiwan.justvet.justpet.profile

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.taiwan.justvet.justpet.*
import com.taiwan.justvet.justpet.data.PetProfile

class PetProfileViewModel : ViewModel() {

    val petName = MutableLiveData<String>()

    val petSpecies = MutableLiveData<Long>()

    val petGender = MutableLiveData<Long>()

    val firebase = FirebaseFirestore.getInstance()
    val users = firebase.collection(USERS)
    val pets = firebase.collection(PETS)

    fun selectSpecies(species: Long) {
        petSpecies.value = species
        Log.d(TAG, "selected species : $species")
    }

    fun selectGender(gender: Long) {
        petGender.value = gender
        Log.d(TAG, "selected species : $gender")
    }

    fun newPetProfile() {
        UserManager.userProfile.value?.let { userProfile ->
            pets.add(
                PetProfile(
                    name = petName.value,
                    species = petSpecies.value,
                    gender = petGender.value,
                    owner = userProfile.profileId
                )
            )
                .addOnSuccessListener {
                    Log.d(TAG, "newPetProfile() succeeded")
                    updatePetsOfUser(it.id)
                }
                .addOnFailureListener {
                    Log.d(TAG, "newPetProfile() failed : $it")
                }
        }
    }

    fun updatePetsOfUser(petId: String) {
        UserManager.userProfile.value?.let { userProfile ->
            userProfile.profileId?.let { profileId ->
                users.document(profileId).update("pets", FieldValue.arrayUnion(petId))
                    .addOnSuccessListener {
                        Log.d(TAG, "updatePetsOfUser() succeeded")
                    }
                    .addOnFailureListener {
                        Log.d(TAG, "updatePetsOfUser() failed : $it")
                    }
            }
        }
    }

}