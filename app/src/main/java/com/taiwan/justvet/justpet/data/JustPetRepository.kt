package com.taiwan.justvet.justpet.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

object JustPetRepository {
    val firestoreInstance: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    val storageInstance: FirebaseStorage by lazy { FirebaseStorage.getInstance() }
}