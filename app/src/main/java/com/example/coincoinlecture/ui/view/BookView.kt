package com.example.coincoinlecture.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.coincoinlecture.model.Book
import com.example.coincoinlecture.ui.theme.Typography
import com.example.coincoinlecture.viewmodel.AccountState

@Composable
fun BookView(
    accountState: AccountState,
    onAdd: () -> Unit,
    onDeleteBook: (String) -> Unit,
    onLend: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(accountState.books, key = { it.id }) { book ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Text(
                            text = book.title,
                            style = Typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(
                                vertical = 8.dp
                            ))
                        Text(
                            text = "Auteur : " + book.author,
                            style = Typography.labelLarge,
//                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(
                                horizontal = 16.dp
                            )
                        )

                        book.borrower?.let {
                            Text("Emprunté par : $it")
                        }
                        book.lender?.let {
                            Text("Prêté par : $it")
                        }
                        book.maxDays?.let {
                            Text("Durée max : $it jours")
                        }
                    }
                    Row(modifier = Modifier.padding(
                        horizontal = 16.dp,
                        vertical = 8.dp
                    ).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),

                    ){
                        Button(
                            onClick = {
                                onLend()
                            }
                        ) {
                            Text("Prêter")
                        }
                        Button(
                            onClick = {
                                onDeleteBook(book.id)
                            }
                        ) {
                            Text("Supprimmer")
                        }
                    }
                }
            }
        }
        FloatingActionButton(
            onClick = onAdd,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(40.dp)
            ) {
            Icon(Icons.Default.Add, contentDescription = "Ajouter")
        }
    }
}