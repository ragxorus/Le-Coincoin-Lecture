package com.example.coincoinlecture.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.maxLength
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.example.coincoinlecture.model.Book
import com.example.coincoinlecture.viewmodel.AccountState
import java.time.LocalDate

@Composable
fun BookLoanView(
    id: String,
    pseudoTaken : Boolean?,
    accountState: AccountState,
    updateBookLoan: (String, String?, String?, LocalDate?, Int?) -> Unit,
    onPseudoChange: (String) -> Unit,
    onValidate: () -> Unit
) {
    var pseudo by remember { mutableStateOf("") }
    var days = rememberTextFieldState()


    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = accountState.books.first {it.id == id}.title
        )
        OutlinedTextField(
            value = pseudo,
            onValueChange = {
                pseudo = it
                onPseudoChange(it)
                            },
            label = { Text("Destinataire") }
        )
        OutlinedTextField(
            state = days,
            label = { Text("Nombre de jours") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            inputTransformation = InputTransformation {
                if (!asCharSequence().all { it in '0'..'9' }) revertAllChanges()
            }
        )


        val maxDays: Int? = days.text.toString().toIntOrNull()

        Button(
            enabled = (maxDays != null && maxDays < 32 && pseudoTaken != null && pseudoTaken),
            onClick = {
                updateBookLoan(id,accountState.pseudo,pseudo,LocalDate.now(),maxDays)
                onValidate()
        }) {
            Text("Valider")
        }
    }
}