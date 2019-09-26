package com.taiwan.justvet.justpet.family

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.taiwan.justvet.justpet.ERIC
import com.taiwan.justvet.justpet.JustPetApplication
import com.taiwan.justvet.justpet.LoadApiStatus
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

    private val _loadStatus = MutableLiveData<LoadApiStatus>()
    val loadStatus: LiveData<LoadApiStatus>
        get() = _loadStatus

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String>
        get() = _errorMessage

    val petName = "${petProfile.name} 的家人"
    val inviteeEmail = MutableLiveData<String>()
    val ownerEmail = petProfile.ownerEmail
    val userEmail: String?
        get() = UserManager.userEmail.value

    val isOwner = ownerEmail.equals(userEmail)

    val firebase = FirebaseFirestore.getInstance()
    val usersReference = firebase.collection("users")
    val inviteReference = firebase.collection("invites")

    fun checkUser() {
        _errorMessage.value = ""
        _loadStatus.value = LoadApiStatus.LOADING
        UserManager.userProfile.value?.let {
            if (inviteeEmail.value == null || inviteeEmail.value == "") {
                _errorMessage.value = "用戶 E-mail 不可為空白"
                _loadStatus.value = LoadApiStatus.ERROR
            } else if (inviteeEmail.value == userEmail) {
                _errorMessage.value = "你已經是 ${petName}囉！"
                _loadStatus.value = LoadApiStatus.ERROR
            } else {
                inviteeEmail.value?.let { inviteeEmail ->
                    usersReference.whereEqualTo("email", inviteeEmail).get()
                        .addOnSuccessListener {
                            if (it.size() > 0) {
                                petProfile.family?.let {
                                    if (it.contains(inviteeEmail)) {
                                        _errorMessage.value = "該用戶已經是 $petName，無法再送出邀請"
                                        _loadStatus.value = LoadApiStatus.DONE
                                    } else {
                                        checkInvite()
                                    }
                                }
                            } else {
                                // show message
                                _errorMessage.value = "該用戶不存在，請重新輸入E-mail"
                                _loadStatus.value = LoadApiStatus.ERROR
                            }
                        }.addOnFailureListener {
                            _errorMessage.value = "發送失敗"
                            _loadStatus.value = LoadApiStatus.ERROR
                            Log.d(ERIC, "checkUser() failed : $it")
                        }
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
                        _errorMessage.value = "無法重複發送邀請"
                        _loadStatus.value = LoadApiStatus.ERROR
                        Log.d(ERIC, "already sent invite")
                    }
                }.addOnFailureListener {
                    _errorMessage.value = "發送失敗"
                    _loadStatus.value = LoadApiStatus.ERROR
                    Log.d(ERIC, "checkInvite() failed : $it")
                }
        }
    }

    private fun sendInvite() {
        UserManager.userProfile.value?.let { userProfile ->
            inviteeEmail.value?.let { inviteeEmail ->
                inviteReference.add(
                    Invite(
                        petId = petProfile.profileId,
                        petName = petProfile.name,
                        inviteeEmail = inviteeEmail,
                        inviterName = UserManager.userName.value,
                        inviterEmail = userProfile.email
                    )
                ).addOnSuccessListener {
                    _loadStatus.value = LoadApiStatus.DONE
                    Toast.makeText(JustPetApplication.appContext,"發送邀請成功！", Toast.LENGTH_LONG).show()
                    Log.d(ERIC, "sendInvite() succeeded , ID : ${it.id}")
                    leaveDialog()
                }.addOnFailureListener {
                    _errorMessage.value = "發送失敗"
                    _loadStatus.value = LoadApiStatus.ERROR
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