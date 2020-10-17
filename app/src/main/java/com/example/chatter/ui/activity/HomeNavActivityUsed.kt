package com.example.chatter.ui.activity

import android.content.Intent
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatter.R
import com.example.chatter.adapters.BotAdapter
import com.example.chatter.adapters.SmallButtonsAdapter
import com.example.chatter.interfaces.SmallIconClickInterface
import com.example.chatter.ui.fragment.BotLevelsFragment
import kotlinx.android.synthetic.main.activity_bot_story.*
import kotlinx.android.synthetic.main.activity_dashboard.*
import kotlinx.android.synthetic.main.activity_home_nav_used.*

class HomeNavActivityUsed : BaseActivity(), SmallIconClickInterface {
    private var imageList1 = arrayListOf<Int>(
        R.drawable.setting_dashboard,
        R.drawable.chat_box,
        R.drawable.homeless
    )
    private var imageList2 = arrayListOf<Int>(
        R.drawable.news,
        R.drawable.grammar
    )

    private var possibleLevelsFragment = BotLevelsFragment()
    //chat bots, levels, settings, culture content, grammar, newsreel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_nav_used)
        setUpButtons()
        setUpRecycler()
        setUpDismissMyLevelPopup()
    }

    private fun setUpDismissMyLevelPopup(){
        home_nav_used_root_layout.setOnClickListener {
            dismissLevelPopupIfShown()
        }
    }

    private fun dismissLevelPopupIfShown(){
        if(possibleLevelsFragment.isVisible){
            supportFragmentManager.popBackStack()
        }
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
        smaller_buttons_recycler1.layoutManager =
            GridLayoutManager(this, 3)
        smaller_buttons_recycler2.layoutManager =
            GridLayoutManager(this, 2)

        val buttonsAdapter1 = SmallButtonsAdapter(
            this,
            imageList1,
            this
        )
        val buttonsAdapter2 = SmallButtonsAdapter(
            this,
            imageList2,
            this
        )
        smaller_buttons_recycler1.adapter = buttonsAdapter1
        smaller_buttons_recycler2.adapter = buttonsAdapter2
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(R.anim.slide_right, R.anim.slide_left)
            .replace(home_nav_used_root_layout.id, fragment)
            .addToBackStack(fragment.javaClass.name)
            .commit()
    }

    override fun onSmallIconClick(id: Int) {
        when (id) {
            R.drawable.homeless -> {
                loadFragment(possibleLevelsFragment)
            }
            else -> {
                Toast.makeText(this, "This button hasn't been implemented", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    override fun setUpTopBar() {
        //TODO("Not yet implemented")
    }
}