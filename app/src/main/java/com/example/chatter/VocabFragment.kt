package com.example.chatter

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.bottom_nav_bar.*
import kotlinx.android.synthetic.main.fragment_vocab.*

class VocabFragment : Fragment() {
    private val spanishWords = ArrayList<String>()
    private val translations = ArrayList<String>()
    private val transliterations = ArrayList<String>()
    private val database: DatabaseReference? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_vocab, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpSpanishWords()
        setUpSpanishTranslations()
        setUpSpanishTransliterations()
        vocab_recycler.layoutManager = LinearLayoutManager(context)
        context?.let {
            vocab_recycler.adapter = VocabAdapter(it, spanishWords, translations, transliterations)
        }
        setUpNavButtons()
    }

    private fun setUpNavButtons() {
        button_back.setOnClickListener { fragmentManager?.popBackStack() }
        button_next.text = "Finish"
        button_next.setOnClickListener { activity?.finish() }
    }

    fun setUpVocabRecycler() {
        val vocabListener = createVocabListener { dataSnapshot ->
            spanishWords.add(dataSnapshot.child(SPANISH_WORD).value.toString())
            translations.add(dataSnapshot.child(TRANSLATION).value.toString())
            transliterations.add(dataSnapshot.child(TRANSLITERATION).value.toString())
        }

    }

    val createVocabListener: ((DataSnapshot) -> Unit) -> ValueEventListener = { doit ->
        val vocabListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                doit(dataSnapshot)
            }

            override fun onCancelled(data: DatabaseError) {
                //TODO
            }
        }
        vocabListener
    }

    fun setUpSpanishWords() {
        spanishWords.add("tener")
        spanishWords.add("hacer")
        spanishWords.add("ver")
        spanishWords.add("buscar")
    }

    fun setUpSpanishTranslations() {
        translations.add("to have")
        translations.add("to do")
        translations.add("to see")
        translations.add("to look")
    }

    fun setUpSpanishTransliterations() {
        transliterations.add("(ten-er)")
        transliterations.add("(has-er)")
        transliterations.add("(ver)")
        transliterations.add("(boos-car)")
    }

    companion object {
        private const val SPANISH_WORD = "spanishWord"
        private const val TRANSLATION = "translation"
        private const val TRANSLITERATION = "transliteration"
    }
}
