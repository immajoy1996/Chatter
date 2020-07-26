package com.example.chatter.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.example.chatter.R
import com.example.chatter.extra.MyBounceInterpolator
import com.example.chatter.ui.activity.ChatterActivity
import com.example.chatter.ui.activity.DashboardActivity
import kotlinx.android.synthetic.main.bottom_nav_bar.*
import kotlinx.android.synthetic.main.fragment_bot_story.*
import kotlinx.android.synthetic.main.top_bar.*

class BotStoryFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_bot_story, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpTopBar()
        setUpBottomNavBar()
        setUpViews()
    }

    private fun setUpTopBar() {
        home.visibility = View.VISIBLE
        top_bar_mic.visibility = View.GONE
        top_bar_quiz.visibility = View.GONE
        top_bar_jokebook.visibility = View.GONE
        top_bar_title.visibility = View.VISIBLE
        top_bar_title.text = "Story"
    }

    private fun setUpBottomNavBar() {
        button_back.setOnClickListener {
            activity?.finish()
        }
        button_next.text = "Start"
        button_next.setOnClickListener {
            fragmentManager?.popBackStack()
            (activity as? ChatterActivity)?.onStoriesFinished()
        }
    }

    private fun setUpViews() {
        val wobbleX =
            AnimationUtils.loadAnimation(context, R.anim.wobble_forever_xaxis)
        val wobbleY =
            AnimationUtils.loadAnimation(context, R.anim.wobble_forever_yaxis)
        wobbleX.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(arg0: Animation) {}
            override fun onAnimationRepeat(arg0: Animation) {}
            override fun onAnimationEnd(arg0: Animation) {
                bot_story_bot_image.startAnimation(wobbleY)
            }
        })
        wobbleY.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(arg0: Animation) {}
            override fun onAnimationRepeat(arg0: Animation) {}
            override fun onAnimationEnd(arg0: Animation) {
                bot_story_bot_image.startAnimation(wobbleX)
            }
        })
        bot_story_bot_image.startAnimation(wobbleX)
    }

}