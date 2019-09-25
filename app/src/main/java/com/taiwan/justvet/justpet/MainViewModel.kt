package com.taiwan.justvet.justpet

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.taiwan.justvet.justpet.data.Invite
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

    private val _inviteList = MutableLiveData<List<Invite>>()
    val inviteList: LiveData<List<Invite>>
        get() = _inviteList

    // Record current fragment to support data binding
    val currentFragmentType = MutableLiveData<CurrentFragmentType>()

    val firebase = FirebaseFirestore.getInstance()
    val usersReference = firebase.collection(USERS)
    val petsReference = firebase.collection(PETS)
    val inviteReference = firebase.collection("invites")

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
                        checkInvite()
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

    fun checkInvite() {
        UserManager.userProfile.value?.let { userProfile ->
            inviteReference
                .whereEqualTo("inviteeEmail", userProfile.email)
                .get()
                .addOnSuccessListener { it ->
                    if (it.isEmpty) {
                        Log.d(ERIC, "no invite")
                    } else {
                        val list = mutableListOf<Invite>()
                        it.documents.forEach {
                            list.add(
                                Invite(
                                    inviteId = it.id,
                                    petId = it["petId"] as String?,
                                    petName = it["petName"] as String?,
                                    inviteeEmail = it["inviteeEmail"] as String?,
                                    inviterName = it["inviterName"] as String?,
                                    inviterEmail = it["inviterEmail"] as String?
                                )
                            )
                        }
                        _inviteList.value = list
                        Log.d(ERIC, "invite list : $list")
                    }
                }.addOnFailureListener {
                    Log.d(ERIC, "checkInvite() failed : $it")
                }
        }
    }

    fun showInvite(inviteList: MutableList<Invite>) {
        if (inviteList.isNotEmpty()) {
            _inviteList.value = inviteList
        } else {
            _inviteList.value = null
            Log.d(ERIC, "inviteList is empty")
        }
    }

    fun confirmInvite(invite: Invite) {
        UserManager.userProfile.value?.let { userProfile ->
            usersReference.whereEqualTo("uid", userProfile.uid).get()
                .addOnSuccessListener {

                    val newPetList = mutableListOf<String>()
                    userProfile.pets?.let { newPetList.addAll(it) }
                    invite.petId?.let { newPetList.add(it) }

                    val newUserProfile = UserProfile(
                        profileId = userProfile.profileId,
                        uid = userProfile.uid,
                        email = userProfile.email,
                        pets = newPetList,
                        displayName = userProfile.displayName,
                        photoUrl = userProfile.photoUrl
                    )

                    updateUserProfile(invite)
                    updatePetProfileFamily(invite.petId)

                    UserManager.refreshUserProfile(newUserProfile)

                }.addOnFailureListener {
                    Log.d(ERIC, "confirmInvite() failed : $it")
                }
        }
    }

    private fun updatePetProfileFamily(petId: String?) {
        petId?.let {
            petsReference.document(it)
                .update("family", FieldValue.arrayUnion(UserManager.userProfile.value?.email))
                .addOnSuccessListener {
                    Log.d(ERIC, "updatePetProfileFamily succeeded")
                }.addOnFailureListener {
                    Log.d(ERIC, "updatePetProfileFamily failed : $it")
                }
        }
    }

    private fun updateUserProfile(invite: Invite) {
        UserManager.userProfile.value?.profileId?.let {
            usersReference.document(it)
                .update("pets", FieldValue.arrayUnion(invite.petId))
                .addOnSuccessListener {
                    deleteInvite(invite)
                }.addOnFailureListener {
                    Log.d(ERIC, "updateUserProfile() failed")
                }
        }
    }

    private fun deleteInvite(invite: Invite) {
        invite.inviteId?.let {
            inviteReference.document(it).delete()
                .addOnSuccessListener {
                    Log.d(ERIC, "deleteInvite() succeeded")
                }.addOnFailureListener {
                    Log.d(ERIC, "deleteInvite() failed")
                }
        }
    }
}