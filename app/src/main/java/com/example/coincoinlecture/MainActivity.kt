package com.example.coincoinlecture

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.mutableStateOf
import com.example.coincoinlecture.ui.theme.CoinCoinLectureTheme
import com.example.coincoinlecture.navigation.CoinCoinNavigation

class MainActivity : ComponentActivity() {

    // id du groupe d'un lien d'invitation reçu (null = rien en attente)
    private val pendingGroupId = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as CoinCoinApplication

        val dbRepository = app.dBaseRepository
        val authRepository = app.authRepository

        pendingGroupId.value = groupIdFrom(intent)

        enableEdgeToEdge()
        setContent {
            CoinCoinLectureTheme {
                CoinCoinNavigation(
                    authRepository,
                    dbRepository,
                    pendingGroupId = pendingGroupId.value,
                    onJoinHandled = { pendingGroupId.value = null }
                )
            }
        }
    }

    // appelé quand on clique sur un lien alors que l'appli est déjà ouverte
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        groupIdFrom(intent)?.let { pendingGroupId.value = it }
    }

    // coincoin://join/<idDuGroupe>
    private fun groupIdFrom(intent: Intent?): String? =
        intent?.data
            ?.takeIf { it.scheme == "coincoin" && it.host == "join" }
            ?.lastPathSegment
}