package com.example.chatter.data

data class QuizQuestion(
    val image: String?,
    val correct: String,
    val question: String,
    val type: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String?,
    val sentence: String?
)