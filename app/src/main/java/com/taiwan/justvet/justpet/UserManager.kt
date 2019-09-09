package com.taiwan.justvet.justpet

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseUser
import com.taiwan.justvet.justpet.data.UserProfile

object UserManager {

//    private const val USER_DATA = "user_data"
//    private const val USER_UID = "user_uid"

    private val _userProfile = MutableLiveData<UserProfile>()
    val userProfile: LiveData<UserProfile>
        get() = _userProfile

    private val _userProfileCompleted = MutableLiveData<Boolean>()
    val userProfileCompleted: LiveData<Boolean>
        get() = _userProfileCompleted

    private val _userProfileWithPetsCompleted = MutableLiveData<Boolean>()
    val userProfileWithPetsCompleted: LiveData<Boolean>
        get() = _userProfileWithPetsCompleted

    fun setupUserProfile(firebaseUser: FirebaseUser) {
        _userProfile.value = UserProfile(
            UID = firebaseUser.uid,
            email = firebaseUser.email,
            pets = null
        )
        _userProfileCompleted.value = true
    }

    fun userProfileCompleted() {
        _userProfileCompleted.value = null
    }

    fun setupUserProfileWithPets(userProfile: UserProfile) {
        _userProfile.value = userProfile
        _userProfileWithPetsCompleted.value = true
    }

    fun userProfileWithPetsCompleted() {
        _userProfileWithPetsCompleted.value = null
    }

    /**
     * Clear the [userProfile]/[_userProfile] data
     */
    fun clear() {
        _userProfile.value = null
    }

}