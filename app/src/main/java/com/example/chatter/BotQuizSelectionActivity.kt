package com.example.chatter

import BotQuizAdapter
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_bot_quiz_selection.*
import kotlinx.android.synthetic.main.bottom_nav_bar.*
import kotlinx.android.synthetic.main.top_bar.*

class BotQuizSelectionActivity : BaseActivity(), BotSelectedInterface {
    private var bots = arrayListOf<String>()
    private var botImages = arrayListOf<String>()
    private lateinit var database: DatabaseReference

    private var selectedBotTitle: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bot_quiz_selection)
        database = FirebaseDatabase.getInstance().reference
        setUpTopBar()
        setUpBottomNavBar()
        setUpDropdownRecycler()
        setUpScrollListener()
        setUpArrowClicks()
    }

    override fun onBotSelected(botTitle: String) {
        selectedBotTitle = botTitle
    }

    private fun setUpBottomNavBar() {
        button_back.setOnClickListener {
            this.finish()
        }
        button_next.setOnClickListener {
            loadQuizFragment()
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
        top_bar_title.text = "Select a Quiz"
        top_bar_mic.visibility = View.GONE
    }

    fun setUpQuizTopBar() {
        selectedBotTitle?.let {
            top_bar_title.text = it
        }
        back.visibility = View.VISIBLE
        home.visibility = View.GONE
        top_bar_mic.visibility = View.VISIBLE
        back.setOnClickListener { supportFragmentManager.popBackStack() }
    }

    private fun setUpDropdownRecycler() {
        bot_selection_recycler.apply {
            layoutManager = LinearLayoutManager(this@BotQuizSelectionActivity)
        }
        database.child("BotCatalog").addChildEventListener(baseChildEventListener {
            val botImage = it.child("botImage").value.toString()
            val botTitle = it.child("botTitle").value.toString()
            placeBotAlphabetically(botTitle, botImage)
            bot_selection_recycler.adapter = BotQuizAdapter(
                this@BotQuizSelectionActivity,
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

    private fun setUpScrollListener() {
        val layoutManager = bot_selection_recycler.layoutManager as LinearLayoutManager
        bot_selection_recycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItem = layoutManager.findFirstCompletelyVisibleItemPosition()
                val lastVisibleItem = layoutManager.findLastCompletelyVisibleItemPosition()
                if (firstVisibleItem == 0) {
                    bot_selection_up_arrow.visibility = View.INVISIBLE
                } else {
                    bot_selection_up_arrow.visibility = View.VISIBLE
                }
                if (lastVisibleItem == totalItemCount - 1) {
                    bot_selection_down_arrow.visibility = View.INVISIBLE
                } else {
                    bot_selection_down_arrow.visibility = View.VISIBLE
                }
            }
        })
    }

    private fun setUpArrowClicks() {
        val layoutManager = bot_selection_recycler.layoutManager as LinearLayoutManager
        bot_selection_up_arrow.setOnClickListener {
            val firstVisibleItem = layoutManager.findFirstCompletelyVisibleItemPosition()
            val targetPos = if (firstVisibleItem - 3 >= 0) firstVisibleItem - 3 else 0
            bot_selection_recycler.smoothScrollToPosition(targetPos)
        }
        bot_selection_down_arrow.setOnClickListener {
            val lastVisibleItem = layoutManager.findLastCompletelyVisibleItemPosition()
            val totalItemCount = layoutManager.itemCount
            val targetPos =
                if (lastVisibleItem + 3 < totalItemCount) lastVisibleItem + 3 else totalItemCount - 1
            bot_selection_recycler.smoothScrollToPosition(targetPos)
        }
    }
}
