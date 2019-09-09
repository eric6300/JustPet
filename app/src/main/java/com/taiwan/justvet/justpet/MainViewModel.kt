package com.taiwan.justvet.justpet

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.taiwan.justvet.justpet.data.UserProfile

class MainViewModel : ViewModel() {

    val firebase = FirebaseFirestore.getInstance()
    val users = firebase.collection(USERS)

    fun checkUserProfile(userProfile: UserProfile) {
        userProfile.UID?.let { uid ->
            users.whereEqualTo(UID, uid).get()
                .addOnSuccessListener {
                    if (it.size() == 0) {
                        registerUserProfile(userProfile)
                    } else {
                        Log.d(TAG, "user already registered")
                        for (item in it) {
                            addPetsDataToUserProfile(
                                UserProfile(
                                    profileId = item.id,
                                    UID = item[UID] as String?,
                                    email = item["email"] as String?
                                )
                            )
                        }
                    }
                }
                .addOnFailureListener {
                    Log.d(TAG, "checkUserProfile() failed : $it")
                }
        }
    }

    fun registerUserProfile(userProfile: UserProfile) {
        userProfile.UID?.let {
            users.add(userProfile)
                .addOnSuccessListener {
                    Log.d(TAG, "registerUserProfile() succeeded")
                }
                .addOnFailureListener {
                    Log.d(TAG, "registerUserProfile() failed : $it")
                }
        }
    }

    fun addPetsDataToUserProfile(userProfile: UserProfile) {
        userProfile.profileId?.let { profileId ->
            users.document(profileId).collection(PETS).get()
                .addOnSuccessListener { pets ->
                    if (pets.size() > 0) {
                        val petList = mutableListOf<String>()
                        for (item in pets) {
                            petList.add((item["petId"] as String))
                            Log.d(TAG, "document Id : ${item.id}" )
                        }
                        UserManager.setupUserProfileWithPets(userProfile, petList)
                    } else {
                        Log.d(TAG,"user doesn't have pets")
                    }
                }
                .addOnFailureListener {

                }
        }
    }
}