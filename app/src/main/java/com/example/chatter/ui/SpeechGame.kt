package com.example.chatter.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.widget.EditText
import android.widget.Toast
import com.example.chatter.R
import com.example.chatter.ui.activity.BaseActivity
import com.example.chatter.ui.activity.BaseChatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_speech_game.*
import kotlinx.android.synthetic.main.fragment_enter_username.*
import java.util.*

class SpeechGameActivity : BaseChatActivity(), TextToSpeech.OnInitListener {
    private var sentenceArray = arrayListOf<String>()
    private var currentIndex = 0




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_speech_game)
        setUpTopBar()
        fetchSentenceArray()
        speech_game_speaker_user_input.performClick()
        setUpBotClick()
    }

    private fun sayCurrentSentence() {
        val size = sentenceArray.size
        if (size > 0) {
            letBearSpeak(sentenceArray[currentIndex])
        }
    }

    private fun setUpCheckButton() {
        speech_game_check_button.setOnClickListener {
        }
    }

    private fun setUpBotClick() {
        speech_game_speaker_bot.setOnDebouncedClickListener {
            sayCurrentSentence()

        }
    }

    private fun fetchSentenceArray() {
        sentenceArray = intent.getStringArrayListExtra("SentenceArray") ?: arrayListOf<String>()
        Collections.shuffle(sentenceArray)
    }


    override fun setUpTopBar() {
        speech_game_back_button.setOnClickListener {
            finish()
        }
    }

    override fun addMessage(msg: String) {
        //TODO("Not yet implemented")
    }

    override fun showFirstBotMessage() {
        //TODO("Not yet implemented")
    }

    override fun initializeMessagesContainer() {
        //TODO("Not yet implemented")
    }

}