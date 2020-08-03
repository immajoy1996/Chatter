package com.example.chatter.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatter.R
import com.example.chatter.adapters.FlashCardDecksAdapter
import com.example.chatter.interfaces.ConcentrationGameClickedInterface
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.sun.mail.imap.protocol.FLAGS
import kotlinx.android.synthetic.main.activity_games.*
import kotlinx.android.synthetic.main.top_bar.*

class GamesActivity : BaseActivity(), ConcentrationGameClickedInterface {
    private var botTitles = arrayListOf<String>()
    private var botImages = arrayListOf<String>()
    private var gamesImages = arrayListOf<Int>()
    private var botDescriptions = arrayListOf<String>()

    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_games)
        database = FirebaseDatabase.getInstance().reference
        setUpTopBar()
        setUpGamesRecycler()
        fetchGames()
    }

    override fun setUpTopBar() {
        back.visibility = View.VISIBLE
        back.setOnClickListener {
            finish()
        }
        home.visibility = View.GONE
        top_bar_title.text = "Games!"
        top_bar_title.visibility = View.VISIBLE
    }

    private fun setUpGamesRecycler() {
        games_recycler.apply {
            layoutManager = LinearLayoutManager(this@GamesActivity)
        }
    }

    private fun resetArrays() {
        botTitles.clear()
        gamesImages.clear()
        botDescriptions.clear()
    }

    private fun fetchGames() {
        resetArrays()

        botTitles.add("Listen to me!")
        botTitles.add("Multiple Choice")
        botTitles.add("Concentration")

        gamesImages.add(R.drawable.audio)
        gamesImages.add(R.drawable.quiz_icon)
        gamesImages.add(R.drawable.concentration)

        botDescriptions.add("Write what Helper Monkey says")
        botDescriptions.add("Choose the correct meaning of the expression or word")
        botDescriptions.add("Be quick and match the bot images")

        games_recycler.adapter = FlashCardDecksAdapter(
            this,
            botTitles,
            null,
            gamesImages,
            botDescriptions,
            true,
            this
        )
    }

    override fun onResume() {
        super.onResume()
        launched = false
    }

    private var launched = false

    override fun onConcentrationGameClicked() {
        val imageArray = arrayListOf<String>()
        database.child("BotCatalog").addChildEventListener(baseChildEventListener {
            if (!launched) {
                val botImage = it.child("botImage").value.toString()
                val intent = Intent(this, ConcentrationActivity::class.java)
                imageArray.add(botImage)
                if (imageArray.size >= NUM_BOTS) {
                    launched = true
                    intent.putStringArrayListExtra("botImages", imageArray)
                    startActivity(intent)
                }
            }
        })
    }

    companion object {
        private const val NUM_BOTS = 9
    }
}