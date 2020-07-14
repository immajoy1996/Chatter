package com.example.chatter.ui.activity

import BotSelectionAdapter
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatter.ui.fragment.QuizFragment
import com.example.chatter.R
import com.example.chatter.interfaces.BotSelectedInterface
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_create_bot.*
import kotlinx.android.synthetic.main.bottom_nav_bar.*
import kotlinx.android.synthetic.main.top_bar.*

class CreateBotActivity : BaseSelectionActivity(),
    BotSelectedInterface {

    private var bots = arrayListOf<String>()
    private var botImages = arrayListOf<String>()
    private lateinit var database: DatabaseReference

    private var selectedBotTitle: String? = null
    private var selectedBotImage: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_bot)
        database = FirebaseDatabase.getInstance().reference
        setUpTopBar()
        setUpBottomNavBar()
        setUpDropdownRecycler()
        setUpScrollListener()
        setUpArrowClicks()
    }

    override fun onBotSelected(botTitle: String, botImage: String) {
        selectedBotTitle = botTitle
        selectedBotImage = botImage
    }

    private fun setUpBottomNavBar() {
        button_back.setOnClickListener {
            this.finish()
        }
        button_next.setOnClickListener {
            val intent = Intent(this, CreateChatActivity::class.java)
            intent.putExtra(BOT_TITLE, selectedBotTitle)
            intent.putExtra(IMAGE_PATH, selectedBotImage)
            startActivity(intent)
        }
    }

    private fun loadQuizFragment() {
        loadFragmentIntoInnerContainer(QuizFragment())
    }

    private fun loadFragmentIntoInnerContainer(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(bot_quiz_inner_layout.id, fragment)
            .addToBackStack(fragment.javaClass.name)
            .commit()
    }

    override fun setUpTopBar() {
        top_bar_title.text = "Select a Bot"
        top_bar_mic.visibility = View.GONE
    }

    override fun setUpDropdownRecycler() {
        create_bot_recycler.apply {
            layoutManager = LinearLayoutManager(this@CreateBotActivity)
        }
        database.child("BotCatalog").addChildEventListener(baseChildEventListener {
            val botImage = it.child("botImage").value.toString()
            val botTitle = it.child("botTitle").value.toString()
            placeBotAlphabetically(botTitle, botImage)
            create_bot_recycler.adapter = BotSelectionAdapter(
                this@CreateBotActivity,
                bots,
                botImages,
                this
            )
        })
    }

    private fun placeBotAlphabetically(botTitle: String, botImage: String) {
        var index = 0
        while (index < bots.size && botTitle.compareTo(bots[index]) >= 0) {
            index++
        }
        if (index < bots.size) {
            bots.add(index, botTitle)
            botImages.add(index, botImage)
        } else {
            bots.add(botTitle)
            botImages.add(botImage)
        }
    }

    override fun setUpScrollListener() {
        val layoutManager = create_bot_recycler.layoutManager as LinearLayoutManager
        create_bot_recycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItem = layoutManager.findFirstCompletelyVisibleItemPosition()
                val lastVisibleItem = layoutManager.findLastCompletelyVisibleItemPosition()
                if (firstVisibleItem == 0) {
                    create_bot_up_arrow.visibility = View.INVISIBLE
                } else {
                    create_bot_up_arrow.visibility = View.VISIBLE
                }
                if (lastVisibleItem == totalItemCount - 1) {
                    create_bot_down_arrow.visibility = View.INVISIBLE
                } else {
                    create_bot_down_arrow.visibility = View.VISIBLE
                }
            }
        })
    }

    override fun setUpArrowClicks() {
        val layoutManager = create_bot_recycler.layoutManager as LinearLayoutManager
        create_bot_up_arrow.setOnClickListener {
            val firstVisibleItem = layoutManager.findFirstCompletelyVisibleItemPosition()
            val targetPos = if (firstVisibleItem - 3 >= 0) firstVisibleItem - 3 else 0
            create_bot_recycler.smoothScrollToPosition(targetPos)
        }
        create_bot_down_arrow.setOnClickListener {
            val lastVisibleItem = layoutManager.findLastCompletelyVisibleItemPosition()
            val totalItemCount = layoutManager.itemCount
            val targetPos =
                if (lastVisibleItem + 3 < totalItemCount) lastVisibleItem + 3 else totalItemCount - 1
            create_bot_recycler.smoothScrollToPosition(targetPos)
        }
    }

    companion object {
        private const val BOT_TITLE = "BOT_TITLE"
        private const val IMAGE_PATH = "IMAGE_PATH"
    }
}