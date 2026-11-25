package com.example.coincoinlecture.ui.view

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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.coincoinlecture.model.Book
import com.example.coincoinlecture.model.Group
import com.example.coincoinlecture.model.GroupLoan
import com.example.coincoinlecture.model.Member
import com.example.coincoinlecture.ui.theme.Typography
import com.example.coincoinlecture.viewmodel.AccountState
import java.time.LocalDate

@Composable
fun BookView(
    accountState: AccountState,
    groups: List<Group>,
    currentUid: String,
    borrowedLoans: List<GroupLoan>,
    onAdd: () -> Unit,
    onDeleteBook: (String) -> Unit,
    // prêt à un membre d'un groupe (fenêtre de choix)
    onLend: (book: Book, groupId: String, member: Member, maxDays: Int?) -> Unit,
    // prêt à un utilisateur ayant un compte (écran BookLoanView)
    onLendToUser: (bookId: String) -> Unit,
    // supprime le prêt partagé quand on récupère le livre
    onReturn: (bookId: String) -> Unit,
    updateBookLoan: (String, String?, String?, LocalDate?, Int?) -> Unit
) {
    // livre pour lequel on est en train de choisir l'emprunteur (null = pas de dialogue)
    var bookToLend by remember { mutableStateOf<Book?>(null) }

    bookToLend?.let { book ->
        LendDialog(
            book = book,
            groups = groups,
            currentUid = currentUid,
            onConfirm = { groupId, member, maxDays ->
                onLend(book, groupId, member, maxDays)
                bookToLend = null
            },
            onDismiss = { bookToLend = null }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ---- livres qu'on m'a prêtés
            if (borrowedLoans.isNotEmpty()) {
                item {
                    Text("Livres empruntés", style = Typography.titleLarge, fontWeight = FontWeight.Bold)
                }
                items(borrowedLoans, key = { "loan-" + it.id }) { loan ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Text(loan.bookTitle, style = Typography.bodyLarge, fontWeight = FontWeight.Bold)
                            Text("Auteur : ${loan.bookAuthor}", style = Typography.labelLarge)
                            Text("Prêté par : ${loan.ownerName}", style = Typography.labelMedium)
                            loan.startDate?.let { Text("Depuis le : $it", style = Typography.labelMedium) }
                            loan.endDate?.let { Text("À rendre avant le : $it", style = Typography.labelMedium) }
                        }
                    }
                }
                item {
                    Text("Mes livres", style = Typography.titleLarge, fontWeight = FontWeight.Bold)
                }
            }

            // ---- mes livres
            items(accountState.books, key = { it.id }) { book ->
                val iLentIt = book.lender != null && book.lender == accountState.pseudo

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Text(
                            text = book.title,
                            style = Typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        Text(
                            text = "Auteur : " + book.author,
                            style = Typography.labelLarge
                        )
                        if (book.borrower != null && book.borrower != accountState.pseudo) {
                            Text("Emprunté par : ${book.borrower}", style = Typography.labelMedium)
                        }
                        if (book.lender != null && book.lender != accountState.pseudo) {
                            Text("Prêté par : ${book.lender}", style = Typography.labelMedium)
                        }
                        book.startDate?.let {
                            Text("Depuis le : $it", style = Typography.labelMedium)
                        }
                        book.maxDays?.let {
                            Text("Durée max : $it jours", style = Typography.labelMedium)
                        }
                    }
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (iLentIt) {
                            Button(onClick = {
                                updateBookLoan(book.id, null, null, null, null)
                                onReturn(book.id)
                            }) { Text("Récupérer") }
                        } else {
                            Button(onClick = { bookToLend = book }) { Text("Prêter") }
                            OutlinedButton(onClick = { onLendToUser(book.id) }) { Text("À un compte") }
                        }
                        IconButton(onClick = { onDeleteBook(book.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Supprimer")
                        }
                    }
                }
            }
        }
        FloatingActionButton(
            onClick = onAdd,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(40.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Ajouter")
        }
    }
}