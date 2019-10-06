package com.taiwan.justvet.justpet

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseUser
import com.taiwan.justvet.justpet.data.UserProfile

object UserManager {
    private val _userProfile = MutableLiveData<UserProfile>()
    val userProfile: LiveData<UserProfile>
        get() = _userProfile

    private val _getFirebaseUserCompleted = MutableLiveData<Boolean>()
    val getFirebaseUserCompleted: LiveData<Boolean>
        get() = _getFirebaseUserCompleted

    private val _refreshUserProfileCompleted = MutableLiveData<Boolean>()
    val refreshUserProfileCompleted: LiveData<Boolean>
        get() = _refreshUserProfileCompleted

    fun getFirebaseUser(firebaseUser: FirebaseUser) {
        firebaseUser.apply {
            _userProfile.value = UserProfile(
                uid = this.uid,
                email = this.email,
                pets = emptyList(),
                displayName = this.displayName,
                photoUrl = this.photoUrl
            )
        }
        _getFirebaseUserCompleted.value = true
    }

    fun userProfileCompleted() {
        _getFirebaseUserCompleted.value = null
    }

    fun refreshUserProfile(userProfile: UserProfile) {
        _userProfile.value = userProfile
        _refreshUserProfileCompleted.value = true
    }

    fun refreshUserProfileCompleted() {
        _refreshUserProfileCompleted.value = null
    }

    fun userHasPets(): Boolean {
        userProfile.value?.let {
            return when (it.pets?.size) {
                0, null -> false
                else -> true
            }
        }
        return false
    }

}