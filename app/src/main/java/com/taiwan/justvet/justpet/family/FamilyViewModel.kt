package com.taiwan.justvet.justpet.family

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.taiwan.justvet.justpet.*
import com.taiwan.justvet.justpet.UserManager.userProfile
import com.taiwan.justvet.justpet.util.LoadApiStatus
import com.taiwan.justvet.justpet.data.Invitation
import com.taiwan.justvet.justpet.data.PetProfile
import com.taiwan.justvet.justpet.util.Util
import com.taiwan.justvet.justpet.util.Util.getString

const val EMAIL = "email"
const val EMPTY_STRING = ""
const val INVITER_EMAIL = "inviterEmail"
const val INVITEE_EMAIL = "inviteeEmail"
class FamilyViewModel(val petProfile: PetProfile) : ViewModel() {

    private val _expandStatus = MutableLiveData<Boolean>()
    val expandStatus: LiveData<Boolean>
        get() = _expandStatus

    private val _loadStatus = MutableLiveData<LoadApiStatus>()
    val loadStatus: LiveData<LoadApiStatus>
        get() = _loadStatus

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String>
        get() = _errorMessage

    private val _leaveDialog = MutableLiveData<Boolean>()
    val leaveDialog: LiveData<Boolean>
        get() = _leaveDialog

    val petName = getString(R.string.text_pet_family, petProfile.name)
    val userEmail = UserManager.userEmail.value
    val inviteeEmail = MutableLiveData<String>()

    val firebase = FirebaseFirestore.getInstance()
    val usersReference = firebase.collection(USERS)
    val inviteReference = firebase.collection(INVITES)

    fun isOwner(): Boolean {
        return petProfile.ownerEmail.equals(userEmail)
    }

    fun checkUser() {
        _errorMessage.value = EMPTY_STRING
        _loadStatus.value = LoadApiStatus.LOADING

        when (inviteeEmail.value) {
            null, EMPTY_STRING -> {
                _errorMessage.value = getString(R.string.text_empty_email)
                _loadStatus.value = LoadApiStatus.ERROR
            }
            userEmail -> {
                _errorMessage.value = getString(R.string.text_user_already_pet_family, petName)
                _loadStatus.value = LoadApiStatus.ERROR
            }
            else -> {
                inviteeEmail.value?.let { inviteeEmail ->
                    usersReference.whereEqualTo(EMAIL, inviteeEmail).get()
                        .addOnSuccessListener {
                            if (it.size() > 0) {
                                petProfile.family?.let { familyList ->
                                    if (familyList.contains(inviteeEmail)) {
                                        _errorMessage.value = getString(
                                            R.string.text_invitee_already_pet_family,
                                            petName
                                        )
                                        _loadStatus.value = LoadApiStatus.DONE
                                    } else {
                                        checkInvite()
                                    }
                                }
                            } else {
                                _errorMessage.value = getString(R.string.text_invitee_not_exist)
                                _loadStatus.value = LoadApiStatus.ERROR
                            }
                        }.addOnFailureListener {
                            _errorMessage.value = getString(R.string.text_invite_failure)
                            _loadStatus.value = LoadApiStatus.ERROR
                            Log.d(ERIC, "checkUser() failed : $it")
                        }
                }
            }
        }
    }

    private fun checkInvite() {
        _loadStatus.value = LoadApiStatus.LOADING

        inviteReference
            .whereEqualTo(INVITER_EMAIL, userEmail)
            .whereEqualTo(INVITEE_EMAIL, inviteeEmail.value)
            .get()
            .addOnSuccessListener {
                if (it.isEmpty) {
                    sendInvite()
                } else {
                    _errorMessage.value = getString(R.string.text_invite_already_exist)
                    _loadStatus.value = LoadApiStatus.ERROR
                    Log.d(ERIC, "already sent invite")
                }
            }.addOnFailureListener {
                _errorMessage.value = getString(R.string.text_invite_failure)
                _loadStatus.value = LoadApiStatus.ERROR
                Log.d(ERIC, "checkInvite() failed : $it")
            }
    }

    private fun sendInvite() {
        _loadStatus.value = LoadApiStatus.LOADING

        inviteeEmail.value?.let { inviteeEmail ->
            inviteReference.add(
                Invitation(
                    petId = petProfile.profileId,
                    petName = petProfile.name,
                    inviteeEmail = inviteeEmail,
                    inviterName = UserManager.userName.value,
                    inviterEmail = userEmail
                )
            ).addOnSuccessListener {
                _loadStatus.value = LoadApiStatus.DONE

                Toast.makeText(
                    JustPetApplication.appContext,
                    getString(R.string.text_invite_success),
                    Toast.LENGTH_LONG
                ).show()

                Log.d(ERIC, "sendInvite() succeeded , ID : ${it.id}")

                leaveDialog()

            }.addOnFailureListener {
                _errorMessage.value = getString(R.string.text_invite_failure)
                _loadStatus.value = LoadApiStatus.ERROR
                Log.d(ERIC, "sendInvite() failed : $it")
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