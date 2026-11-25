package com.example.coincoinlecture.ui.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.coincoinlecture.model.Book
import com.example.coincoinlecture.model.Group

// Choix de la personne à qui on prête le livre (parmi les membres de ses groupes).
@Composable
fun LendDialog(
    book: Book,
    groups: List<Group>,
    onConfirm: (borrower: String, maxDays: Int?) -> Unit,
    onDismiss: () -> Unit
) {
    val usableGroups = groups.filter { it.members.isNotEmpty() }
    // (id du groupe, nom du membre) : évite la confusion si deux groupes ont un "Pierre"
    var selected by remember { mutableStateOf<Pair<String, String>?>(null) }
    var days by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Prêter « ${book.title} »") },
        text = {
            if (usableGroups.isEmpty()) {
                Text("Crée d'abord un groupe avec des personnes dans l'onglet Groupe.")
            } else {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    usableGroups.forEach { group ->
                        Text(
                            group.name,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        group.members.map { it.name }.forEach { member ->
                            val key = group.id to member
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selected = key },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = selected == key, onClick = { selected = key })
                                Text(member)
                            }
                        }
                    }
                    OutlinedTextField(
                        value = days,
                        onValueChange = { days = it.filter(Char::isDigit) },
                        label = { Text("Durée max en jours (optionnel)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = selected != null,
                onClick = { selected?.let { onConfirm(it.second, days.toIntOrNull()) } }
            ) { Text("Prêter") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}