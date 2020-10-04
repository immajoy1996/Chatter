package com.example.chatter.interfaces

interface DeckSelectedInterface {
    fun onDeckSelected(botTitle:String, isMultipleChoiceFrag:Boolean)
    fun onDeckUnselected(botTitle: String)
}