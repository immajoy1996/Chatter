package com.example.chatter

import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.bottom_nav_bar.*
import kotlinx.android.synthetic.main.fragment_vocab.*
import kotlinx.android.synthetic.main.top_bar.*
import kotlinx.android.synthetic.main.top_bar.top_bar_mic
import kotlinx.android.synthetic.main.vocab_search_bar.*
import java.util.Collections.copy

class VocabFragment : BaseFragment() {
    private val spanishWords = arrayListOf<String>()
    private val translations = arrayListOf<String>()
    private val transliterations = arrayListOf<String>()
    private val audioSrc = arrayListOf<String>()
    private lateinit var vocabAdapter: VocabAdapter
    private lateinit var database: DatabaseReference

    private var searchSpanishWords = arrayListOf<String>()
    private var searchTranslations = arrayListOf<String>()
    private var searchTransliterations = arrayListOf<String>()
    private var searchAudioSrc = arrayListOf<String>()

    private val chatterActivity by lazy { activity as ChatterActivity }
    private var isMicActive = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_vocab, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        database = FirebaseDatabase.getInstance().reference
        setUpTopBar()
        setUpNavButtons()
        setUpVocabRecycler()
    }

    private fun setUpTopBar() {
        search_submit.setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_ATOP)
        top_bar_mic.setOnClickListener {
            if (!isMicActive) {
                isMicActive = true
                chatterActivity.toggleRestartFlag(false)
                chatterActivity.toggleIsVocabFragmentFlag(true)
                top_bar_mic.setImageResource(R.drawable.microphone_listening)
                (top_bar_mic.drawable as AnimationDrawable).start()
                chatterActivity.startListening()
            }
            else{
                isMicActive=false
                top_bar_mic.setImageResource(R.drawable.microphone_listening)
            }
        }
        vocab_search_text.setOnClickListener {
            val userInput = vocab_search_text.text?.toString()
            userInput?.let {
                doSearch(it)
            }
        }
    }

    private fun setUpNavButtons() {
        button_back.setOnClickListener {
            stopMediaPlayer()
            fragmentManager?.popBackStack()
            chatterActivity.loadOptionsMenu()
        }
        button_next.text = "Finish"
        button_next.setOnClickListener {
            stopMediaPlayer()
            chatterActivity.toggleRestartFlag(false)
            activity?.finish()
        }
    }

    private fun stopMediaPlayer() {
        if (vocabAdapter.mediaPlayer.isPlaying) {
            vocabAdapter.mediaPlayer.stop()
            vocabAdapter.mediaPlayer.release()
        }
    }

    fun clearVocab() {
        audioSrc.clear()
        spanishWords.clear()
        translations.clear()
        transliterations.clear()
    }

    fun clearSearchLists() {
        searchAudioSrc.clear()
        searchSpanishWords.clear()
        searchTranslations.clear()
        searchTransliterations.clear()
    }

    fun setUpVocabRecycler() {
        clearVocab()
        vocab_recycler.layoutManager = LinearLayoutManager(context)
        context?.let {
            vocabAdapter = VocabAdapter(it, audioSrc, spanishWords, translations, transliterations)
            vocab_recycler.adapter = vocabAdapter
        }
        val botTitle = chatterActivity.getCurrentBotTitle()
        val pathReference = database.child(VOCAB.plus(botTitle))
        val vocabListener = baseChildEventListener { dataSnapshot ->
            spanishWords.add(dataSnapshot.child(SPANISH_WORD).value.toString())
            translations.add(dataSnapshot.child(TRANSLATION).value.toString())
            transliterations.add(dataSnapshot.child(TRANSLITERATION).value.toString())
            audioSrc.add(dataSnapshot.child(AUDIO).value.toString())
            vocab_recycler.adapter?.notifyDataSetChanged()
        }
        pathReference.addChildEventListener(vocabListener)
    }

    fun setUpSearch(text: String) {
        vocab_search_text.setText(text)
        doSearch(text)
    }

    private fun doSearch(text: String) {
        clearSearchLists()
        val end = spanishWords.size
        for (i in 0 until end) {
            var str = spanishWords[i]
            if (chatterActivity.isMatch(text, str)) {
                var curTranslation = translations[i]
                var curTransliteration = transliterations[i]
                var curAudio = audioSrc[i]
                searchSpanishWords.add(str)
                searchTranslations.add(curTranslation)
                searchTransliterations.add(curTransliteration)
                searchAudioSrc.add(curAudio)
            }
        }
        clearVocab()
        val searchEnd = searchSpanishWords.size
        for (i in 0 until searchEnd) {
            spanishWords.add(searchSpanishWords[i])
            translations.add(searchTranslations[i])
            transliterations.add(searchTransliterations[i])
            audioSrc.add(searchAudioSrc[i])
        }
        vocab_recycler.adapter?.notifyDataSetChanged()
    }

    companion object {
        private const val SPANISH_WORD = "spanishWord"
        private const val TRANSLATION = "translation"
        private const val TRANSLITERATION = "transliteration"
        private const val AUDIO = "audio"
        private const val VOCAB = "Vocab/"
        const val RESULT_SPEECH = 3
    }
}
