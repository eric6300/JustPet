package com.taiwan.justvet.justpet

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.taiwan.justvet.justpet.data.UserProfile

class MainViewModel : ViewModel() {

    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String>
        get() = _userName

    private val _userEmail = MutableLiveData<String>()
    val userEmail: LiveData<String>
        get() = _userEmail

    private val _userPhotoUrl = MutableLiveData<Uri>()
    val userPhotoUrl: LiveData<Uri>
        get() = _userPhotoUrl

    val firebase = FirebaseFirestore.getInstance()
    val users = firebase.collection(USERS)

    fun checkUserProfile(userProfile: UserProfile) {
        userProfile.uid?.let { uid ->
            users.whereEqualTo(UID, uid).get()
                .addOnSuccessListener {
                    if (it.size() == 0) {
                        registerUserProfile(userProfile)
                    } else {
                        Log.d(TAG, "user already registered")
                        for (item in it) {
                            UserManager.refreshUserProfile(
                                UserProfile(
                                    profileId = item.id,
                                    uid = item[UID] as String?,
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
        userProfile.uid?.let {
            users.add(userProfile)
                .addOnSuccessListener {
                    Log.d(TAG, "registerUserProfile() succeeded")
                }
                .addOnFailureListener {
                    Log.d(TAG, "registerUserProfile() failed : $it")
                }
        }
    }

    fun setupDrawerUser(userProfile: UserProfile) {
        userProfile.let {
            _userName.value = it.displayName
            _userPhotoUrl.value = it.photoUrl
            _userEmail.value = it.email
        }
    }
}