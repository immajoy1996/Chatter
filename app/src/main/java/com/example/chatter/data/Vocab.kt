package com.example.chatter.data

data class Vocab(
    var expression: String,
    var definition: String,
    var image: String?,
    var flashcardType: String?,
    var isFavorite: Boolean = false,
    var whichBot: String? = null
)