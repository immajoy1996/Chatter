package com.example.chatter

import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.bottom_nav_bar.*
import java.util.*

abstract class BaseChatActivity : AppCompatActivity(), RecognitionListener {

    var prevMsgId = ConstraintSet.PARENT_ID
    var newMsgId = -1
    var newSide = "left"
    var isFirst = true
    var msgCount = 0

    var timerTaskArray = arrayListOf<TimerTask>()
    var childEventListenerArray = arrayListOf<Pair<DatabaseReference, ChildEventListener>>()
    var valueEventListenerArray = arrayListOf<Pair<DatabaseReference, ValueEventListener>>()
    var shouldRestart = true

    private lateinit var speech: SpeechRecognizer
    private var isVocabFragment: Boolean = false
    private var isChatterActivity: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base_chat)
    }

    abstract fun setUpTopBar()

    abstract fun initializeMessagesContainer()

    abstract fun showFirstBotMessage()

    open fun handleNewMessageLogic(str: String) {
        msgCount++
        newMsgId = 10 * msgCount
        addMessage(str)
        if (newSide == "left") {
            newSide = "right"
        } else {
            newSide = "left"
        }
        if (isFirst) isFirst = false
        prevMsgId = newMsgId
    }

    abstract fun addMessage(msg: String)

    private fun restartActivity() {
        finish()
        overridePendingTransition(0, 0);
        startActivity(intent)
        overridePendingTransition(0, 0);
    }

    override fun onPause() {
        super.onPause()
        for (timerTask in timerTaskArray) {
            timerTask.cancel()
        }
        handleRestartFlag()
    }

    fun disposeListeners() {
        for (item in valueEventListenerArray) {
            val pathReference = item.first
            val listener = item.second
            pathReference.removeEventListener(listener)
        }
        for (item in childEventListenerArray) {
            val pathReference = item.first
            val listener = item.second
            pathReference.removeEventListener(listener)
        }
    }

    open fun handleRestartFlag() {
        if (shouldRestart) {
            restartActivity()
        }
    }

    override fun onBackPressed() {
        //deactivate back button
    }

    fun storeValueEventListener(
        databaseReference: DatabaseReference,
        listener: ValueEventListener
    ) {
        valueEventListenerArray.add(Pair(databaseReference, listener))
    }

    fun storeChildEventListener(
        databaseReference: DatabaseReference,
        listener: ChildEventListener
    ) {
        childEventListenerArray.add(Pair(databaseReference, listener))
    }

    val baseChildEventListener: ((DataSnapshot) -> Unit) -> ChildEventListener = { doit ->
        val childEventListener = object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, p1: String?) {
                doit(dataSnapshot)
            }

            override fun onChildRemoved(p0: DataSnapshot) {
                //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
                //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onCancelled(p0: DatabaseError) {
                //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        }
        childEventListener
    }

    val baseValueEventListener: ((DataSnapshot) -> Unit) -> ValueEventListener = { doit ->
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                doit(dataSnapshot)
            }

            override fun onCancelled(data: DatabaseError) {
                //TODO
            }

        }
        valueEventListener
    }

    fun disableNextButton() {
        button_next.setClickable(false)
    }

    fun enableNextButton() {
        button_next.setClickable(true)
    }

    fun toggleRestartFlag(flag: Boolean) {
        this.shouldRestart = flag
    }

    fun toggleIsVocabFragmentFlag(flag: Boolean) {
        this.isVocabFragment = flag
    }

    fun toggleIsChatterActivity(flag: Boolean) {
        this.isChatterActivity = flag
    }

    fun isVocabFragment(): Boolean {
        return isVocabFragment
    }

    fun isChatterActivity(): Boolean {
        return isChatterActivity
    }

    fun isMatch(inputString: String, targetString: String): Boolean {
        return (inputString.toLowerCase()).equals((targetString.toLowerCase()))
    }

    fun startListening() {
        speech = SpeechRecognizer.createSpeechRecognizer(this)
        speech.setRecognitionListener(this)
        val recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "es")
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, "es");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Please start speaking...");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getPackageName())
        recognizerIntent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH
        );
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        speech.startListening(recognizerIntent)
    }

    override fun onBeginningOfSpeech() {
    }

    override fun onReadyForSpeech(params: Bundle?) {
    }

    override fun onBufferReceived(buffer: ByteArray?) {
    }

    override fun onEndOfSpeech() {
    }

    override fun onError(error: Int) {
    }

    override fun onEvent(eventType: Int, params: Bundle?) {
    }

    override fun onPartialResults(partialResults: Bundle?) {
    }

    override fun onResults(results: Bundle?) {
    }

    override fun onRmsChanged(rmsdB: Float) {
    }
}
