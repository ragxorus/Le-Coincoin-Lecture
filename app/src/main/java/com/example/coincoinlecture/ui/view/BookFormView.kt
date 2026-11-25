package com.example.coincoinlecture.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.coincoinlecture.model.Book
import com.example.coincoinlecture.model.GoogleBookInfo
import kotlinx.coroutines.CancellationException

@Composable
fun BookFormView(
    newBook: (Book) -> Unit,
    onAdd: () -> Unit,
    searchBookInfo: suspend (String) -> GoogleBookInfo?,
    initialIsbn: String? = null
) {
    var isbn by rememberSaveable(initialIsbn) {
        mutableStateOf(initialIsbn.orEmpty())
    }

    var title by rememberSaveable(initialIsbn) {
        mutableStateOf("")
    }

    var author by rememberSaveable(initialIsbn) {
        mutableStateOf("")
    }

    var isSearching by rememberSaveable(initialIsbn) {
        mutableStateOf(false)
    }

    var lookupMessage by rememberSaveable(initialIsbn) {
        mutableStateOf<String?>(null)
    }

    // Recherche automatique lorsque le formulaire reçoit un ISBN scanné
    LaunchedEffect(initialIsbn) {
        val scannedIsbn = initialIsbn?.trim().orEmpty()

        if (scannedIsbn.isNotEmpty()) {
            isbn = scannedIsbn
            isSearching = true
            lookupMessage = null

            try {
                val info = searchBookInfo(scannedIsbn)

                if (info != null) {
                    info.title?.let { title = it }
                    info.author?.let { author = it }

                    if (info.title == null && info.author == null) {
                        lookupMessage = "Aucune information trouvée."
                    }
                } else {
                    lookupMessage =
                        "Livre introuvable. Tu peux remplir les champs manuellement."
                }

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                lookupMessage =
                    "Erreur lors de la recherche. Remplis les champs manuellement."
            } finally {
                isSearching = false
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        OutlinedTextField(
            value = isbn,
            onValueChange = { isbn = it },
            label = { Text("ISBN") },
            singleLine = true
        )

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Titre") }
        )

        OutlinedTextField(
            value = author,
            onValueChange = { author = it },
            label = { Text("Auteur") }
        )

        if (isSearching) {
            CircularProgressIndicator()
            Text("Recherche du livre...")
        }

        lookupMessage?.let {
            Text(it)
        }

        Button(
            enabled = !isSearching &&
                    isbn.isNotBlank() &&
                    title.isNotBlank() &&
                    author.isNotBlank(),

            onClick = {
                newBook(
                    Book(
                        "",
                        isbn.trim(),
                        title.trim(),
                        author.trim(),
                        null,
                        null,
                        null,
                        null
                    )
                )

                onAdd()
            }
        ) {
            Text("Valider")
        }
    }
}