package com.example.coincoinlecture.data

import com.example.coincoinlecture.model.Book
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.time.LocalDate

class FireBaseRepository(
    private val auth: AuthRepository
) {
    private val db = Firebase.firestore

    //renvoie l'id de l'utilisateur connecte
    fun userId(): String = auth.currentUserId ?: throw IllegalStateException("Not connected")

    //renvoie l'email de l'utilisateur connecte
    fun userEmail(): String = auth.currentUserEmail ?: throw IllegalStateException("Not connected")

    //ajoute un nouvelle utilisateur dans la collections Users de la DB
    suspend fun newUser(pseudo : String){
        val user = hashMapOf(
            "pseudo" to pseudo
        )
        db.collection("Users").document(userId())
            .set(user).await()
    }

    //vérifie si l'utilisateur connecte existe dans la DB
    suspend fun userExist(): Boolean =
        db.collection("Users").document(userId())
            .get()
            .await()
            .exists()

    suspend fun pseudoExists(pseudo: String): Boolean =
        !db.collection("Users")
            .whereEqualTo("pseudo", pseudo)
            .limit(1)
            .get()
            .await()
            .isEmpty

    suspend fun userPseudo(): String? =
        db.collection("Users").document(userId())
            .get()
            .await()
            .getString("pseudo")

    //ajoute un nouveau livre dans la sous collections Books de l'utilisateur connecte
    suspend fun newBook(b : Book){
        val book = hashMapOf(
            "isbn" to b.isbn,
            "title" to b.title,
            "author" to b.author,
            "lender" to b.lender,
            "borrower" to b.borrower,
            "startDate" to b.startDate?.toString(),
            "endDate" to b.endDate?.toString(),
            "maxDays" to b.maxDays
        )
        db.collection("Users").document(userId()).collection("Books")
            .add(book).await()
    }

    suspend fun deleteBook(id: String) {
        db.collection("Users").document(userId())
            .collection("Books").document(id)
            .delete()
            .await()
    }
    private fun DocumentSnapshot.toBook() = Book(
        id = id,
        isbn = getString("isbn") ?: "",
        title = getString("title") ?: "",
        author = getString("author") ?: "",
        lender = getString("lender"),
        borrower = getString("borrower"),
        startDate = getString("startDate")?.let(LocalDate::parse),
        endDate = getString("endDate")?.let(LocalDate::parse),
        maxDays = getLong("maxDays")?.toInt(),
    )

    fun booksFlow(uid: String): Flow<List<Book>> = callbackFlow {
        val registration = db.collection("Users").document(uid)
            .collection("Books")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.documents?.map { it.toBook() } ?: emptyList())
            }
        awaitClose { registration.remove() }
    }


}