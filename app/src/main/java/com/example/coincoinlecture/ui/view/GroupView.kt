package com.example.coincoinlecture.ui.view

import android.content.Intent
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.coincoinlecture.model.Group
import com.example.coincoinlecture.model.GroupLoan
import com.example.coincoinlecture.model.Member
import com.example.coincoinlecture.ui.theme.Typography

@Composable
fun GroupView(
    groups: List<Group>,
    loans: List<GroupLoan>,
    currentUid: String,
    onCreateGroup: () -> Unit,
    onAddMember: (groupId: String, member: String) -> Unit,
    onRemoveMember: (groupId: String, member: Member) -> Unit,
    onDeleteGroup: (groupId: String) -> Unit,
    onClaim: (groupId: String, memberId: String) -> Unit,
    onJoinWithCode: (String) -> Unit
) {
    var showCodeDialog by remember { mutableStateOf(false) }

    if (showCodeDialog) {
        CodeDialog(
            onConfirm = { code ->
                showCodeDialog = false
                onJoinWithCode(code)
            },
            onDismiss = { showCodeDialog = false }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                OutlinedButton(onClick = { showCodeDialog = true }) {
                    Text("Rejoindre avec un code")
                }
            }
            if (groups.isEmpty()) {
                item { Text("Aucun groupe pour le moment. Appuie sur + pour en créer un.") }
            }
            items(groups, key = { it.id }) { group ->
                GroupCard(group, loans.filter { it.groupId == group.id }, currentUid, onAddMember, onRemoveMember, onDeleteGroup, onClaim)
            }
        }
        FloatingActionButton(
            onClick = onCreateGroup,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(40.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Créer un groupe")
        }
    }
}

@Composable
private fun GroupCard(
    group: Group,
    loans: List<GroupLoan>,
    currentUid: String,
    onAddMember: (String, String) -> Unit,
    onRemoveMember: (String, Member) -> Unit,
    onDeleteGroup: (String) -> Unit,
    onClaim: (String, String) -> Unit
) {
    val context = LocalContext.current
    var newMember by remember { mutableStateOf("") }
    val isOwner = group.ownerId == currentUid
    val alreadyAssigned = group.members.any { it.uid == currentUid }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(group.name, style = Typography.titleLarge, fontWeight = FontWeight.Bold)

            if (group.members.isEmpty()) {
                Text("Aucun membre")
            }
            group.members.forEach { member ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    when {
                        member.uid == currentUid -> Text("${member.name} (moi)", fontWeight = FontWeight.Bold)
                        member.uid != null -> Text("${member.name} ✓")
                        else -> Text(member.name)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (member.uid == null && !alreadyAssigned) {
                            TextButton(onClick = { onClaim(group.id, member.id) }) {
                                Text("C'est moi")
                            }
                        }
                        if (isOwner && member.uid == null) {
                            IconButton(onClick = { onRemoveMember(group.id, member) }) {
                                Icon(Icons.Default.Close, contentDescription = "Retirer ${member.name}")
                            }
                        }
                    }
                }
            }

            Text(
                "Livres prêtés",
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 8.dp)
            )
            if (loans.isEmpty()) {
                Text("Aucun livre prêté dans ce groupe")
            }
            loans.forEach { loan ->
                val since = loan.startDate?.let { " (depuis le $it)" } ?: ""
                Text("« ${loan.bookTitle} » : ${loan.ownerName} → ${loan.borrowerName}$since")
            }

            if (isOwner) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = newMember,
                        onValueChange = { newMember = it },
                        label = { Text("Nouvelle personne") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = {
                            onAddMember(group.id, newMember)
                            newMember = ""
                        },
                        enabled = newMember.isNotBlank()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Ajouter")
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = {
                    val text = "Rejoins mon groupe « ${group.name} » sur CoinCoin !\n" +
                            "coincoin://join/${group.id}\n\n" +
                            "Si le lien ne s'ouvre pas, entre ce code dans l'appli (Groupe → Rejoindre avec un code) :\n" +
                            group.id
                    val send = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, text)
                    }
                    context.startActivity(Intent.createChooser(send, "Partager le groupe"))
                }) { Text("Partager") }
                if (isOwner) {
                    TextButton(onClick = { onDeleteGroup(group.id) }) {
                        Text("Supprimer")
                    }
                }
            }
        }
    }
}

@Composable
private fun CodeDialog(onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var code by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rejoindre un groupe") },
        text = {
            OutlinedTextField(
                value = code,
                onValueChange = { code = it },
                label = { Text("Code du groupe") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(enabled = code.isNotBlank(), onClick = { onConfirm(code.trim()) }) {
                Text("Rejoindre")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler") } }
    )
}