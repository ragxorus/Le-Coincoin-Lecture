    package com.example.coincoinlecture.model

import java.time.LocalDate

// Un prêt visible par tous les membres d'un groupe (collection racine "Loans").
// L'id du document = l'id du livre prêté (un livre ne peut être prêté qu'à une personne à la fois).
data class GroupLoan(
    val id: String,
    val groupId: String,
    val ownerUid: String,
    val ownerName: String,
    val bookTitle: String,
    val bookAuthor: String,
    val borrowerMemberId: String,
    val borrowerName: String,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val maxDays: Int? = null
) {
    // sert à retenir qu'un prêt a déjà été annoncé à l'emprunteur
    val key: String get() = "$id|$startDate"
}
