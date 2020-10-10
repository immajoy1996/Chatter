package com.example.chatter.data

data class BotVocab(
    var flashcardList: Set<Vocab> = HashSet<Vocab>(),
    var seenFlashcards: Set<Vocab> = HashSet<Vocab>()
)