package com.example.chatter

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_language_selection.*
import kotlinx.android.synthetic.main.bottom_nav_bar.*
import kotlinx.android.synthetic.main.top_bar.*

class LanguageSelectionActivity : BaseSelectionActivity() {
    private var nations = arrayListOf<String>(
        "Spanish",
        "French",
        "German",
        "Arabic",
        "Mandarin",
        "Hebrew",
        "Dutch",
        "Hindi",
        "Russian"
    )
    private var flagImages =
        arrayListOf<Int>(
            R.drawable.spanishflag,
            R.drawable.frenchflag,
            R.drawable.germanflag,
            R.drawable.spanishflag,
            R.drawable.spanishflag,
            R.drawable.frenchflag,
            R.drawable.germanflag,
            R.drawable.spanishflag,
            R.drawable.spanishflag
        )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_language_selection)
        setUpTopBar()
        setUpBottomNavBar()
        setUpDropdownRecycler()
        setUpScrollListener()
        setUpArrowClicks()
    }

    private fun setUpBottomNavBar() {
        button_back.setOnClickListener {
            this.finish()
        }
        button_next.setOnClickListener {
            val intent = Intent(this, DashboardActivity::class.java)
            intent.putExtra("GUEST_MODE", true)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        }
    }

    override fun setUpTopBar() {
        setUpTopAndBottomBars()
    }

    private fun setUpTopAndBottomBars() {
        if (intent.extras?.containsKey("ChangingDefaultLanguage") == true) {
            top_bar_title.text = "Language"
            top_bar_mic.visibility = View.GONE
            home.visibility = View.GONE
            back.visibility = View.VISIBLE
            top_bar_save_button.visibility = View.VISIBLE
            language_selection_bottom_bar.visibility = View.GONE
            back.setOnClickListener {
                finish()
            }
        }
        else{
            top_bar_title.text = "Language"
            top_bar_mic.visibility = View.GONE
        }
    }

    override fun setUpDropdownRecycler() {
        language_recycler.apply {
            layoutManager = LinearLayoutManager(this@LanguageSelectionActivity)
            adapter = LanguageAdapter(
                this@LanguageSelectionActivity,
                nations,
                flagImages
            )
        }
    }

    override fun setUpScrollListener() {
        val layoutManager = language_recycler.layoutManager as LinearLayoutManager
        language_recycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItem = layoutManager.findFirstCompletelyVisibleItemPosition()
                val lastVisibleItem = layoutManager.findLastCompletelyVisibleItemPosition()
                if (firstVisibleItem == 0) {
                    language_selection_up_arrow.visibility = View.INVISIBLE
                } else {
                    language_selection_up_arrow.visibility = View.VISIBLE
                }
                if (lastVisibleItem == totalItemCount - 1) {
                    language_selection_down_arrow.visibility = View.INVISIBLE
                } else {
                    language_selection_down_arrow.visibility = View.VISIBLE
                }
            }
        })
    }

    override fun setUpArrowClicks() {
        val layoutManager = language_recycler.layoutManager as LinearLayoutManager
        language_selection_up_arrow.setOnClickListener {
            val firstVisibleItem = layoutManager.findFirstCompletelyVisibleItemPosition()
            val targetPos = if (firstVisibleItem - 3 >= 0) firstVisibleItem - 3 else 0
            language_recycler.smoothScrollToPosition(targetPos)
        }
        language_selection_down_arrow.setOnClickListener {
            val lastVisibleItem = layoutManager.findLastCompletelyVisibleItemPosition()
            val totalItemCount = layoutManager.itemCount
            val targetPos =
                if (lastVisibleItem + 3 < totalItemCount) lastVisibleItem + 3 else totalItemCount - 1
            language_recycler.smoothScrollToPosition(targetPos)
        }
    }
}
