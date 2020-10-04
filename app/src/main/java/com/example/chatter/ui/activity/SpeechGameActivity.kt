package com.example.chatter.ui.activity

import ProgressBarAnimation
import android.content.Context
import android.graphics.drawable.AnimationDrawable
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.chatter.R
import com.example.chatter.extra.MyBounceInterpolator
import com.example.chatter.ui.fragment.EasterEggFragment
import com.example.chatter.ui.fragment.SpeechGameCorrectFragment
import com.example.chatter.ui.fragment.SpeechGameIncorrectFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_speech_game.*
import java.util.*
import kotlin.collections.ArrayList

class SpeechGameActivity : BaseChatActivity(), TextToSpeech.OnInitListener {
    private var sentenceArray = arrayListOf<String>()
    private var currentIndex = 0
    private val correctAnswerFragment = SpeechGameCorrectFragment()
    private var wrongAnswerFragment = SpeechGameIncorrectFragment()
    private var mediaPlayer = MediaPlayer()
    private lateinit var audioManager: AudioManager
    var count = 0
    private var isStart = true
    var ncorrect = 0
    var gotItWrong = false
    private var wonGameFragment =
        EasterEggFragment.newInstance("Congrats! You've passed.", 100L)
    private var lostGameFragment =
        EasterEggFragment.newInstance("Better luck next time")
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_speech_game)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        setUpTopBar()
        fetchSentenceArray()
        setUpMicClick()
        setUpUserInput()
        setUpCheckButton()
        sayItForFirstTime()
    }

    private fun loadGameResultsPopup(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(speech_game_root_container.id, fragment)
            .addToBackStack(fragment.javaClass.name)
            .commit()
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(R.anim.slide_in, R.anim.slide_out)
            .replace(speech_game_correct_answer_root_container.id, fragment)
            .addToBackStack(fragment.javaClass.name)
            .commit()
    }

    fun startGame() {
        ncorrect = 0
        count = 0
        currentIndex = 0
        Collections.shuffle(sentenceArray)
        startNextSentence()
    }

    fun removeStartGameFragment() {
        supportFragmentManager.popBackStack()
    }

    private fun sayItForFirstTime() {
        startMicAnimation()
        setTimerTask("initializeMic", 1000, {
            sayCurrentSentence()
        })
    }

    private fun sayIt() {
        startMicAnimation()
        sayCurrentSentence()
    }

    private fun makeProgressBarVisible() {
        if (speech_game_user_input_progress_bar.visibility == View.GONE) {
            speech_game_user_input_progress_bar.visibility = View.VISIBLE
        }
    }

    private fun showProgressAnimation(from: Int, to: Int, progressBar: ProgressBar) {
        val anim = ProgressBarAnimation(progressBar, from.toFloat(), to.toFloat())
        anim.duration = 1000
        progressBar.startAnimation(anim)
    }

    private fun setUpUserInput() {
        speech_game_speaker_user_input.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (!isStart) {
                    makeProgressBarVisible()
                } else {
                    isStart = false
                }
                val text = speech_game_speaker_user_input.text.toString()
                if (text.isEmpty()) {
                    speech_game_user_input_progress_bar.setProgress(0)
                } else {
                    val progress = calculateMatchPercentage(
                        text,
                        sentenceArray[currentIndex]
                    )
                    val from = speech_game_user_input_progress_bar.progress
                    val to = (100 * progress).toInt()
                    showProgressAnimation(from, to, speech_game_user_input_progress_bar)
                }
            }

            override fun afterTextChanged(p0: Editable?) {
                //TODO("Not yet implemented")
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                //TODO("Not yet implemented")
            }
        })
    }

    fun updateTotalScore(pointsAdded: Long) {
        var oldScore: Int? = null
        var latestScore: Int? = null
        if (auth.currentUser != null) {
            auth.currentUser?.uid?.let {
                val userUid = it
                val pathRef = database.child(ChatterActivity.USERS).child(userUid).child("points")
                val pointsListener = baseValueEventListener { dataSnapshot ->
                    val currentScore = dataSnapshot.value as Long
                    oldScore = currentScore.toInt()
                    val newScore = currentScore + pointsAdded
                    latestScore = newScore.toInt()
                    database.child(ChatterActivity.USERS).child(userUid).child("points")
                        .setValue(newScore)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Points added", Toast.LENGTH_SHORT).show()
                        }
                }
                pathRef.addListenerForSingleValueEvent(pointsListener)
            }
        } else {
            //Guest mode
            val currentScore = preferences.getCurrentScore()
            oldScore = currentScore
            val newScore = currentScore + pointsAdded.toInt()
            latestScore = newScore
            preferences.storeCurrentScore(newScore)
            Toast.makeText(this, "Points added", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isGameWon(): Boolean {
        return (count == 5 && ncorrect >= 4)
    }

    private fun runGotItRightLogic() {
        if (!gotItWrong) {
            ncorrect++
        }
        gotItWrong = false
    }

    private fun runGotItWrongLogic() {
        gotItWrong = true
    }

    fun startNextSentence() {
        isStart = true
        speech_game_user_input_progress_bar.visibility = View.GONE
        speech_game_speaker_user_input.setText("")
        showCheckButton()
        currentIndex = (currentIndex + 1) % sentenceArray.size
        count++
        if (count <= TOTAL) {
            sayIt()
        } else {
            if (isGameWon()) {
                loadGameResultsPopup(wonGameFragment)
            } else {
                loadGameResultsPopup(lostGameFragment)
            }
        }
    }

    private fun sayCurrentSentence() {
        if (audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) == 0) {
            Toast.makeText(this, "Turn up your volume", Toast.LENGTH_LONG).show()
        }
        val size = sentenceArray.size
        if (size > 0) {
            letBearSpeak(sentenceArray[currentIndex])
        }
    }

    private fun calculateMatchPercentage(userInput: String, correctAnswer: String): Double {
        if (userInput.isEmpty()) return 0.0
        val list1 = userInput.split(" ", ".", ",", ":", ";", "!", "?", "\'")
        val list2 = correctAnswer.split(" ", ".", ",", ":", ";", "!", "?", "\'")
        val wordArray = arrayListOf<String>()
        val answerWordArray = arrayListOf<String>()
        for (word in list1) {
            if (word.isNotEmpty()) {
                wordArray.add(word)
            }
        }
        for (word in list2) {
            if (word.isNotEmpty()) {
                answerWordArray.add(word)
            }
        }
        return calculateCorrectnessFromWordArrays(wordArray, answerWordArray)
    }

    private fun setUpCheckButton() {
        speech_game_check_button.setOnDebouncedClickListener {
            val userInput = speech_game_speaker_user_input.text.toString()
            if (userInput.isEmpty()) {
                Toast.makeText(
                    this@SpeechGameActivity,
                    "Type something!",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnDebouncedClickListener
            }
            val correctAnswer = sentenceArray[currentIndex]
            if (calculateMatchPercentage(userInput, correctAnswer) > SENTENCE_MATCH_PERCENTAGE) {
                playCorrectAnswerSound()
                runGotItRightLogic()
                hideCheckButton()
                loadFragment(correctAnswerFragment)
            } else {
                playWrongAnswerSound()
                runGotItWrongLogic()
                hideCheckButton()
                wrongAnswerFragment =
                    SpeechGameIncorrectFragment.newInstance(sentenceArray[currentIndex])
                loadFragment(wrongAnswerFragment)
            }
        }
    }

    private fun showCheckButton() {
        speech_game_check_button.visibility = View.VISIBLE
    }

    private fun hideCheckButton() {
        speech_game_check_button.visibility = View.GONE
    }

    private fun playCorrectAnswerSound() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
            mediaPlayer.release()
        }
        mediaPlayer = MediaPlayer.create(this, R.raw.correct_answer)
        mediaPlayer.start()
    }

    private fun playWrongAnswerSound() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
            mediaPlayer.release()
        }
        mediaPlayer = MediaPlayer.create(this, R.raw.wrong_answer)
        mediaPlayer.start()
    }

    private fun calculateCorrectnessFromWordArrays(
        userWordsArray: ArrayList<String>,
        correctWordsArray: ArrayList<String>
    ): Double {
        var pos = 0
        var nmatches = 0
        for (word in correctWordsArray) {
            var found = false
            val start = pos
            while (pos < userWordsArray.size) {
                Log.d("Match", word.toLowerCase() + " " + userWordsArray[pos].toLowerCase())
                Log.d(
                    "MatchPercentage",
                    "" + isWordMatch(userWordsArray[pos].toLowerCase(), word.toLowerCase())
                )
                if (isWordMatch(userWordsArray[pos].toLowerCase(), word.toLowerCase())) {
                    nmatches++
                    pos++
                    found = true
                    break
                }
                pos++
            }
            if (!found) {
                pos = start
            }
        }
        return 1.0 * nmatches / correctWordsArray.size
    }

    private fun isWordMatch(str: String, target: String): Boolean {
        val total = target.length
        val used = arrayListOf<Char>()
        for (c in str) {
            used.add(c)
        }
        var count = 0
        for (c in target) {
            if (used.contains(c)) {
                used.remove(c)
                count++
            }
        }
        return 1.0 * count / total > WORD_MATCH_PERCENTAGE
    }

    private fun startMicAnimation() {
        speech_game_speaker_bot.setImageResource(R.drawable.microphone_listening)
        (speech_game_speaker_bot.drawable as AnimationDrawable).start()
        setTimerTask("SpeechGame", 3000, {
            stopMicAnimation()
        })
    }

    private fun stopMicAnimation() {
        speech_game_speaker_bot.setImageResource(R.drawable.microphone_listening)
    }

    private fun setUpMicClick() {
        speech_game_speaker_bot.setOnDebouncedClickListener {
            if (sentenceArray.size > 0) {
                startMicAnimation()
                sayCurrentSentence()
            } else {
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show()
            }
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

    companion object {
        private const val WORD_MATCH_PERCENTAGE = .8
        private const val SENTENCE_MATCH_PERCENTAGE = .9
        private const val TOTAL = 5
    }

}