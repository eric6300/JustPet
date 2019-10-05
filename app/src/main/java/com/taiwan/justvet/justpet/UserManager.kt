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

    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String>
        get() = _userName

    private val _userEmail = MutableLiveData<String>()
    val userEmail: LiveData<String>
        get() = _userEmail

    fun getFirebaseUser(firebaseUser: FirebaseUser) {
        firebaseUser.apply {
            _userProfile.value = UserProfile(
                uid = this.uid,
                email = this.email,
                pets = emptyList(),
                displayName = this.displayName,
                photoUrl = this.photoUrl
            )
            _userName.value = this.displayName
            _userEmail.value = this.email
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

}