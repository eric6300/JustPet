package com.taiwan.justvet.justpet

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseUser
import com.taiwan.justvet.justpet.data.UserProfile

object UserManager {

    private const val USER_DATA = "user_data"
    private const val USER_UID = "user_uid"

    private val _userProfile = MutableLiveData<UserProfile>()

    val userProfile: LiveData<UserProfile>
        get() = _userProfile

    var userUID: String? = null
        get() = JustPetApplication.appContext
            .getSharedPreferences(USER_DATA, Context.MODE_PRIVATE)
            .getString(USER_UID, null)
        set(value) {
            field = when (value) {
                null -> {
                    JustPetApplication.appContext
                        .getSharedPreferences(USER_DATA, Context.MODE_PRIVATE).edit()
                        .remove(USER_UID)
                        .apply()
                    null
                }
                else -> {
                    JustPetApplication.appContext
                        .getSharedPreferences(USER_DATA, Context.MODE_PRIVATE).edit()
                        .putString(USER_UID, value)
                        .apply()
                    value
                }
            }
        }

    fun setupUserProfile(firebaseUser: FirebaseUser) {
        _userProfile.value = UserProfile(
            UID = firebaseUser.uid,
            email = firebaseUser.email
        )
    }

    /**
     * It can be use to check login status directly
     */
    val isLoggedIn: Boolean
        get() = userUID != null

    /**
     * Clear the [userUID] and the [userProfile]/[_userProfile] data
     */
    fun clear() {
        userUID = null
        _userProfile.value = null
    }

}