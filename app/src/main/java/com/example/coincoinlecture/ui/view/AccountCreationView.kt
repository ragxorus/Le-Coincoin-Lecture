package com.example.coincoinlecture.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
fun AccountCreationView(
    pseudoTaken : Boolean?,
    onPseudoChange: (String) -> Unit,
    newAccount : (String) -> Unit
){
    var newPseudo by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Card() {
            OutlinedTextField(
                value = newPseudo,
                onValueChange = {
                    newPseudo = it
                    onPseudoChange(it)
                                },
                label = { Text("Pseudo") },
                isError = pseudoTaken == true,
                supportingText = {
                    when (pseudoTaken) {
                        true -> Text("Ce pseudo est déjà pris")
                        false -> {}
                        null -> {}
                    }
                },
                singleLine = true
            )
        }
        Spacer(Modifier.width(8.dp))
        Button(
            onClick = { newAccount(newPseudo) },
            enabled = newPseudo.isNotBlank() && pseudoTaken != true
        ) {
            Text("Valider")
        }
    }
}