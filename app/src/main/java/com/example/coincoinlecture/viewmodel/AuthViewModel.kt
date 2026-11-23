package com.example.coincoinlecture.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.coincoinlecture.data.AuthRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    // uid de l'utilisateur connecté, ou null s'il n'y en a pas.
    val userId: StateFlow<String?> = repository.userId
        .stateIn(viewModelScope, SharingStarted.Eagerly, repository.currentUserId)

    fun signIn(activityContext: Context, onError: (String) -> Unit) =
        repository.signInWithGoogle(activityContext, onError)

    fun signOut() = repository.signOut()

    companion object {
        fun factory(repository: AuthRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer { AuthViewModel(repository) }
        }
    }
}