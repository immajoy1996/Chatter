package com.example.chatter.ui.activity

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet
import com.example.chatter.extra.Preferences
import com.example.chatter.R
import com.example.chatter.ui.fragment.BaseFragment
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.translate.Translate
import com.google.cloud.translate.TranslateOptions
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.bottom_nav_bar.*
import java.io.IOException
import java.util.*
import kotlin.concurrent.schedule

abstract class BaseChatActivity : AppCompatActivity(), RecognitionListener,
    TextToSpeech.OnInitListener {

    var prevMsgId = ConstraintSet.PARENT_ID
    var newMsgId = -1
    var newSide = "left"
    var isFirst = true
    var msgCount = 0

    var childEventListenerArray = arrayListOf<Pair<DatabaseReference, ChildEventListener>>()
    var valueEventListenerArray = arrayListOf<Pair<DatabaseReference, ValueEventListener>>()
    var shouldRestart = true

    private var speech: SpeechRecognizer? = null
    private var isVocabFragment: Boolean = false
    private var isChatterActivity: Boolean = false
    var timerTaskArray = arrayListOf<TimerTask>()
    private lateinit var timerTask: TimerTask

    private var isMicActive = false
    private var textToSpeech: TextToSpeech? = null
    private var textToSpeechInitialized = false

    lateinit var preferences: Preferences
    private var targetBearBotSpeakerName = "en-us-x-sfg-local"
    private var targetMaleAmericanSpeaker = "en-us-x-sfg#male_2-local"
    private var targetFemaleAmericanSpeaker = "en-us-x-sfg#female_2-local"

    private lateinit var firebaseStorage: FirebaseStorage

    var translateService: Translate? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base_chat)
        firebaseStorage = FirebaseStorage.getInstance()
        setUpTranslateService()
        preferences = Preferences(this)
        setUpTextToSpeech()
    }

    fun canConnectToInternet(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        if (connectivityManager != null) {
            val capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
                    return true
                }
            }
        }
        return false
    }

    fun initializeVariables() {
        prevMsgId = ConstraintSet.PARENT_ID
        newMsgId = -1
        newSide = "left"
        isFirst = true
        msgCount = 0
    }

    fun textToSpeechInitialized(): Boolean {
        return textToSpeechInitialized
    }

    abstract fun setUpTopBar()

    abstract fun initializeMessagesContainer()

    abstract fun showFirstBotMessage()

    fun getMessageCount(): Int {
        return msgCount
    }

    private fun setUpTranslateService() {
        firebaseStorage.getReferenceFromUrl(TRANSLATE_API_URL).stream.addOnSuccessListener { downloadTask ->
            Thread(Runnable {
                val inputStream = downloadTask.stream
                inputStream.use {
                    val myCredentials = GoogleCredentials.fromStream(it)
                    val translateOptions =
                        TranslateOptions.newBuilder().setCredentials(myCredentials).build()
                    translateService = translateOptions.service
                }
            }
            ).start()
        }.addOnFailureListener {
            Toast.makeText(this, "Translate api failed", Toast.LENGTH_SHORT).show()
        }
    }

    fun translate(text: String, targetLanguage: String): String? {
        val translation = translateService?.translate(
            text,
            Translate.TranslateOption.targetLanguage(targetLanguage),
            Translate.TranslateOption.model("base")
        )
        return translation?.translatedText
    }

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

    fun undoMessageVariables() {
        msgCount--
        newMsgId = 10 * msgCount
        if (newSide == "left") {
            newSide = "right"
        } else {
            newSide = "left"
        }
        isFirst = false
        prevMsgId = newMsgId
        //if (msgCount == 1) prevMsgId = -1
    }

    abstract fun addMessage(msg: String)

    private fun setUpTextToSpeech() {
        textToSpeech = TextToSpeech(this, this)
        Toast.makeText(this, "Initializing audio...", Toast.LENGTH_SHORT).show()
        //runThroughVoiceList()
    }

    fun sayBearsNextQuote() {
        preferences.getCurrentQuote().let {
            letBearSpeak(it)
        }
    }

    fun getCurrentJokeAnswer(): String {
        return preferences?.getCurrentJokeAnswer() ?: "Oops, something went wrong"
    }

    fun getNextJoke(): String {
        return preferences.getNextJoke()
    }

    fun runThroughVoiceList() {
        textToSpeech?.voices?.let {
            for (speaker in it) {
                Log.d("Voice instance", speaker.name.toString())
            }
        }
    }

    fun getMyPreferences(): Preferences? {
        return preferences
    }

    private fun restartActivity() {
        finish()
        overridePendingTransition(0, 0);
        startActivity(intent)
        overridePendingTransition(0, 0);
    }

    fun readMessageBubble(text: String) {
        if (textToSpeechInitialized) {
            textToSpeech?.voices?.let {
                for (speaker in it) {
                    Log.d("speakers ", speaker.name)
                    if (speaker.name == targetMaleAmericanSpeaker) {
                        textToSpeech?.setVoice(speaker)
                    }
                }
            }
            if (textToSpeech?.isSpeaking == true) {
                textToSpeech?.stop()
            }
            textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
        } else {
            Toast.makeText(this, "An error has occurred", Toast.LENGTH_SHORT)
                .show()
        }
    }

    fun letBearSpeak(text: String) {
        if (textToSpeechInitialized) {
            textToSpeech?.voices?.let {
                for (speaker in it) {
                    Log.d("speakers ", speaker.name)
                    if (speaker.name == targetBearBotSpeakerName) {
                        textToSpeech?.setVoice(speaker)
                    }
                }
            }
            if (textToSpeech?.isSpeaking == true) {
                textToSpeech?.stop()
            }
            textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
        } else {
            Toast.makeText(this, "An error has occurred", Toast.LENGTH_SHORT)
                .show()
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            // set US English as language for tts
            val result = textToSpeech?.setLanguage(Locale.ENGLISH)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                textToSpeechInitialized = false
                Toast.makeText(this, "Text to speech Language not supported", Toast.LENGTH_SHORT)
                    .show()
            } else {
                textToSpeechInitialized = true
            }
        } else {
            textToSpeechInitialized = false
            Toast.makeText(this, "Text to speech initilization failed", Toast.LENGTH_SHORT)
                .show()
        }
    }

    override fun onPause() {
        super.onPause()
        for (timerTask in timerTaskArray) {
            timerTask.cancel()
        }
        //handleRestartFlag()
    }

    override fun onDestroy() {
        textToSpeech?.let {
            it.stop()
            it.shutdown()
        }
        speech?.let {
            it.cancel()
            it.destroy()
        }
        super.onDestroy()
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

    val setTimerTask: (name: String, delay: Long, () -> Unit) -> TimerTask = { name, delay, doit ->
        timerTask = Timer(name, false).schedule(delay) {
            runOnUiThread {
                doit()
            }
        }
        timerTaskArray.add(timerTask)
        timerTask
    }

    fun disableNextButton() {
        button_next.setClickable(false)
    }

    fun enableNextButton() {
        button_next.setClickable(true)
    }

    fun hideNextButton() {
        button_next.visibility = View.GONE
    }

    fun showNextButton() {
        button_next.visibility = View.VISIBLE
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
        speech?.setRecognitionListener(this)
        val recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en")
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, "en")
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Please start speaking...")
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getPackageName())
        recognizerIntent.putExtra(
            RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS,
            1500
        );
        recognizerIntent.putExtra(
            RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS,
            1500
        );
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 1000);
        recognizerIntent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH
        )
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        speech?.startListening(recognizerIntent)
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
        Log.d("Speech Error", error.toString())
    }

    override fun onEvent(eventType: Int, params: Bundle?) {
    }

    override fun onPartialResults(partialResults: Bundle?) {
    }

    override fun onResults(results: Bundle?) {
    }

    override fun onRmsChanged(rmsdB: Float) {
    }

    fun View.setOnDebouncedClickListener(doStuff: () -> (Unit)) {
        this.setOnClickListener {
            val lastClickTime: Long = preferences.getLastClickTime(it.id) ?: -1L
            val currentTime = System.currentTimeMillis()
            if (lastClickTime == -1L || currentTime - lastClickTime > BaseFragment.MIN_TIME_BETWEEN_CLICKS) {
                preferences.storeLastClickTime(it.id, System.currentTimeMillis())
                doStuff()
            }
        }
    }

    companion object {
        private const val MIN_TIME_BETWEEN_CLICKS = 700L
        const val TRANSLATE_API_URL =
            "https://firebasestorage.googleapis.com/v0/b/chatter-f7ae2.appspot.com/o/TranslateApi%2Fchatter-f7ae2-75eff5610fbd.json?alt=media&token=c6cef0f5-8649-4282-ac54-1ec0cac56153"
    }
}
