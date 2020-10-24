package com.example.chatter.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatter.R
import com.example.chatter.adapters.UserProfileDashboardAdapter
import com.example.chatter.extra.NOTIFICATION_TYPE
import com.example.chatter.ui.activity.HomeNavActivityLatest
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_story_mode.*

class StoryModeFragment : BaseFragment() {

    var notificationType = arrayListOf(
        NOTIFICATION_TYPE.QUIZ,
        NOTIFICATION_TYPE.STUDYMODE,
        NOTIFICATION_TYPE.LEVEL_REACHED
    )
    var imageList = arrayListOf(R.drawable.quiz_icon, R.drawable.hotel, R.drawable.homeless)
    var subtitleList = arrayListOf("Doctor Hum-Vee", "Doctor Hum-Vee", "Language Level : Homeless")
    var locationNameList = arrayListOf("Test your understanding", "Edward's Hospital, NYC", "")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_story_mode, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpTabLayout()
        setUpStoryModeNotficationRecycler()
    }

    private fun setUpStoryModeNotficationRecycler() {
        recycler_view_dashboard_icons.apply {
            layoutManager = LinearLayoutManager(context)
            adapter =
                UserProfileDashboardAdapter(
                    context,
                    notificationType,
                    imageList,
                    subtitleList,
                    locationNameList
                )
        }
    }

    private fun setUpTabLayout() {
        (activity as? HomeNavActivityLatest)?.let {
            it.setUpTabLayout()
        }
    }
}