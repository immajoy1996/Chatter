package com.example.chatter.ui.activity

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.chatter.R
import com.example.chatter.data.Vocab
import com.example.chatter.ui.fragment.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_flash_card.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class FlashCardActivity : BaseChatActivity() {

    private var flashCardCategoriesFragment = FlashCardCategoriesFragment()
    private lateinit var decksFragment: FlashCardDecksFragment
    private lateinit var myFavsFragment: FlashCardDecksFragment
    private lateinit var viewFlashcardsFragment: ViewFlashcardsFragment
    private var loadingAnimatedFragment = LoadingAnimatedFragment()
    private var flashcardsArray = arrayListOf<Vocab>()
    private var favoriteDeckPickerFragment = FavoriteDeckPickerFragment()
    private var executorService: ExecutorService? = null

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flash_card)
        setUpTopBar()
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
        executorService = Executors.newFixedThreadPool(5)
        initializeDecksFragment()
        loadFlashCardsCategoriesFragment()
    }

    private fun initializeDecksFragment() {
        val myLevel = intent?.getStringExtra("userLevel") ?: "Easy"
        decksFragment = FlashCardDecksFragment.newInstance(myLevel)
        myFavsFragment = FlashCardDecksFragment.newInstance(true)
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
        if (isFavoriteFragment) {
            if (auth.currentUser != null) {
                prepareFlashcards(decksList, true)
            } else {
                flashcardsArray = preferences.getMyFavoritesArray() ?: arrayListOf<Vocab>()
            }
        } else {
            prepareFlashcards(decksList, false)
        }
        setTimerTask("loadFlashcards", 2000, {
            supportFragmentManager.popBackStack()
            viewFlashcardsFragment =
                ViewFlashcardsFragment.newInstance(flashcardsArray, isFavoriteFragment)
            loadFragment(viewFlashcardsFragment)
        })
    }

    private fun prepareFlashcards(decksList: ArrayList<String>, isFavoriteFragment: Boolean) {
        val vocabListener = baseChildEventListener {
            val newExpression = it.child(VocabFragment.EXPRESSION).value.toString()
            val newDefinitionValue = it.child(VocabFragment.DEFINITION).value
            val newImage = it.child(VocabFragment.IMAGE).value
            val newFlashcardType = it.child(VocabFragment.FLASHCARD_TYPE).value
            val isFavorite = it.child(VocabFragment.IS_FAVORITE).value
            var image = ""
            var flashcardType = "text"
            newImage?.let {
                image = it.toString()
            }
            newFlashcardType?.let {
                flashcardType = it.toString()
            }
            var newIsFavorite = false
            if (isFavorite == true) {
                newIsFavorite = true
            }
            var newDefinition = ""
            newDefinitionValue?.let {
                newDefinition = it.toString()
            }
            val vocab = Vocab(
                newExpression,
                newDefinition,
                image,
                flashcardType,
                newIsFavorite
            )
            if (newDefinition.isEmpty()) {
                val savedDefinition =
                    preferences.getDefinition(newExpression, preferences.getCurrentTargetLanguage())
                if (savedDefinition.isEmpty()) {
                    executorService?.submit {
                        val result =
                            translate(newExpression, preferences.getCurrentTargetLanguage())
                        result?.let {
                            vocab.definition = it
                            preferences.storeWordAndDefinition(
                                newExpression,
                                it,
                                preferences.getCurrentTargetLanguage()
                            )
                        }
                        runOnUiThread {
                            flashcardsArray.add(vocab)
                        }
                    }
                } else {
                    vocab.definition = savedDefinition
                    flashcardsArray.add(vocab)
                }
            } else {
                flashcardsArray.add(vocab)
            }
        }
        if (isFavoriteFragment) {
            if (auth.currentUser != null) {
                auth.currentUser?.let {
                    val uid = it.uid
                    database.child("Users").child(uid).child("My Favorites")
                        .addChildEventListener(vocabListener)
                }
            }
        } else {
            for (botTitle in decksList) {
                database.child("Vocab").child(botTitle).addChildEventListener(vocabListener)
            }
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

    fun loadFavoriteDeckPickerFragment() {
        supportFragmentManager
            .beginTransaction()
            .replace(flashcard_root_container.id, favoriteDeckPickerFragment)
            .addToBackStack(favoriteDeckPickerFragment.javaClass.name)
            .commit()
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