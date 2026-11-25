package com.example.coincoinlecture.ui.view

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.example.coincoinlecture.model.GroupLoan

// Avertit l'emprunteur qu'on lui a prêté un ou plusieurs livres.
@Composable
fun NewLoanDialog(
    loans: List<GroupLoan>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (loans.size == 1) "Nouveau livre prêté" else "Nouveaux livres prêtés") },
        text = {
            Column {
                loans.forEach { loan ->
                    Text("${loan.ownerName} t'a prêté « ${loan.bookTitle} »")
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("OK") } }
    )
}
