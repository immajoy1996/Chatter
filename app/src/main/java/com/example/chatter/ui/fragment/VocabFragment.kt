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
    private lateinit var vocabAdapter: VocabAdapter
    private lateinit var database: DatabaseReference

    private var searchSpanishWords = arrayListOf<String>()
    private var searchTranslations = arrayListOf<String>()
    private var searchTransliterations = arrayListOf<String>()
    private var searchAudioSrc = arrayListOf<String>()

    private var chatterActivity: BaseChatActivity? = null
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
        vocabl_search_title.text = "Expressions"
        vocab_screen_home.setOnLongClickListener {
            if (vocab_top_bar_plus_button.visibility == View.VISIBLE) {
                vocab_top_bar_plus_button.visibility = View.INVISIBLE
            }else{
                vocab_top_bar_plus_button.visibility = View.VISIBLE
            }
            true
        }
        if (activity is CreateChatActivity) {
            vocab_top_bar_plus_button.visibility = View.VISIBLE
            vocab_top_bar_plus_button.setOnClickListener {
                addEditableExpressionToRecycler()
            }
        }
    }

    private fun addEditableExpressionToRecycler() {
        expressions.add(0, "")
        definitions.add(0, "")
        context?.let {
            vocab_recycler.adapter = VocabAdapter(
                it,
                expressions,
                definitions,
                if (activity is ChatterActivity) activity as ChatterActivity else activity as CreateChatActivity,
                this
            )
        }
    }

    private fun setUpNavButtons() {
        button_back.setOnClickListener {
            fragmentManager?.popBackStack()
            if (activity is ChatterActivity) {
                (activity as? ChatterActivity)?.loadOptionsMenu()
            } else if (activity is CreateChatActivity) {
                (activity as? CreateChatActivity)?.loadOptionsMenu()
            }
        }
        button_next.text = "Finish"
        button_next.setOnClickListener {
            chatterActivity?.toggleRestartFlag(false)
            activity?.finish()
        }
    }

    private fun resetArrays() {
        expressions.clear()
        definitions.clear()
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
            placeExpressionAlphabetically(newExpression, newDefinition)

            context?.let {
                vocabAdapter = VocabAdapter(
                    it,
                    expressions,
                    definitions,
                    if (activity is ChatterActivity) activity as ChatterActivity else activity as CreateChatActivity,
                    this
                )
                vocab_recycler.adapter = vocabAdapter
            }
        }
        pathReference.addChildEventListener(vocabListener)
    }

    private fun placeExpressionAlphabetically(expression: String, definition: String) {
        var index = 0
        for (item in expressions) {
            if (expression.compareTo(item) < 0) {
                expressions.add(index, expression)
                definitions.add(index, definition)
                return
            }
            index++
        }
        expressions.add(expression)
        definitions.add(definition)
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
        private const val EXPRESSION = "expression"
        private const val DEFINITION = "definition"
        private const val TRANSLITERATION = "transliteration"
        private const val AUDIO = "audio"
        private const val VOCAB = "Vocab/"
        const val RESULT_SPEECH = 3
    }
}
