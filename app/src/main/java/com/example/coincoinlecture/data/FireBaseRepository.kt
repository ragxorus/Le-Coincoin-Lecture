package com.example.coincoinlecture.data

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class FireBaseRepository(
    private val auth: AuthRepository
) {
    private val db = Firebase.firestore

    fun userId(): String = auth.currentUserId ?: throw IllegalStateException("Not connected")

    fun userEmail(): String = auth.currentUserEmail ?: throw IllegalStateException("Not connected")

    suspend fun newUser(pseudo : String){
        val user = hashMapOf(
            "pseudo" to pseudo,
            "mail" to userEmail())
        db.collection("Users").document(userId())
            .set(user)
    }

    suspend fun userExist(): Boolean =
        db.collection("Users").document(userId())
            .get()
            .await()
            .exists()

}