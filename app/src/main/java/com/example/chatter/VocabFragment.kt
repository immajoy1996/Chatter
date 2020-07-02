package com.example.chatter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.bottom_nav_bar.*
import kotlinx.android.synthetic.main.fragment_vocab.*
import kotlinx.android.synthetic.main.vocab_search_bar.*

class VocabFragment : BaseFragment() {
    private val expressions = arrayListOf<String>()
    private val definitions = arrayListOf<String>()
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
        vocabl_search_title.text = "Expressions"
        /*search_submit.setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_ATOP)
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
        }*/
    }

    private fun setUpNavButtons() {
        button_back.setOnClickListener {
            fragmentManager?.popBackStack()
            chatterActivity.loadOptionsMenu()
        }
        button_next.text = "Finish"
        button_next.setOnClickListener {
            chatterActivity.toggleRestartFlag(false)
            activity?.finish()
        }
    }

    fun clearSearchLists() {
        searchAudioSrc.clear()
        searchSpanishWords.clear()
        searchTranslations.clear()
        searchTransliterations.clear()
    }

    private fun setUpVocabRecycler() {
        vocab_recycler.layoutManager = LinearLayoutManager(context)
        /*context?.let {
            vocabAdapter = VocabAdapter(it, expressions, definitions, activity as ChatterActivity)
            vocab_recycler.adapter = vocabAdapter
        }*/
        val botTitle = chatterActivity.getCurrentBotTitle()
        val pathReference = database.child(VOCAB.plus(botTitle))
        val vocabListener = baseChildEventListener { dataSnapshot ->
            expressions.add(dataSnapshot.child(EXPRESSION).value.toString())
            definitions.add(dataSnapshot.child(DEFINITION).value.toString())
            context?.let {
                vocabAdapter = VocabAdapter(it, expressions, definitions, activity as ChatterActivity)
                vocab_recycler.adapter = vocabAdapter
            }
        }
        pathReference.addChildEventListener(vocabListener)
    }

    fun setUpSearch(text: String) {
        vocab_search_text.setText(text)
    }

    companion object {
        private const val EXPRESSION = "expression"
        private const val DEFINITION = "definition"
        private const val TRANSLITERATION = "transliteration"
        private const val AUDIO = "audio"
        private const val VOCAB = "Vocab/"
        const val RESULT_SPEECH = 3
    }
}
