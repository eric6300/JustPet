package com.taiwan.justvet.justpet

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.taiwan.justvet.justpet.data.UserProfile
import com.taiwan.justvet.justpet.util.CurrentFragmentType

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

    private val _navigateToHome = MutableLiveData<Boolean>()
    val navigateToHome: LiveData<Boolean>
        get() = _navigateToHome

    // Record current fragment to support data binding
    val currentFragmentType = MutableLiveData<CurrentFragmentType>()

    val firebase = FirebaseFirestore.getInstance()
    val usersReference = firebase.collection(USERS)

    fun checkUserProfile(userProfile: UserProfile) {
        userProfile.uid?.let { uid ->
            usersReference.whereEqualTo(UID, uid).get()
                .addOnSuccessListener {
                    if (it.size() == 0) {
                        registerUserProfile(userProfile)
                    } else {
                        Log.d(ERIC, "user already registered")
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
                    Log.d(ERIC, "checkUserProfile() failed : $it")
                }
        }
    }

    private fun registerUserProfile(userProfile: UserProfile) {
        userProfile.uid?.let {
            usersReference.add(userProfile)
                .addOnSuccessListener {
                    Log.d(ERIC, "registerUserProfile() succeeded")
                    checkUserProfile(userProfile)
                }
                .addOnFailureListener {
                    Log.d(ERIC, "registerUserProfile() failed : $it")
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