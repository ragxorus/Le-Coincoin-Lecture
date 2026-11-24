package com.example.coincoinlecture.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.coincoinlecture.data.AuthRepository
import com.example.coincoinlecture.data.FireBaseRepository
import com.example.coincoinlecture.model.Book
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

class FireBaseViewModel(
    private val auth: AuthRepository,
    private val db : FireBaseRepository
) : ViewModel() {
    private val _userExist = MutableStateFlow<Boolean?>(null)
    val userExist: StateFlow<Boolean?> = _userExist.asStateFlow()

    init {
        viewModelScope.launch {
            auth.userId.collectLatest { uid ->              //se relance à chaque changement de userId
                _userExist.value = null
                if (uid != null) {
                    _userExist.value = db.userExist()
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
        db.newUser(pseudo)
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

    fun newBook(b : Book) = viewModelScope.launch { db.newBook(b) }

    val books: StateFlow<List<Book>> = db.booksFlow()
        .catch { emit(emptyList()) }   // sinon une erreur Firestore fait planter l'app
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun deleteBook(id : String) = viewModelScope.launch {db.deleteBook(id)}



    companion object {
        fun factory(auth: AuthRepository, db: FireBaseRepository) = viewModelFactory {
            initializer { FireBaseViewModel(auth, db) }
        }
    }
}