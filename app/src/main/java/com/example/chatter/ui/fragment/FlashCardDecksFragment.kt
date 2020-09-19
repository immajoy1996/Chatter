package com.example.chatter.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatter.R
import com.example.chatter.adapters.FlashCardDecksAdapter
import com.example.chatter.data.QuestionList
import com.example.chatter.interfaces.DeckSelectedInterface
import com.example.chatter.ui.activity.FlashCardActivity
import com.example.chatter.ui.activity.GamesActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.bottom_nav_bar.*
import kotlinx.android.synthetic.main.fragment_levels.*
import kotlinx.android.synthetic.main.top_bar.*

class FlashCardDecksFragment : BaseFragment(), DeckSelectedInterface {
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private var botTitles = arrayListOf<String>()
    private var botImages = arrayListOf<String>()
    private var botDescriptions = arrayListOf<String>()
    private var myLevel: String = "Easy"
    private var isFavoritesFragment = false
    private var selectedDecksArray = arrayListOf<String>()
    private var isMultipleChoiceFragment = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_levels, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        database = FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()
        setUpTopBar()
        setUpBottomBar()
        setUpDecksRecycler()
        if (isFavoritesFragment) {
            fetchMyFavoriteDecks()
        } else {
            fetchDecks()
        }
    }

    private fun setUpTopBar() {
        home.visibility = View.GONE
        back.visibility = View.VISIBLE
        back.setOnClickListener {
            fragmentManager?.popBackStack()
            (activity as? FlashCardActivity)?.loadFlashCardsCategoriesFragment()
        }
        top_bar_title.visibility = View.VISIBLE
        if (isFavoritesFragment) {
            top_bar_title.text = "My Favorites"
        } else if (isMultipleChoiceFragment) {
            top_bar_title.text = "Chats"
        }else {
            top_bar_title.text = "Decks"
        }
    }

    private fun setUpBottomBar() {
        button_back.visibility = View.GONE
        button_start.visibility = View.VISIBLE
        button_start.setOnDebouncedClickListener {
            if (selectedDecksArray.isEmpty()) {
                if (isMultipleChoiceFragment) {
                    Toast.makeText(context, "Select a bot to test!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Select a deck!", Toast.LENGTH_SHORT).show()
                }
            } else {
                fragmentManager?.popBackStack()
                if (isMultipleChoiceFragment) {
                    (activity as? GamesActivity)?.loadMultipleChoiceQuestions(selectedDecksArray)
                } else {
                    (activity as? FlashCardActivity)?.loadViewFlashCardsFragment(
                        selectedDecksArray,
                        isFavoritesFragment
                    )
                }
            }
        }
        button_next.visibility = View.GONE
    }

    private fun setUpDecksRecycler() {
        flashcard_decks_recycler.apply {
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun resetDecks() {
        selectedDecksArray.clear()
        botTitles.clear()
        botImages.clear()
        botDescriptions.clear()
    }

    private fun fetchMyFavoriteDecks() {
        resetDecks()
        botImages.add("")
        botTitles.add("")
        botDescriptions.add("")

        val decksListener = baseChildEventListener { it ->
            val botImage = it.child("deckImage").value.toString()
            val botTitle = it.child("deckTitle").value.toString()
            val deckDescription = it.child("deckDescription").value
            botImages.add(botImage)
            botTitles.add(botTitle)
            botDescriptions.add(deckDescription.toString())
            context?.let {
                flashcard_decks_recycler.adapter = FlashCardDecksAdapter(
                    it,
                    botTitles,
                    botImages,
                    null,
                    botDescriptions,
                    null,
                    this
                )
            }
        }
        database.child(FLASHCARD_PATH).addChildEventListener(decksListener)
    }

    private fun fetchDecks() {
        resetDecks()
        val decksListener = baseChildEventListener { it ->
            val botImage = it.child("botImage").value.toString()
            val botTitle = it.child("botTitle").value.toString()
            val deckDescription = it.child("deckDescription").value
            val botDescription = it.child("botDescription").value
            val level = it.child("level").value
            var botLevel = "Hard"
            level?.let {
                botLevel = it.toString()
            }
            if (myLevel.isNotEmpty() && botLevel.compareTo(myLevel) <= 0) {
                botImages.add(botImage)
                botTitles.add(botTitle)
                if (isMultipleChoiceFragment) {
                    if (botDescription == null) {
                        botDescriptions.add("");
                    } else {
                        botDescriptions.add(botDescription.toString())
                    }
                } else {
                    if (deckDescription == null) {
                        botDescriptions.add("");
                    } else {
                        botDescriptions.add(deckDescription.toString())
                    }
                }
            }
            context?.let {
                Log.d("BotTitles", botTitles.toString())
                Log.d("BotDesc", botDescriptions.toString())
                flashcard_decks_recycler.adapter = FlashCardDecksAdapter(
                    it,
                    botTitles,
                    botImages,
                    null,
                    botDescriptions,
                    null,
                    this
                )
            }
        }
        database.child("BotCatalog").addChildEventListener(decksListener)
    }

    override fun onDeckSelected(botTitle: String) {
        selectedDecksArray.add(botTitle)
    }

    override fun onDeckUnselected(botTitle: String) {
        selectedDecksArray.remove(botTitle)
    }

    companion object {
        fun newInstance(myLevel: String): FlashCardDecksFragment {
            val fragment = FlashCardDecksFragment()
            fragment.myLevel = myLevel
            return fragment
        }

        fun newInstance(isFavoritesFragment: Boolean): FlashCardDecksFragment {
            val fragment = FlashCardDecksFragment()
            fragment.isFavoritesFragment = isFavoritesFragment
            return fragment
        }

        fun newInstance(
            isMultipleChoiceFragment: Boolean,
            userLevel: String
        ): FlashCardDecksFragment {
            val fragment = FlashCardDecksFragment()
            fragment.myLevel = userLevel
            fragment.isMultipleChoiceFragment = isMultipleChoiceFragment
            return fragment
        }

        const val FLASHCARD_PATH = "Flashcards/MyFavorites"
    }
}