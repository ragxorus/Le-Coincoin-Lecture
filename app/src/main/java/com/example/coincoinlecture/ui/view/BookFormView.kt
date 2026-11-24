package com.example.coincoinlecture.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.coincoinlecture.model.Book

//à changer quand on aura la traduction ISBN -> Book

@Composable
fun BookFormView(
    newBook: (Book) -> Unit,
    onAdd: () -> Unit
) {
    var isbn by remember { mutableStateOf("")}
    var title by remember{ mutableStateOf("")}
    var author by remember{ mutableStateOf("")}

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = isbn,
            onValueChange = { isbn = it },
            label = { Text("ISBN") }
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
        Button(onClick = {
            if(isbn != "" && title != "" && author != ""){
                newBook(Book("",isbn,title,author,null,null,null,null,null))
                onAdd()
            }
        }) {
            Text("Valider")
        }
    }
}