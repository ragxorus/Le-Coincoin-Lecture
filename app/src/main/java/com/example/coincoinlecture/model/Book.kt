package com.example.coincoinlecture.model

import java.time.LocalDate

data class Book(
    val id: String = "",
    val isbn : String,
    val title : String,
    val author : String,
    val lender : String? = null,
    val borrower : String? = null,
    val startDate: LocalDate? = null,
    val maxDays : Int? = null
){}
