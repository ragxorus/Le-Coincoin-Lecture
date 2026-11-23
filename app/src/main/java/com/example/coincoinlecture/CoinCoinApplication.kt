package com.example.coincoinlecture

import android.app.Application
import com.example.coincoinlecture.data.AuthRepository
import com.example.coincoinlecture.data.FireBaseRepository

class CoinCoinApplication : Application() {
    val authRepository : AuthRepository by lazy {
        AuthRepository()
    }
    val dBaseRepository : FireBaseRepository by lazy {
        FireBaseRepository()
    }
}