package com.example.chatter.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatter.R
import com.example.chatter.adapters.VocabAdapter
import com.example.chatter.data.Expression
import com.example.chatter.data.Vocab
import com.example.chatter.interfaces.SubmitExpressionInterface
import com.example.chatter.ui.activity.BaseActivity
import com.example.chatter.ui.activity.BaseChatActivity
import com.example.chatter.ui.activity.ChatterActivity
import com.example.chatter.ui.activity.CreateChatActivity
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.bottom_nav_bar.*
import kotlinx.android.synthetic.main.fragment_vocab.*
import kotlinx.android.synthetic.main.top_bar.*
import kotlinx.android.synthetic.main.vocab_search_bar.*
import kotlin.math.exp

class VocabFragment : BaseFragment(), SubmitExpressionInterface {
    private val expressions = arrayListOf<String>()
    private val definitions = arrayListOf<String>()
    private var vocabArray = arrayListOf<Vocab>()
    private lateinit var vocabAdapter: VocabAdapter
    private lateinit var database: DatabaseReference

    private var chatterActivity: BaseChatActivity? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_vocab, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        database = FirebaseDatabase.getInstance().reference
        if (activity is ChatterActivity) {
            chatterActivity = activity as ChatterActivity
        } else {
            chatterActivity = activity as CreateChatActivity
        }
        setUpTopBar()
        setUpNavButtons()
        setUpVocabRecycler()
    }

    private fun setUpTopBar() {
        vocabl_search_title.text = "Vocab"
        top_bar.setOnLongClickListener {
            if (vocab_top_bar_plus_button.visibility == View.VISIBLE) {
                vocab_top_bar_plus_button.visibility = View.INVISIBLE
            } else {
                vocab_top_bar_plus_button.visibility = View.VISIBLE
            }
            true
        }
        vocab_screen_back.setOnClickListener {
            fragmentManager?.popBackStack()
            if (activity is ChatterActivity) {
                (activity as? ChatterActivity)?.loadOptionsMenu()
            } else if (activity is CreateChatActivity) {
                (activity as? CreateChatActivity)?.loadOptionsMenu()
            }
        }
        if (activity is CreateChatActivity) {
            vocab_top_bar_plus_button.visibility = View.VISIBLE
            vocab_top_bar_plus_button.setOnClickListener {
                addEditableExpressionToRecycler()
            }
        }
    }

    private fun addEditableExpressionToRecycler() {
        vocabArray.add(0, Vocab("", "", null, null))
        context?.let {
            vocab_recycler.adapter = VocabAdapter(
                it,
                vocabArray,
                if (activity is ChatterActivity) activity as ChatterActivity else activity as CreateChatActivity,
                this
            )
        }
    }

    private fun setUpNavButtons() {
        button_back.visibility = View.GONE
        button_next.visibility = View.GONE
        button_start.visibility = View.VISIBLE
        button_start.text = "Finish"
        button_start.setOnClickListener {
            activity?.finish()
        }
    }

    private fun resetArrays() {
        vocabArray.clear()
    }

    private fun setUpVocabRecycler() {
        resetArrays()
        vocab_recycler.layoutManager = LinearLayoutManager(context)
        val botTitle =
            if (activity is ChatterActivity) (activity as? ChatterActivity)?.getCurrentBotTitle() else
                (activity as? CreateChatActivity)?.getCurrentBotTitle()
        val pathReference = database.child(VOCAB.plus(botTitle))

        val vocabListener = baseChildEventListener { dataSnapshot ->
            val newExpression = dataSnapshot.child(EXPRESSION).value.toString()
            val newDefinition = dataSnapshot.child(DEFINITION).value.toString()
            val newImage = dataSnapshot.child(IMAGE).value
            val newFlashcardType = dataSnapshot.child(FLASHCARD_TYPE).value

            var vocabImage: String? = null
            var flashcardType: String? = null
            newImage?.let {
                vocabImage = it.toString()
            }
            newFlashcardType?.let {
                flashcardType = it.toString()
            }
            placeExpressionAlphabetically(
                Vocab(
                    newExpression,
                    newDefinition,
                    vocabImage,
                    flashcardType
                )
            )
            context?.let {
                vocabAdapter = VocabAdapter(
                    it,
                    vocabArray,
                    if (activity is ChatterActivity) activity as ChatterActivity else activity as CreateChatActivity,
                    this
                )
                vocab_recycler.adapter = vocabAdapter
            }
        }
        pathReference.addChildEventListener(vocabListener)
    }

    private fun placeExpressionAlphabetically(vocabItem: Vocab) {
        var index = 0
        for (item in vocabArray) {
            if (vocabItem.expression.toLowerCase().compareTo(item.expression.toLowerCase()) < 0) {
                vocabArray.add(index, vocabItem)
                return
            }
            index++
        }
        vocabArray.add(vocabItem)
    }

    override fun onSubmitExpressionClicked(expression: String, definition: String) {
        val botTitle =
            if (activity is ChatterActivity) (activity as? ChatterActivity)?.getCurrentBotTitle() else
                (activity as? CreateChatActivity)?.getCurrentBotTitle()
        val str1 = (Math.random() * 10000).toInt().toString()
        val str2 = (Math.random() * 10000).toInt().toString()
        val childId = "Word $str1 $str2"
        val expressionReference = database.child(VOCAB.plus(botTitle)).child(childId)
        if (expression.isNotEmpty() && definition.isNotEmpty()) {
            expressionReference.setValue(Expression(expression, definition)).addOnSuccessListener {
                Toast.makeText(context, "Expression Added", Toast.LENGTH_LONG).show()
            }.addOnFailureListener {
                Toast.makeText(context, "Something went wrong", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(context, "Enter an expression", Toast.LENGTH_LONG).show()
        }
    }

    override fun onResume() {
        super.onResume()
        if (vocab_top_bar_plus_button.visibility == View.VISIBLE) {
            vocab_top_bar_plus_button.visibility = View.INVISIBLE
        }
    }

    companion object {
        const val EXPRESSION = "expression"
        const val DEFINITION = "definition"
        const val FLASHCARD_TYPE = "flashcardType"
        const val IMAGE = "image"
        const val IS_FAVORITE = "is_favorite"
        private const val TRANSLITERATION = "transliteration"
        private const val AUDIO = "audio"
        private const val VOCAB = "Vocab/"
        const val RESULT_SPEECH = 3
    }
}
