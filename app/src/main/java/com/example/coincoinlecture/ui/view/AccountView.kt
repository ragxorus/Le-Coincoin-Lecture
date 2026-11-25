package com.example.coincoinlecture.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.coincoinlecture.viewmodel.AccountState
import com.example.coincoinlecture.ui.theme.Typography

@Composable
fun AccountView(
    accountState: AccountState,
    onDisconnect: () -> Unit
){
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            accountState.pseudo,
            style = Typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(
                horizontal = 16.dp,
                vertical = 8.dp
            )
        )
        Text(
            text = "Nombre de livres possédés : " + accountState.bookNumber.toString(),
            modifier = Modifier.align(Alignment.Start)
                .padding(
                    horizontal = 16.dp,
                    vertical = 8.dp
                )
        )
        Text(
            text = "Nombre de livres prêtés : " + accountState.lendedBookNumber.toString(),
            modifier = Modifier.align(Alignment.Start)
                .padding(
                    horizontal = 16.dp,
                    vertical = 8.dp
                )
        )
        Text(
            text = "Nombre de livres empruntés : " + accountState.borrowedBookNumber.toString(),
            modifier = Modifier.align(Alignment.Start)
                .padding(
                    horizontal = 16.dp,
                    vertical = 8.dp
                )
        )
        Button(
            onClick = onDisconnect,
            modifier = Modifier.padding(
                horizontal = 16.dp,
                vertical = 8.dp
            )
        ) {
            Text("Se déconnecter")
        }

    }
}