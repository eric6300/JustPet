package com.taiwan.justvet.justpet.family

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.taiwan.justvet.justpet.*
import com.taiwan.justvet.justpet.util.LoadStatus
import com.taiwan.justvet.justpet.data.Invite
import com.taiwan.justvet.justpet.data.JustPetRepository
import com.taiwan.justvet.justpet.data.PetProfile
import com.taiwan.justvet.justpet.util.Util.getString

class FamilyViewModel(val petProfile: PetProfile) : ViewModel() {

    private val _expandStatus = MutableLiveData<Boolean>()
    val expandStatus: LiveData<Boolean>
        get() = _expandStatus

    private val _loadStatus = MutableLiveData<LoadStatus>()
    val loadStatus: LiveData<LoadStatus>
        get() = _loadStatus

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String>
        get() = _errorMessage

    private val _leaveFamilyDialog = MutableLiveData<Boolean>()
    val leaveFamilyDialog: LiveData<Boolean>
        get() = _leaveFamilyDialog

    val petFamily = getString(R.string.text_pet_family, petProfile.name)
    val userEmail = UserManager.userProfile.value?.email
    val inviteeEmail = MutableLiveData<String>()

    val usersReference = JustPetRepository.firestoreInstance.collection(USERS)
    val inviteReference = JustPetRepository.firestoreInstance.collection(INVITES)

    fun isOwner(): Boolean {
        return petProfile.ownerEmail.equals(userEmail)
    }

    fun checkUser() {
        _errorMessage.value = EMPTY_STRING
        _loadStatus.value = LoadStatus.LOADING

        when (inviteeEmail.value) {
            null, EMPTY_STRING -> {
                _errorMessage.value = getString(R.string.text_empty_email)
                _loadStatus.value = LoadStatus.ERROR
            }
            userEmail -> {
                _errorMessage.value = getString(R.string.text_user_already_pet_family, petProfile.name)
                _loadStatus.value = LoadStatus.ERROR
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
                                            petProfile.name
                                        )
                                        _loadStatus.value = LoadStatus.DONE
                                    } else {
                                        checkInvite()
                                    }
                                }
                            } else {
                                _errorMessage.value = getString(R.string.text_invitee_not_exist)
                                _loadStatus.value = LoadStatus.ERROR
                            }
                        }.addOnFailureListener {
                            _errorMessage.value = getString(R.string.text_invite_failure)
                            _loadStatus.value = LoadStatus.ERROR
                            Log.d(ERIC, "checkUser() failed : $it")
                        }
                }
            }
        }
    }

    private fun checkInvite() {
        _loadStatus.value = LoadStatus.LOADING

        inviteReference
            .whereEqualTo(INVITER_EMAIL, userEmail)
            .whereEqualTo(INVITEE_EMAIL, inviteeEmail.value)
            .get()
            .addOnSuccessListener {
                if (it.isEmpty) {
                    sendInvite()
                } else {
                    _errorMessage.value = getString(R.string.text_invite_already_exist)
                    _loadStatus.value = LoadStatus.ERROR
                    Log.d(ERIC, "already sent invite")
                }
            }.addOnFailureListener {
                _errorMessage.value = getString(R.string.text_invite_failure)
                _loadStatus.value = LoadStatus.ERROR
                Log.d(ERIC, "checkInvite() failed : $it")
            }
    }

    private fun sendInvite() {
        _loadStatus.value = LoadStatus.LOADING

        inviteeEmail.value?.let { inviteeEmail ->
            inviteReference.add(
                Invite(
                    petId = petProfile.profileId,
                    petName = petProfile.name,
                    inviteeEmail = inviteeEmail,
                    inviterName = UserManager.userProfile.value?.displayName,
                    inviterEmail = userEmail
                )
            ).addOnSuccessListener {
                _loadStatus.value = LoadStatus.DONE

                Toast.makeText(
                    JustPetApplication.appContext,
                    getString(R.string.text_invite_success),
                    Toast.LENGTH_LONG
                ).show()

                Log.d(ERIC, "sendInvite() succeeded , ID : ${it.id}")

                leaveFamilyDialog()

            }.addOnFailureListener {
                _errorMessage.value = getString(R.string.text_invite_failure)
                _loadStatus.value = LoadStatus.ERROR
                Log.d(ERIC, "sendInvite() failed : $it")
            }
        }

    }

    fun leaveFamilyDialog() {
        _leaveFamilyDialog.value = true
    }

    fun leaveFamilyDialogComplete() {
        _leaveFamilyDialog.value = false
    }

    fun expandDialog() {
        if (_expandStatus.value != true) {
            _expandStatus.value = true
        } else {
            _expandStatus.value = null
        }
    }

    companion object {
        const val FAMILY = "family"
        const val PET_FAMILY = "petFamily"
        const val INVITEE_EMAIL = "inviteeEmail"
        const val INVITER_NAME = "inviterName"
        const val INVITER_EMAIL = "inviterEmail"
    }

}