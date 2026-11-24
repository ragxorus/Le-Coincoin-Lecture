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
import androidx.compose.ui.unit.dp
import com.example.coincoinlecture.model.Book

@Composable
fun BookView(
    books : List<Book>,
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
            items(books, key = { it.id }) { book ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Text(book.title)
                        Text(book.author)

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
                    Row(modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(100.dp),

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