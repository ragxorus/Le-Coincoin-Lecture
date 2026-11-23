package com.example.coincoinlecture.model

import java.time.LocalDate

data class User(
    val name : String,
    val ownedBook : List<Book>,
    val startingDate : LocalDate,
    val endingDate : LocalDate?,
    val maxDays : Int
){}
