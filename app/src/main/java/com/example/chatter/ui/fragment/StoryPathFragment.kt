package com.example.chatter.ui.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.chatter.R
import com.example.chatter.data.StoryPathInfo
import com.example.chatter.ui.activity.BotStoryActivityLatest
import kotlinx.android.synthetic.main.fragment_story_path.*

class StoryPathFragment : BaseFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_story_path, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpViews()
    }

    fun setUpViews() {
        bot_image1.setOnClickListener {
            val intent = Intent(context, BotStoryActivityLatest::class.java)
            intent.putExtra("botStoryTitle", "Taxi Pete")
            startActivity(intent)
        }
    }

    companion object {
        fun newInstance(storyArray: ArrayList<StoryPathInfo>): StoryPathFragment {
            val fragment = StoryPathFragment()
            return fragment
        }
    }

}