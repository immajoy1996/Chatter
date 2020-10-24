package com.example.chatter.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatter.R
import com.example.chatter.adapters.BotGridItemDecoration
import com.example.chatter.adapters.StudyModeAdapter
import com.example.chatter.adapters.UserProfileDashboardAdapter
import com.example.chatter.ui.activity.HomeNavActivityLatest
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_user_profile.*

class StudyModeFragment : BaseFragment() {
    var imageList = arrayListOf(
        R.drawable.chat_box,
        R.drawable.quiz_icon,
        R.drawable.flashcards,
        R.drawable.settings_new
    )
    var titleList = arrayListOf("Chats", "Games", "Flashcards", "Settings")
    var subtitleList =
        arrayListOf("Talk to bots", "Learn and have fun!", "Practice vocab", "All about you!")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_user_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpBackButton()
        setUpStudyModeRecycler()
        setUpTabLayout()
    }

    private fun setUpTabLayout() {
        (activity as? HomeNavActivityLatest)?.let {
            it.setUpTabLayout()
        }
    }

    private fun setUpBackButton() {
        study_mode_back_button.setOnClickListener {
            activity?.finish()
        }
    }

    private fun setUpStudyModeRecycler() {
        study_mode_recycler.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = StudyModeAdapter(context, imageList, titleList, subtitleList)
            //addItemDecoration(BotGridItemDecoration(0,20))
        }
    }
}