package com.example.chatter.ui.activity

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import com.example.chatter.R
import com.example.chatter.data.Vocab
import com.example.chatter.ui.fragment.*
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_flash_card.*

class FlashCardActivity : BaseChatActivity() {

    private var flashCardCategoriesFragment = FlashCardCategoriesFragment()
    private lateinit var decksFragment: FlashCardDecksFragment
    private lateinit var myFavsFragment: FlashCardDecksFragment
    private lateinit var viewFlashcardsFragment: ViewFlashcardsFragment
    private var loadingAnimatedFragment = LoadingAnimatedFragment()
    private lateinit var database: DatabaseReference
    private var flashcardsArray = arrayListOf<Vocab>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flash_card)
        setUpTopBar()
        database = FirebaseDatabase.getInstance().reference
        initializeDecksFragment()
        loadFlashCardsCategoriesFragment()
    }

    private fun initializeDecksFragment() {
        val myLevel = intent?.getStringExtra("userLevel") ?: "Easy"
        decksFragment = FlashCardDecksFragment.newInstance(myLevel)
        myFavsFragment = FlashCardDecksFragment.Companion.newInstance(true)
    }

    fun readFlashcard(text: String) {
        letBearSpeak(text)
    }

    fun loadFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(flashcard_root_container.id, fragment)
            .addToBackStack(fragment.javaClass.name)
            .commit()
    }

    private fun resetFlashCardsArray() {
        flashcardsArray.clear()
    }

    fun loadViewFlashCardsFragment(decksList: ArrayList<String>, isFavoriteFragment: Boolean) {
        resetFlashCardsArray()
        loadFragment(loadingAnimatedFragment)
        prepareFlashcards(decksList)
        setTimerTask("loadFlashcards", 2000, {
            viewFlashcardsFragment =
                ViewFlashcardsFragment.newInstance(flashcardsArray, isFavoriteFragment)
            loadFragment(viewFlashcardsFragment)
        })
    }

    private fun prepareFlashcards(decksList: ArrayList<String>) {
        val vocabListener = baseChildEventListener {
            val newExpression = it.child(VocabFragment.EXPRESSION).value.toString()
            val newDefinition = it.child(VocabFragment.DEFINITION).value.toString()
            val newImage = it.child(VocabFragment.IMAGE).value
            val newFlashcardType = it.child(VocabFragment.FLASHCARD_TYPE).value
            var image = ""
            var flashcardType = "text"
            newImage?.let {
                image = it.toString()
            }
            newFlashcardType?.let {
                flashcardType = it.toString()
            }
            flashcardsArray.add(Vocab(newExpression, newDefinition, image, flashcardType))
        }
        for (botTitle in decksList) {
            database.child("Vocab").child(botTitle).addChildEventListener(vocabListener)
        }
    }

    fun loadFlashCardsCategoriesFragment() {
        loadFragment(flashCardCategoriesFragment)
    }

    fun loadDecksFragment() {
        loadFragment(decksFragment)
    }

    fun loadMyFavoritesFragment() {
        loadFragment(myFavsFragment)
    }

    override fun setUpTopBar() {
        //not implemented
    }

    override fun addMessage(msg: String) {
        //TODO("Not yet implemented")
    }

    override fun initializeMessagesContainer() {
        //TODO("Not yet implemented")
    }

    override fun showFirstBotMessage() {
        //TODO("Not yet implemented")
    }
}