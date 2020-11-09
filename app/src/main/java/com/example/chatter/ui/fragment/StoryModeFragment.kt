package com.example.chatter.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatter.R
import com.example.chatter.adapters.UserProfileDashboardAdapter
import com.example.chatter.extra.NOTIFICATION_TYPE
import com.example.chatter.extra.NotificationManager
import com.example.chatter.ui.activity.HomeNavActivityLatest
import jp.wasabeef.recyclerview.animators.SlideInDownAnimator
import jp.wasabeef.recyclerview.animators.SlideInLeftAnimator
import jp.wasabeef.recyclerview.animators.SlideInRightAnimator
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_story_mode, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpTabLayout()
    }

    override fun onResume() {
        super.onResume()
        setUpStoryModeNotficationRecycler()
    }

    private fun setUpStoryModeNotficationRecycler() {
        val notificationList =
            NotificationManager.getNotificationList(preferences)
        recycler_view_dashboard_icons.apply {
            layoutManager = LinearLayoutManager(context)
            adapter =
                UserProfileDashboardAdapter(
                    context,
                    notificationList
                )
        }
        /*recycler_view_dashboard_icons.setItemAnimator(SlideInDownAnimator())
        recycler_view_dashboard_icons.setItemAnimator(SlideInRightAnimator())
        recycler_view_dashboard_icons.setItemAnimator(SlideInLeftAnimator())
        recycler_view_dashboard_icons.setItemAnimator(SlideInUpAnimator())*/
    }

    private fun setUpTabLayout() {
        (activity as? HomeNavActivityLatest)?.let {
            it.setUpTabLayout()
        }
    }
}