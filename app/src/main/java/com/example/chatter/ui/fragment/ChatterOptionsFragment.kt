package com.example.chatter.ui.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.chatter.R
import com.example.chatter.ui.activity.ChatterActivity
import com.example.chatter.ui.activity.DashboardActivity
import com.example.chatter.ui.activity.GamesActivity
import kotlinx.android.synthetic.main.fragment_bot_story_options.*
import kotlinx.android.synthetic.main.fragment_bot_story_options.bot_story_optionA
import kotlinx.android.synthetic.main.fragment_bot_story_options.bot_story_optionB
import kotlinx.android.synthetic.main.fragment_chatter_options.*

class ChatterOptionsFragment : BaseFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_chatter_options, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpOptionsButtons()
    }

    private fun setUpOptionsButtons() {
        //Practice (Games)
        bot_story_optionA.setOnDebouncedClickListener {
            //val gameType = (activity as? ChatterActivity)?.getGameType()
            val botTitle = (activity as? ChatterActivity)?.getCurrentBotTitle()
            if (botTitle?.isNotEmpty() == true) {
                val intent = Intent(context, GamesActivity::class.java)
                intent.putExtra("botTitle", botTitle)
                startActivity(intent)
            }
        }
        //Vocab and Flashcards
        bot_story_optionB.setOnDebouncedClickListener {
            (activity as? ChatterActivity)?.loadVocabFragment()
        }
        bot_story_optionC.setOnDebouncedClickListener {
            val gameType = (activity as? ChatterActivity)?.getGameType()
            val botTitle = (activity as? ChatterActivity)?.getCurrentBotTitle()
            if (gameType?.isNotEmpty() == true && botTitle?.isNotEmpty() == true) {
                val intent = Intent(context, GamesActivity::class.java)
                intent.putExtra("gameType", gameType)
                intent.putExtra("botTitle", botTitle)
                intent.putExtra("shouldShowGameOptions", false)
                startActivity(intent)
            }
        }
    }
}