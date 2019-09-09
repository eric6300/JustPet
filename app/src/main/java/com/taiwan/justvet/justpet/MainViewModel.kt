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
                            UserManager.setupUserProfileWithPets(
                                UserProfile(
                                    profileId = item.id,
                                    UID = item[UID] as String?,
                                    email = item["email"] as String?,
                                    pets = item["pets"] as List<String>?
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

}