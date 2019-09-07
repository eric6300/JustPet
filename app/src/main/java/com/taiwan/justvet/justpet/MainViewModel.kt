package com.taiwan.justvet.justpet

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore

class MainViewModel : ViewModel() {

    val firebase = FirebaseFirestore.getInstance()
    val users = firebase.collection(PETS)

    fun checkUserProfile() {

    }

}