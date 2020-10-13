package com.example.chatter.ui.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatter.R
import com.example.chatter.adapters.BotAdapter
import com.example.chatter.adapters.SmallButtonsAdapter
import kotlinx.android.synthetic.main.activity_dashboard.*
import kotlinx.android.synthetic.main.activity_home_nav_used.*

class HomeNavActivityUsed : BaseActivity() {
    private var imageList = arrayListOf<Int>(
        R.drawable.chat_box,
        R.drawable.news_report,
        R.drawable.grammar,
        R.drawable.american_football_field,
        R.drawable.king,
        R.drawable.setting_dashboard
    )
    //chat bots, levels, settings, culture content, grammar, newsreel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_nav_used)
        setUpButtons()
        setUpRecycler()
    }

    private fun setUpButtons() {
        story_mode_button_layout.setOnDebouncedClickListener {
            val intent = Intent(this, BotStoryActivity::class.java)
            intent.putExtra("botStoryTitle", preferences.getCurrentBotStory())
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
        signout_bar.setOnDebouncedClickListener {
            val intent = Intent(this, SignInActivity::class.java)
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    private fun setUpRecycler() {
        smaller_buttons_recycler.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        val buttonsAdapter = SmallButtonsAdapter(
            this,
            imageList
        )
        smaller_buttons_recycler.adapter = buttonsAdapter
    }

    override fun setUpTopBar() {
        //TODO("Not yet implemented")
    }
}