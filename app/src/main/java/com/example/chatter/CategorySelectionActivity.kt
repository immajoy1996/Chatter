package com.example.chatter

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatter.DashboardActivity.Companion.CATEGORY_REQUEST_CODE
import kotlinx.android.synthetic.main.activity_category_selection.*
import kotlinx.android.synthetic.main.bottom_nav_bar.*
import kotlinx.android.synthetic.main.fragment_settings_options.*
import kotlinx.android.synthetic.main.top_bar.*

class CategorySelectionActivity : BaseActivity(), CategorySelectionInterface {
    private var categories = arrayListOf<String>(
        "All Bots",
        "Funny",
        "Day-to-Day",
        "Famous People",
        "Politics",
        "Quirky",
        "Sporty"
    )
    private var categoryImages =
        arrayListOf<Int>(
            R.drawable.earth,
            R.drawable.funny,
            R.drawable.house,
            R.drawable.spanishflag,
            R.drawable.spanishflag,
            R.drawable.frenchflag,
            R.drawable.germanflag,
            R.drawable.spanishflag,
            R.drawable.spanishflag
        )

    private var selectedCategory: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category_selection)
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
        top_bar_title.text = "Categories"
        top_bar_mic.visibility = View.GONE
        home.visibility = View.GONE
        back.visibility = View.VISIBLE
        top_bar_save_button.visibility = View.VISIBLE
        category_selection_bottom_bar.visibility = View.GONE
        top_bar_save_button.setOnClickListener {
            val intent = Intent()
            if (selectedCategory != null) {
                selectedCategory?.let {
                    intent.putExtra("SelectedCategory", it)
                    setResult(CATEGORY_REQUEST_CODE, intent)
                    Toast.makeText(this, "Selection saved", Toast.LENGTH_LONG).show()
                    finish()
                }
            } else {
                Toast.makeText(this, "Select a Category", Toast.LENGTH_LONG).show()
            }
        }
        back.setOnClickListener {
            finish()
        }
    }

    private fun setUpDropdownRecycler() {
        category_recycler.apply {
            layoutManager = LinearLayoutManager(this@CategorySelectionActivity)
            adapter = LanguageAdapter(
                this@CategorySelectionActivity,
                categories,
                categoryImages,
                null,
                this@CategorySelectionActivity
            )
        }
    }

    private fun setUpScrollListener() {
        val layoutManager = category_recycler.layoutManager as LinearLayoutManager
        category_recycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItem = layoutManager.findFirstCompletelyVisibleItemPosition()
                val lastVisibleItem = layoutManager.findLastCompletelyVisibleItemPosition()
                if (firstVisibleItem == 0) {
                    category_selection_up_arrow.visibility = View.INVISIBLE
                } else {
                    category_selection_up_arrow.visibility = View.VISIBLE
                }
                if (lastVisibleItem == totalItemCount - 1) {
                    category_selection_down_arrow.visibility = View.INVISIBLE
                } else {
                    category_selection_down_arrow.visibility = View.VISIBLE
                }
            }
        })
    }

    private fun setUpArrowClicks() {
        val layoutManager = category_recycler.layoutManager as LinearLayoutManager
        category_selection_up_arrow.setOnClickListener {
            val firstVisibleItem = layoutManager.findFirstCompletelyVisibleItemPosition()
            val targetPos = if (firstVisibleItem - 3 >= 0) firstVisibleItem - 3 else 0
            category_recycler.smoothScrollToPosition(targetPos)
        }
        category_selection_down_arrow.setOnClickListener {
            val lastVisibleItem = layoutManager.findLastCompletelyVisibleItemPosition()
            val totalItemCount = layoutManager.itemCount
            val targetPos =
                if (lastVisibleItem + 3 < totalItemCount) lastVisibleItem + 3 else totalItemCount - 1
            category_recycler.smoothScrollToPosition(targetPos)
        }
    }

    override fun onCategorySelected(category: String) {
        selectedCategory = category
    }
}
