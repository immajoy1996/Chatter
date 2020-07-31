package com.example.chatter.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatter.R
import com.example.chatter.adapters.FlashCardDecksAdapter
import com.example.chatter.ui.activity.FlashCardActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.bottom_nav_bar.*
import kotlinx.android.synthetic.main.fragment_levels.*
import kotlinx.android.synthetic.main.top_bar.*

class FlashCardDecksFragment : BaseFragment() {
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private var botTitles = arrayListOf<String>()
    private var botImages = arrayListOf<String>()
    private var botDescriptions = arrayListOf<String>()

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
        fetchDecks()
    }

    private fun setUpTopBar() {
        home.visibility = View.GONE
        back.visibility = View.VISIBLE
        back.setOnClickListener {
            fragmentManager?.popBackStack()
            (activity as? FlashCardActivity)?.loadFlashCardsCategoriesFragment()
        }
        top_bar_title.visibility = View.VISIBLE
        top_bar_title.text = "Decks"
    }

    private fun setUpBottomBar() {
        button_back.visibility = View.GONE
        button_start.visibility = View.VISIBLE
        button_start.setOnDebouncedClickListener {
            fragmentManager?.popBackStack()
            (activity as? FlashCardActivity)?.loadViewFlashCardsFragment()
        }
        button_next.visibility = View.GONE
    }

    private fun setUpDecksRecycler() {
        flashcard_decks_recycler.apply {
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun fetchDecks() {
        botTitles.clear()
        botImages.clear()
        botDescriptions.clear()

        botTitles.add("")
        botTitles.add("The Mummy")
        botTitles.add("The Alien")
        botTitles.add("The Jester")
        botImages.add("")
        botImages.add("https://firebasestorage.googleapis.com/v0/b/chatter-f7ae2.appspot.com/o/botImages%2Fmummy.png?alt=media&token=f3171b96-cf77-41a3-a87e-57f7b2976b45")
        botImages.add("https://firebasestorage.googleapis.com/v0/b/chatter-f7ae2.appspot.com/o/botImages%2Falien.png?alt=media&token=ded95afb-3256-4e8d-9aa3-65759929d03e")
        botImages.add("https://firebasestorage.googleapis.com/v0/b/chatter-f7ae2.appspot.com/o/botImages%2Fjester.png?alt=media&token=785fbfb4-1e5f-4f19-9cd9-21552992f40e")
        botDescriptions.add("")
        botDescriptions.add("Learn words that teach you a secret and dangerous magic")
        botDescriptions.add("If you ever meet an alien, you'll know what to do")
        botDescriptions.add("Pass away your time with frivolous jokes")

        context?.let {
            flashcard_decks_recycler.adapter = FlashCardDecksAdapter(
                it,
                botTitles,
                botImages,
                botDescriptions
            )
        }
    }
}