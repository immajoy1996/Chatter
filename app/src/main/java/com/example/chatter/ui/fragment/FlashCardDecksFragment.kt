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
import com.example.chatter.data.BotVocab
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
    private var completionPercentage: ArrayList<Int> = arrayListOf()

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
    }

    override fun onResume() {
        super.onResume()
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
        } else {
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
        completionPercentage.clear()
    }

    private fun sortListsByCompletionPercentage() {
        var curPos = 0
        val n = completionPercentage.size
        while (curPos < n) {
            var min = 1000
            var i = curPos
            var index = curPos
            while (i < n) {
                if (completionPercentage[i] < min) {
                    min = completionPercentage[i]
                    index = i
                }
                i++
            }
            swap(curPos, index)
            curPos++
        }
    }

    private fun swap(index1: Int, index2: Int) {
        swapStringArrayPositions(botTitles, index1, index2)
        swapStringArrayPositions(botImages, index1, index2)
        swapStringArrayPositions(botDescriptions, index1, index2)
        swapIntArrayPositions(completionPercentage, index1, index2)
    }

    private fun swapStringArrayPositions(list: ArrayList<String>, index1: Int, index2: Int) {
        val temp = list[index1]
        list[index1] = list[index2]
        list[index2] = temp
    }

    private fun swapIntArrayPositions(list: ArrayList<Int>, index1: Int, index2: Int) {
        val temp = list[index1]
        list[index1] = list[index2]
        list[index2] = temp
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

    private fun String.compareLevelTo(otherLevel: String): Int {
        if (this == otherLevel) return 0
        when (this) {
            "Easy" -> {
                return -1
            }
            "Medium" -> {
                if (otherLevel == "Easy") return 1
                else return -1
            }
            "Hard" -> {
                return 1
            }
        }
        return 0
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
            if (myLevel.isNotEmpty() && botLevel.compareLevelTo(myLevel) <= 0) {
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
                    completionPercentage.add(preferences.getCompletionRate(botTitle))
                }
            }

            context?.let {
                Log.d("BotTitles", botTitles.toString())
                Log.d("BotDesc", botDescriptions.toString())
                if (isMultipleChoiceFragment) {
                    flashcard_decks_recycler.adapter = FlashCardDecksAdapter(
                        it,
                        botTitles,
                        botImages,
                        null,
                        botDescriptions,
                        null,
                        this
                    )
                } else {
                    sortListsByCompletionPercentage()
                    flashcard_decks_recycler.adapter = FlashCardDecksAdapter(
                        it,
                        botTitles,
                        botImages,
                        null,
                        botDescriptions,
                        null,
                        this,
                        null,
                        null,
                        null,
                        completionPercentage
                    )
                }
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