package com.example.coincoinlecture.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.coincoinlecture.data.AuthRepository
import com.example.coincoinlecture.data.FireBaseRepository
import com.example.coincoinlecture.model.Book
import com.example.coincoinlecture.model.Group
import com.example.coincoinlecture.model.GroupLoan
import com.example.coincoinlecture.model.Member
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException
import java.time.LocalDate
import com.example.coincoinlecture.data.GoogleBooksRepository
import com.example.coincoinlecture.model.GoogleBookInfo
import com.example.coincoinlecture.BuildConfig
class FireBaseViewModel(
    private val auth: AuthRepository,
    private val db : FireBaseRepository
) : ViewModel() {
    private val _userExist = MutableStateFlow<Boolean?>(null)
    val userExist: StateFlow<Boolean?> = _userExist.asStateFlow()

    private val _pseudo = MutableStateFlow<String?>(null)

    init {
        viewModelScope.launch {
            auth.userId.collectLatest { uid ->              //se relance à chaque changement de userId
                _userExist.value = null
                _pseudo.value = null
                if (uid != null) {
                    val pseudo = db.userPseudo()
                    _pseudo.value = pseudo
                    _userExist.value = pseudo != null
                }
            }
        }
    }

    fun newUser(pseudo : String) = viewModelScope.launch {
        val p = pseudo.trim()
        if (db.pseudoExists(p)) {
            _pseudoTaken.value = true
            return@launch
        }
        db.newUser(p)
        _pseudo.value = p
        _userExist.value = true
    }

    private val _pseudoTaken = MutableStateFlow<Boolean?>(null)
    val pseudoTaken: StateFlow<Boolean?> = _pseudoTaken.asStateFlow()

    private var checkJob: Job? = null

    fun checkPseudo(pseudo: String) {
        checkJob?.cancel()
        _pseudoTaken.value = null
        if (pseudo.isBlank()) return
        checkJob = viewModelScope.launch {
            delay(400)
            _pseudoTaken.value = try {
                db.pseudoExists(pseudo.trim())
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                null
            }
        }
    }
    private val googleBooksRepository =
        GoogleBooksRepository(BuildConfig.GOOGLE_BOOKS_API_KEY)

    suspend fun searchBookInfo(isbn: String): GoogleBookInfo? {
        return googleBooksRepository.searchByIsbn(isbn)
    }
    // ---------------------------------------------------------------- LIVRES

    fun newBook(b : Book) = viewModelScope.launch { db.newBook(b) }

    @OptIn(ExperimentalCoroutinesApi::class)
    val books: StateFlow<List<Book>> = auth.userId
        .flatMapLatest { uid ->
            if (uid == null) {
                flowOf(emptyList())                       // déconnecté : liste vide, aucune écoute
            } else {
                db.booksFlow(uid).catch { emit(emptyList()) }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun updateBookLoan(
        bookId: String,
        lender: String?,
        borrower: String?,
        startDate: LocalDate?,
        maxDays: Int?
    ) = viewModelScope.launch {
        db.updateBookLoan(bookId, lender, borrower, startDate, maxDays)
    }

    fun deleteBook(id: String) = launchCatching {
        db.deleteBook(id)
        db.returnLoan(id)
    }

    // prête un livre à un membre d'un groupe : met à jour le livre ET crée le prêt partagé
    fun lendBook(book: Book, groupId: String, member: Member, maxDays: Int?) = launchCatching {
        val lender = _pseudo.value.orEmpty()
        db.lendBook(book.id, lender, member.name, maxDays)
        db.createLoan(book, groupId, lender, member, maxDays)
    }

    fun returnBook(bookId: String) = launchCatching {
        db.returnBook(bookId)
        db.returnLoan(bookId)
    }

    // --------------------------------------------------------------- GROUPES

    // lance une coroutine sans faire planter l'appli si Firestore renvoie une erreur
    private fun launchCatching(block: suspend () -> Unit) = viewModelScope.launch {
        try {
            block()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("FireBaseViewModel", "Erreur Firestore", e)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val groups: StateFlow<List<Group>> = auth.userId
        .flatMapLatest { uid ->
            if (uid == null) {
                flowOf(emptyList())
            } else {
                db.groupsFlow(uid).catch { emit(emptyList()) }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    // tous les prêts des groupes dont je fais partie
    @OptIn(ExperimentalCoroutinesApi::class)
    val groupLoans: StateFlow<List<GroupLoan>> = groups
        .map { list -> list.map { it.id }.sorted() }
        .distinctUntilChanged()
        .flatMapLatest { ids -> db.loansFlow(ids) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    // les livres qu'on m'a prêtés : prêts dont l'emprunteur est un membre lié à mon compte
    val borrowedLoans: StateFlow<List<GroupLoan>> =
        combine(groups, groupLoans, auth.userId) { groupList, allLoans, uid ->
            allLoans.filter { loan ->
                uid != null && groupList.firstOrNull { it.id == loan.groupId }
                    ?.members
                    ?.any { it.id == loan.borrowerMemberId && it.uid == uid } == true
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun newGroup(name: String, members: List<String>) = launchCatching {
        db.newGroup(name.trim(), members.map { it.trim() }.filter { it.isNotEmpty() }.distinct())
    }

    fun deleteGroup(id: String) = launchCatching { db.deleteGroup(id) }

    fun addMember(groupId: String, member: String) = launchCatching {
        val m = member.trim()
        if (m.isNotEmpty()) db.addMember(groupId, m)
    }

    fun removeMember(groupId: String, member: Member) = launchCatching {
        db.removeMember(groupId, member)
    }

    // ---- rejoindre un groupe via un lien / un code

    private val _join = MutableStateFlow(JoinState())
    val join: StateFlow<JoinState> = _join.asStateFlow()

    fun loadJoin(groupId: String) {
        viewModelScope.launch {
            _join.value = JoinState(loading = true)
            _join.value = try {
                val group = db.getGroup(groupId.trim())
                if (group == null) JoinState(loading = false, error = "Groupe introuvable")
                else JoinState(loading = false, group = group)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("FireBaseViewModel", "Chargement du groupe impossible", e)
                JoinState(loading = false, error = "Impossible de charger le groupe")
            }
        }
    }

    // "je suis cette personne" ; onDone reçoit null si tout s'est bien passé, sinon le message d'erreur
    fun claimMember(groupId: String, memberId: String, onDone: (String?) -> Unit) {
        viewModelScope.launch {
            val error = try {
                db.claimMember(groupId, memberId)
                null
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                e.message ?: "Erreur"
            }
            onDone(error)
        }
    }

    val accountState: StateFlow<AccountState> = combine(_pseudo, books) { pseudo, bookList ->
        AccountState(pseudo = pseudo.orEmpty(), books = bookList)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AccountState(pseudo = "", books = emptyList())
    )

    companion object {
        fun factory(auth: AuthRepository, db: FireBaseRepository) = viewModelFactory {
            initializer { FireBaseViewModel(auth, db) }
        }
    }
}