package com.taiwan.justvet.justpet.chart

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.taiwan.justvet.justpet.PETS
import com.taiwan.justvet.justpet.TAG
import com.taiwan.justvet.justpet.UserManager
import com.taiwan.justvet.justpet.data.EventTag
import com.taiwan.justvet.justpet.data.PetProfile
import com.taiwan.justvet.justpet.data.UserProfile
import kotlinx.coroutines.launch

class ChartViewModel : ViewModel() {

    private val _listOfProfile = MutableLiveData<List<PetProfile>>()
    val listOfProfile: LiveData<List<PetProfile>>
        get() = _listOfProfile

    private val _selectedProfile = MutableLiveData<PetProfile>()
    val selectedProfile: LiveData<PetProfile>
        get() = _selectedProfile

    val petData = mutableListOf<PetProfile>()

    val database = FirebaseFirestore.getInstance()
    val pets = database.collection(PETS)

    init {
        UserManager.userProfile.value?.let {
            getPetProfileData(it)
        }
    }

    fun getProfileByPosition(position: Int) {
        _selectedProfile.value = petData[position]
    }

    fun getPetProfileData(userProfile: UserProfile) {
        userProfile.pets?.let {
            viewModelScope.launch {
                for (petId in it) {
                    pets.document(petId).get()
                        .addOnSuccessListener { document ->
                            val petProfile = PetProfile(
                                profileId = document.id,
                                name = document["name"] as String?,
                                species = document["species"] as Long?,
                                gender = document["gender"] as Long?,
                                neutered = document["neutered"] as Boolean?,
                                birthDay = document["birthDay"] as String?,
                                idNumber = document["idNumber"] as String?,
                                owner = document["owner"] as String?
                            )
                            petData.add(petProfile)
                            petData.sortBy { it.profileId }
                            _listOfProfile.value = petData
                            Log.d(TAG, "ChartViewModel getPetProfileData() succeeded")
                        }
                        .addOnFailureListener {
                            Log.d(TAG, "ChartViewModel getPetProfileData() failed : $it")
                        }
                }
            }
        }
    }
}