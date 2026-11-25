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
import com.example.coincoinlecture.model.Member

// Choix de la personne à qui on prête le livre (parmi les membres de ses groupes, sauf soi-même).
@Composable
fun LendDialog(
    book: Book,
    groups: List<Group>,
    currentUid: String,
    onConfirm: (groupId: String, member: Member, maxDays: Int?) -> Unit,
    onDismiss: () -> Unit
) {
    // on retire l'utilisateur lui-même : on ne se prête pas un livre à soi-même
    val usableGroups = groups
        .map { g -> g.copy(members = g.members.filter { it.uid != currentUid }) }
        .filter { it.members.isNotEmpty() }

    // (id du groupe, membre choisi)
    var selected by remember { mutableStateOf<Pair<String, Member>?>(null) }
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
                        group.members.forEach { member ->
                            val isSelected =
                                selected?.first == group.id && selected?.second?.id == member.id
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selected = group.id to member },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { selected = group.id to member }
                                )
                                Text(member.name)
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
                onClick = {
                    selected?.let { (groupId, member) ->
                        onConfirm(groupId, member, days.toIntOrNull())
                    }
                }
            ) { Text("Prêter") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}