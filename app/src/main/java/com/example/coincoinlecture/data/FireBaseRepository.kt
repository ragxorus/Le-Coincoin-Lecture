package com.example.coincoinlecture.data

import com.example.coincoinlecture.model.Book
import com.example.coincoinlecture.model.Group
import com.example.coincoinlecture.model.Member
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.time.LocalDate
import java.util.UUID

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

    // ---------------------------------------------------------------- LIVRES

    private fun booksCollection() =
        db.collection("Users").document(userId()).collection("Books")

    //ajoute un nouveau livre dans la sous collections Books de l'utilisateur connecte
    suspend fun newBook(b : Book){
        val book = hashMapOf(
            "isbn" to b.isbn,
            "title" to b.title,
            "author" to b.author,
            "lender" to b.lender,
            "borrower" to b.borrower,
            "startDate" to b.startDate?.toString(),
            "maxDays" to b.maxDays
        )
        booksCollection().add(book).await()
    }

    suspend fun deleteBook(id: String) {
        booksCollection().document(id).delete().await()
    }

    //marque un livre comme prêté à un membre d'un groupe
    suspend fun lendBook(bookId: String, lender: String, borrower: String, maxDays: Int?) {
        val today = LocalDate.now()
        val data = hashMapOf<String, Any?>(
            "lender" to lender,
            "borrower" to borrower,
            "startDate" to today.toString(),
            "endDate" to maxDays?.let { today.plusDays(it.toLong()).toString() },
            "maxDays" to maxDays
        )
        booksCollection().document(bookId).update(data).await()
    }

    //le livre est rendu : on efface les infos de prêt
    suspend fun returnBook(bookId: String) {
        val data = hashMapOf<String, Any?>(
            "lender" to null,
            "borrower" to null,
            "startDate" to null,
            "endDate" to null,
            "maxDays" to null
        )
        booksCollection().document(bookId).update(data).await()
    }

    private fun DocumentSnapshot.toBook() = Book(
        id = id,
        isbn = getString("isbn") ?: "",
        title = getString("title") ?: "",
        author = getString("author") ?: "",
        lender = getString("lender"),
        borrower = getString("borrower"),
        startDate = getString("startDate")?.let(LocalDate::parse),
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

    suspend fun updateBookLoan(
        bookId: String,
        lender: String?,
        borrower: String?,
        startDate: LocalDate?,
        maxDays: Int?
    ) {
        val changes = hashMapOf<String, Any?>(
            "lender" to lender,
            "borrower" to borrower,
            "startDate" to startDate?.toString(),   // même format que newBook
            "maxDays" to maxDays
        )
        db.collection("Users").document(userId())
            .collection("Books").document(bookId)
            .update(changes)
            .await()
    }
    // --------------------------------------------------------------- GROUPES
    // Les groupes sont dans la collection racine "Groups" pour pouvoir être partagés.
    // Champs : name, ownerId, memberUids (comptes qui ont rejoint), members (liste de {id, name, uid}).

    private val groups = db.collection("Groups")

    private fun parseMembers(raw: Any?): List<Member> =
        (raw as? List<*>)?.mapNotNull { item ->
            val m = item as? Map<*, *> ?: return@mapNotNull null
            Member(
                id = m["id"] as? String ?: return@mapNotNull null,
                name = m["name"] as? String ?: "",
                uid = m["uid"] as? String
            )
        } ?: emptyList()

    private fun DocumentSnapshot.toGroup() = Group(
        id = id,
        name = getString("name") ?: "",
        ownerId = getString("ownerId") ?: "",
        members = parseMembers(get("members"))
    )

    suspend fun newGroup(name: String, memberNames: List<String>) {
        val members = memberNames.map { Member(id = UUID.randomUUID().toString(), name = it) }
        val data = hashMapOf(
            "name" to name,
            "ownerId" to userId(),
            "memberUids" to listOf(userId()),
            "members" to members.map { it.toMap() }
        )
        groups.add(data).await()
    }

    suspend fun deleteGroup(id: String) {
        groups.document(id).delete().await()
    }

    suspend fun addMember(groupId: String, name: String) {
        val member = Member(id = UUID.randomUUID().toString(), name = name)
        groups.document(groupId)
            .update("members", FieldValue.arrayUnion(member.toMap()))
            .await()
    }

    suspend fun removeMember(groupId: String, member: Member) {
        groups.document(groupId)
            .update("members", FieldValue.arrayRemove(member.toMap()))
            .await()
    }

    //charge un groupe à partir de son id (utilisé quand on ouvre un lien d'invitation)
    suspend fun getGroup(groupId: String): Group? {
        val snap = groups.document(groupId).get().await()
        return if (snap.exists()) snap.toGroup() else null
    }

    //"je suis cette personne" : lie le compte connecté à un membre du groupe
    suspend fun claimMember(groupId: String, memberId: String) {
        val uid = userId()
        val ref = groups.document(groupId)
        db.runTransaction { tx ->
            val snap = tx.get(ref)
            if (!snap.exists()) fail("Ce groupe n'existe plus")
            val members = parseMembers(snap.get("members"))
            if (members.any { it.uid == uid }) fail("Tu es déjà assigné à quelqu'un dans ce groupe")
            val target = members.firstOrNull { it.id == memberId } ?: fail("Cette personne n'existe plus")
            if (target.uid != null) fail("Quelqu'un s'est déjà assigné à ${target.name}")

            val updated = members.map { if (it.id == memberId) it.copy(uid = uid) else it }
            tx.update(
                ref,
                mapOf<String, Any>(
                    "members" to updated.map { it.toMap() },
                    "memberUids" to FieldValue.arrayUnion(uid)
                )
            )
            null
        }.await()
    }

    private fun fail(message: String): Nothing =
        throw FirebaseFirestoreException(message, FirebaseFirestoreException.Code.FAILED_PRECONDITION)

    //tous les groupes dont l'utilisateur fait partie
    fun groupsFlow(uid: String): Flow<List<Group>> = callbackFlow {
        val registration = groups
            .whereArrayContains("memberUids", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.documents?.map { it.toGroup() } ?: emptyList())
            }
        awaitClose { registration.remove() }
    }
}