package com.example.chatter.data

data class MultipleChoiceQuestion(
    var questionTitle: String,
    var question: String?,
    var answer1: String,
    var answer2: String,
    var answer3: String,
    var correctAnswer: Int,
    var questionType: String,
    var image: String?
)
