package com.example.coincoinlecture.model

import java.time.LocalDate

data class User(
    val name : String,
    val ownedBook : List<Book>
){}
