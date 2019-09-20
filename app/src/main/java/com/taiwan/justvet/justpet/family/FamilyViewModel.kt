package com.taiwan.justvet.justpet.family

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.taiwan.justvet.justpet.ERIC
import com.taiwan.justvet.justpet.UserManager
import com.taiwan.justvet.justpet.data.Invite
import com.taiwan.justvet.justpet.data.PetProfile
import com.taiwan.justvet.justpet.data.UserProfile

class FamilyViewModel(val petProfile: PetProfile) : ViewModel() {

    private val _leaveDialog = MutableLiveData<Boolean>()
    val leaveDialog: LiveData<Boolean>
        get() = _leaveDialog

    private val _expandStatus = MutableLiveData<Boolean>()
    val expandStatus: LiveData<Boolean>
        get() = _expandStatus

    val inviteeEmail = MutableLiveData<String>()
    val ownerEmail = petProfile.ownerEmail
    val userEmail: String?
        get() = UserManager.userEmail.value

    val isOwner = ownerEmail.equals(userEmail)

    val firebase = FirebaseFirestore.getInstance()
    val usersReference = firebase.collection("users")
    val inviteReference = firebase.collection("invite")

    fun checkUser() {
        UserManager.userProfile.value?.let {
            inviteeEmail.value?.let { inviteeEmail ->
                usersReference.whereEqualTo("email", inviteeEmail).get()
                    .addOnSuccessListener {
                        if (it.size() > 0) {
                            checkInvite()
                            Log.d(ERIC, "invitee user ID : ${it.documents[0].id}")
                        } else {
                            // show message
                            Log.d(ERIC, "can't find this user")
                        }
                    }.addOnFailureListener {
                        Log.d(ERIC, "checkUser() failed : $it")
                    }
            }
        }
    }

    private fun checkInvite() {
        UserManager.userProfile.value?.let { userProfile ->
            inviteReference
                .whereEqualTo("inviterEmail", userProfile.email)
                .whereEqualTo("inviteeEmail", inviteeEmail.value)
                .get()
                .addOnSuccessListener {
                    if (it.isEmpty) {
                        sendInvite()
                    } else {
                        Log.d(ERIC, "already sent invite")
                    }
                }.addOnFailureListener {
                    Log.d(ERIC, "checkInvite() failed : $it")
                }
        }
    }

    private fun sendInvite() {
        UserManager.userProfile.value?.let { userProfile ->
            inviteeEmail.value?.let { inviteeEmail ->
                Log.d(ERIC, "$userProfile")
                inviteReference.add(
                    Invite(
                        petId = petProfile.profileId,
                        petName = petProfile.name,
                        inviteeEmail = inviteeEmail,
                        inviterName = UserManager.userName.value,
                        inviterEmail = userProfile.email
                    )
                ).addOnSuccessListener {
                    Log.d(ERIC, "sendInvite() succeeded , ID : ${it.id}")
                    leaveDialog()
                }.addOnFailureListener {
                    Log.d(ERIC, "sendInvite() failed : $it")
                }
            }
        }
    }

    fun leaveDialog() {
        _leaveDialog.value = true
    }

    fun leaveDialogComplete() {
        _leaveDialog.value = false
    }

    fun expandDialog() {
        if (_expandStatus.value != true) {
            _expandStatus.value = true
        } else {
            _expandStatus.value = null
        }
    }

}