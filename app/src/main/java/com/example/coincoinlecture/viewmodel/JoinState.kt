package com.example.coincoinlecture.viewmodel

import com.example.coincoinlecture.model.Group

// État de l'écran "Rejoindre un groupe"
data class JoinState(
    val loading: Boolean = true,
    val group: Group? = null,
    val error: String? = null
)
