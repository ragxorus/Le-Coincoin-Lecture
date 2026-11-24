package com.example.coincoinlecture.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.coincoinlecture.data.AuthRepository
import com.example.coincoinlecture.data.FireBaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

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
        db.newUser(pseudo)
        _userExist.value = true
    }

    companion object {
        fun factory(auth: AuthRepository, db: FireBaseRepository) = viewModelFactory {
            initializer { FireBaseViewModel(auth, db) }
        }
    }
}