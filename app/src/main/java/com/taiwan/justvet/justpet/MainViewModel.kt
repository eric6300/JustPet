package com.taiwan.justvet.justpet

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FieldValue
import com.taiwan.justvet.justpet.data.Invite
import com.taiwan.justvet.justpet.data.JustPetRepository
import com.taiwan.justvet.justpet.data.UserProfile
import com.taiwan.justvet.justpet.event.EventViewModel.Companion.PET_ID
import com.taiwan.justvet.justpet.family.FamilyViewModel.Companion.FAMILY
import com.taiwan.justvet.justpet.family.FamilyViewModel.Companion.INVITEE_EMAIL
import com.taiwan.justvet.justpet.family.FamilyViewModel.Companion.INVITER_EMAIL
import com.taiwan.justvet.justpet.family.FamilyViewModel.Companion.INVITER_NAME
import com.taiwan.justvet.justpet.family.FamilyViewModel.Companion.PET_FAMILY
import com.taiwan.justvet.justpet.util.CurrentFragmentType
import com.taiwan.justvet.justpet.util.Util.getString

class MainViewModel(private val justPetRepository: com.taiwan.justvet.justpet.data.source.JustPetRepository) : ViewModel() {

    private val _invitationList = MutableLiveData<List<Invite>>()
    val invitationList: LiveData<List<Invite>>
        get() = _invitationList

    private var hasCheckedInvitation = false

    val currentFragmentType = MutableLiveData<CurrentFragmentType>()

    private val usersReference = JustPetRepository.firestoreInstance.collection(USERS)
    private val petsReference = JustPetRepository.firestoreInstance.collection(PETS)
    private val inviteReference = JustPetRepository.firestoreInstance.collection(INVITES)

    fun checkUserProfile(userProfile: UserProfile) {
        userProfile.uid?.let { uid ->
            usersReference.whereEqualTo(UID, uid).get()
                .addOnSuccessListener {
                    when (it.size()) {
                        0 -> registerUserProfile(userProfile)
                        else -> {
                            for (user in it) {
                                UserManager.refreshUserProfile(
                                    UserProfile(
                                        profileId = user.id,
                                        uid = user[UID] as String?,
                                        email = user[EMAIL] as String?,
                                        displayName = userProfile.displayName,
                                        pets = user[PETS] as List<String>?
                                    )
                                )
                            }
                            checkInvite()
                        }
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
                    checkUserProfile(userProfile)
                    Log.d(ERIC, "registerUserProfile() succeeded")
                }
                .addOnFailureListener {
                    Log.d(ERIC, "registerUserProfile() failed : $it")
                }
        }
    }

    fun checkInvite() {
        UserManager.userProfile.value?.let { userProfile ->
            inviteReference
                .whereEqualTo(INVITEE_EMAIL, userProfile.email)
                .get()
                .addOnSuccessListener { it ->
                    if (it.isEmpty) {
                        if (hasCheckedInvitation) {  //  not check invitation yet
                            Toast.makeText(
                                JustPetApplication.appContext,
                                getString(R.string.text_no_invitation_now),
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            hasCheckedInvitation = true
                        }
                    } else {
                        val list = mutableListOf<Invite>()

                        it.documents.forEach {
                            list.add(
                                Invite(
                                    inviteId = it.id,
                                    petId = it[PET_ID] as String?,
                                    petName = it[PET_FAMILY] as String?,
                                    inviteeEmail = it[INVITEE_EMAIL] as String?,
                                    inviterName = it[INVITER_NAME] as String?,
                                    inviterEmail = it[INVITER_EMAIL] as String?
                                )
                            )
                        }

                        _invitationList.value = list

                    }
                }.addOnFailureListener {
                    Log.d(ERIC, "checkInvite() failed : $it")
                }
        }
    }

    fun showInvite(invitationList: MutableList<Invite>) {
        if (invitationList.isEmpty()) {
            _invitationList.value = null
            Log.d(ERIC, "invitationList is empty")
        } else {
            _invitationList.value = invitationList
        }
    }

    fun confirmInvite(invitation: Invite) {
        UserManager.userProfile.value?.let { userProfile ->
            usersReference.whereEqualTo(UID, userProfile.uid).get()
                .addOnSuccessListener {
                    updateUserPets(invitation)
                }.addOnFailureListener {
                    Log.d(ERIC, "confirmInvite() failed : $it")
                }
        }
    }

    private fun updateUserPets(invitation: Invite) {
        UserManager.userProfile.value?.profileId?.let {
            usersReference.document(it)
                .update(PETS, FieldValue.arrayUnion(invitation.petId))
                .addOnSuccessListener {
                    deleteInvitation(invitation)
                }.addOnFailureListener {
                    Log.d(ERIC, "updateUserPets() failed")
                }
        }
    }

    fun deleteInvitation(invitation: Invite) {
        invitation.inviteId?.let {
            inviteReference.document(it).delete()
                .addOnSuccessListener {
                    updateFamilyOfPet(invitation)
                    Log.d(ERIC, "deleteInvitation() succeeded")
                }.addOnFailureListener {
                    Log.d(ERIC, "deleteInvitation() failed")
                }
        }
    }

    private fun updateFamilyOfPet(invitation: Invite) {
        UserManager.userProfile.value?.let { userProfile ->
            invitation.petId?.let { petId ->
                petsReference.document(petId)
                    .update(FAMILY, FieldValue.arrayUnion(userProfile.email))
                    .addOnSuccessListener {

                        refreshUserProfile(petId)

                        Log.d(ERIC, "updateFamilyOfPet() succeeded")
                    }.addOnFailureListener {
                        Log.d(ERIC, "updateFamilyOfPet() failed : $it")
                    }
            }
        }
    }

    private fun refreshUserProfile(petId: String) {
        UserManager.userProfile.value?.let { userProfile ->
            val newPetList = mutableListOf<String>()

            userProfile.pets?.let {
                newPetList.addAll(it)
            }

            newPetList.add(petId)

            UserManager.refreshUserProfile(
                UserProfile(
                    profileId = userProfile.profileId,
                    uid = userProfile.uid,
                    email = userProfile.email,
                    pets = newPetList.sortedBy { it },
                    displayName = userProfile.displayName,
                    photoUrl = userProfile.photoUrl
                )
            )
        }
    }
}