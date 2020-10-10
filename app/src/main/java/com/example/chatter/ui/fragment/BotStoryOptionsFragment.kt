package com.example.chatter.ui.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.chatter.R
import com.example.chatter.ui.activity.BotStoryActivity
import com.example.chatter.ui.activity.DashboardActivity
import kotlinx.android.synthetic.main.fragment_bot_story_options.*

class BotStoryOptionsFragment : BaseFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_bot_story_options, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpOptionsButtons()
    }

    private fun setUpOptionsButtons() {
        bot_story_optionA.setOnDebouncedClickListener {
            //Study Mode
            //(activity as? BotStoryActivity)?.dismissMenuOptionsPopup()
            val intent = Intent(context, DashboardActivity::class.java)
            //intent.putExtra("GUEST_MODE", true)
            startActivity(intent)
        }
        bot_story_optionB.setOnDebouncedClickListener {
            //todo impl newsreel
        }
    }
}