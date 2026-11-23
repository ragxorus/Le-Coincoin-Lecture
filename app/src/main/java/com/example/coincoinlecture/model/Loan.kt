package com.example.coincoinlecture.model

import java.time.LocalDate

data class Loan(
    val lender : String,
    val borrower : String,
    val startingDate : LocalDate,
    val endingDate : LocalDate?,
    val maxDays : Int,
    val book : Book
){
}
