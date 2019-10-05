package com.taiwan.justvet.justpet

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.taiwan.justvet.justpet.data.Invitation
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

    private val _userHasPets = MutableLiveData<Boolean>()
    val userHasPets: LiveData<Boolean>
        get() = _userHasPets

    private val _navigateToHome = MutableLiveData<Boolean>()
    val navigateToHome: LiveData<Boolean>
        get() = _navigateToHome

    private val _inviteList = MutableLiveData<List<Invitation>>()
    val invitationList: LiveData<List<Invitation>>
        get() = _inviteList

    var checkedInvite = false

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

    fun userHasPets(status: Boolean) {
        _userHasPets.value = status
    }

    fun checkInvite() {
        UserManager.userProfile.value?.let { userProfile ->
            inviteReference
                .whereEqualTo("inviteeEmail", userProfile.email)
                .get()
                .addOnSuccessListener { it ->
                    if (it.isEmpty) {
                        Log.d(ERIC, "no invite")
                        if (checkedInvite) {
                            Toast.makeText(JustPetApplication.appContext, "目前沒有邀請", Toast.LENGTH_LONG).show()
                        }
                        checkedInvite = true
                    } else {
                        val list = mutableListOf<Invitation>()

                        it.documents.forEach {
                            list.add(
                                Invitation(
                                    inviteId = it.id,
                                    petId = it["petId"] as String?,
                                    petName = it["petFamily"] as String?,
                                    inviteeEmail = it["inviteeEmail"] as String?,
                                    inviterName = it["inviterName"] as String?,
                                    inviterEmail = it["inviterEmail"] as String?
                                )
                            )
                        }
                        _inviteList.value = list
//                        Toast.makeText(JustPetApplication.appContext, "你有${list.size}個邀請待確認", Toast.LENGTH_LONG).show()
                        Log.d(ERIC, "invite list : $list")
                    }
                }.addOnFailureListener {
                    Log.d(ERIC, "checkInvite() failed : $it")
                }
        }
    }

    fun showInvite(invitationList: MutableList<Invitation>) {
        if (invitationList.isNotEmpty()) {
            _inviteList.value = invitationList
        } else {
            _inviteList.value = null
            Log.d(ERIC, "invitationList is empty")
        }
    }

    fun confirmInvite(invitation: Invitation) {
        UserManager.userProfile.value?.let { userProfile ->
            usersReference.whereEqualTo("uid", userProfile.uid).get()
                .addOnSuccessListener {
                    updateUserProfile(invitation)
                }.addOnFailureListener {
                    Log.d(ERIC, "confirmInvite() failed : $it")
                }
        }
    }

    private fun updateUserProfile(invitation: Invitation) {
        UserManager.userProfile.value?.profileId?.let {
            usersReference.document(it)
                .update("pets", FieldValue.arrayUnion(invitation.petId))
                .addOnSuccessListener {
                    deleteInvite(invitation)
                }.addOnFailureListener {
                    Log.d(ERIC, "updateUserProfile() failed")
                }
        }
    }

    private fun deleteInvite(invitation: Invitation) {
        invitation.inviteId?.let {
            inviteReference.document(it).delete()
                .addOnSuccessListener {
                    updatePetProfileFamily(invitation)
                }.addOnFailureListener {
                    Log.d(ERIC, "deleteInvite() failed")
                }
        }
    }

    private fun updatePetProfileFamily(invitation: Invitation) {
        UserManager.userProfile.value?.let { userProfile ->
            invitation.petId?.let { petId ->
                petsReference.document(petId)
                    .update("family", FieldValue.arrayUnion(userProfile.email))
                    .addOnSuccessListener {
                        val newPetList = mutableListOf<String>()
                        userProfile.pets?.let { newPetList.addAll(it) }
                        newPetList.add(petId)

                        val newUserProfile = UserProfile(
                            profileId = userProfile.profileId,
                            uid = userProfile.uid,
                            email = userProfile.email,
                            pets = newPetList.sortedBy { it },
                            displayName = userProfile.displayName,
                            photoUrl = userProfile.photoUrl
                        )

                        UserManager.refreshUserProfile(newUserProfile)

                        Log.d(ERIC, "updatePetProfileFamily succeeded")
                    }.addOnFailureListener {
                        Log.d(ERIC, "updatePetProfileFamily failed : $it")
                    }
            }
        }
    }

}