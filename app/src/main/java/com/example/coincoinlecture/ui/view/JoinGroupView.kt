package com.example.coincoinlecture.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.coincoinlecture.ui.theme.Typography
import com.example.coincoinlecture.viewmodel.JoinState

// Écran affiché quand on ouvre un lien d'invitation : "Qui es-tu dans ce groupe ?"
@Composable
fun JoinGroupView(
    state: JoinState,
    currentUid: String,
    onClaim: (memberId: String) -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when {
            state.loading -> CircularProgressIndicator()

            state.group == null -> {
                Text(state.error ?: "Groupe introuvable")
                OutlinedButton(onClick = onCancel, modifier = Modifier.padding(top = 16.dp)) {
                    Text("Retour")
                }
            }

            else -> {
                val group = state.group
                val alreadyAssigned = group.members.any { it.uid == currentUid }

                Text(group.name, style = Typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(
                    if (alreadyAssigned) "Tu fais déjà partie de ce groupe."
                    else "Qui es-tu dans ce groupe ?",
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                group.members.forEach { member ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Button(
                            onClick = { onClaim(member.id) },
                            enabled = member.uid == null && !alreadyAssigned
                        ) {
                            Text(if (member.uid == null) member.name else "${member.name} (déjà pris)")
                        }
                    }
                }

                OutlinedButton(onClick = onCancel, modifier = Modifier.padding(top = 16.dp)) {
                    Text(if (alreadyAssigned) "Ouvrir mes groupes" else "Annuler")
                }
            }
        }
    }
}
