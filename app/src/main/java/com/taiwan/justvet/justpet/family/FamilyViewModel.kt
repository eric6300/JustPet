package com.taiwan.justvet.justpet.family

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.taiwan.justvet.justpet.ERIC
import com.taiwan.justvet.justpet.UserManager
import com.taiwan.justvet.justpet.data.FamilyInvite

class FamilyViewModel : ViewModel() {

    val inviteeEmail = MutableLiveData<String>()

    private val _sendInviteCompleted = MutableLiveData<Boolean>()
    val sendInviteCompleted: LiveData<Boolean>
        get() = _sendInviteCompleted

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
                    FamilyInvite(
                        petId = "5DjrhdAlZka29LSmOe12",
                        petName = "MeiMei",
                        inviteeEmail = inviteeEmail,
                        inviterName = UserManager.userName.value,
                        inviterEmail = userProfile.email
                    )
                ).addOnSuccessListener {
                    Log.d(ERIC, "sendInvite() succeeded , ID : ${it.id}")
                    _sendInviteCompleted.value = true
                }.addOnFailureListener {
                    Log.d(ERIC, "sendInvite() failed : $it")
                }
            }
        }
    }

    fun sendInviteCompleted() {
        _sendInviteCompleted.value = false
    }

}