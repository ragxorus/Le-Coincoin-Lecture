package com.example.coincoinlecture.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// Création d'un groupe : un nom + des personnes (sans compte), comme dans Tricount.
@Composable
fun GroupFormView(
    onCreate: (String, List<String>) -> Unit,
    onDone: () -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var memberInput by rememberSaveable { mutableStateOf("") }
    val members = remember { mutableStateListOf<String>() }

    fun addMember() {
        val m = memberInput.trim()
        if (m.isNotEmpty() && members.none { it.equals(m, ignoreCase = true) }) {
            members.add(m)
        }
        memberInput = ""
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nom du groupe") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = memberInput,
                onValueChange = { memberInput = it },
                label = { Text("Ajouter une personne") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = ::addMember, enabled = memberInput.isNotBlank()) {
                Icon(Icons.Default.Add, contentDescription = "Ajouter")
            }
        }

        Text("Membres (${members.size})")
        members.forEach { member ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(member)
                IconButton(onClick = { members.remove(member) }) {
                    Icon(Icons.Default.Close, contentDescription = "Retirer $member")
                }
            }
        }

        Button(
            onClick = {
                onCreate(name, members.toList())
                onDone()
            },
            enabled = name.isNotBlank()
        ) {
            Text("Créer le groupe")
        }
    }
}
