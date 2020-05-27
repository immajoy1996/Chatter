package com.example.chatter

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_language_selection.*
import kotlinx.android.synthetic.main.bottom_nav_bar.*
import kotlinx.android.synthetic.main.top_bar.*

class LanguageSelectionActivity : BaseActivity() {
    private var nations = arrayListOf<String>("Spanish", "French", "German","Arabic")
    private var flagImages =
        arrayListOf<Int>(R.drawable.spanishflag, R.drawable.frenchflag, R.drawable.germanflag,R.drawable.spanishflag)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_language_selection)
        setUpTopBar()
        setUpBottomNavBar()
        setUpDropdownRecycler()
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
        top_bar_title.text = "Language"
        top_bar_mic.visibility = View.GONE
    }

    private fun setUpDropdownRecycler() {
        language_recycler.apply {
            layoutManager = LinearLayoutManager(this@LanguageSelectionActivity)
            adapter = LanguageAdapter(
                this@LanguageSelectionActivity,
                nations,
                flagImages
            )
        }
    }
}
