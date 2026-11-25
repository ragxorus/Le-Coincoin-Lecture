package com.example.coincoinlecture.viewmodel

import com.example.coincoinlecture.model.Book

data class AccountState(
    val pseudo : String,
    val books : List<Book>
){
    val bookNumber : Int get() = books.size
    val lendedBookNumber : Int get() = books.count {it.lender == pseudo}
    val borrowedBookNumber : Int get() = books.count {it.borrower == pseudo}
}
