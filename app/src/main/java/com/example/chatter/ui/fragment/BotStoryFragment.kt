package com.example.chatter.ui.fragment

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.bumptech.glide.Glide
import com.example.chatter.R
import com.example.chatter.extra.MyBounceInterpolator
import com.example.chatter.ui.activity.BaseChatActivity
import com.example.chatter.ui.activity.ChatterActivity
import com.example.chatter.ui.activity.DashboardActivity
import com.example.chatter.ui.activity.DashboardActivity.Companion.BOT_TITLE
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.bottom_nav_bar.*
import kotlinx.android.synthetic.main.fragment_bot_story.*
import kotlinx.android.synthetic.main.top_bar.*

class BotStoryFragment : BaseFragment() {

    private lateinit var database: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_bot_story, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        database = FirebaseDatabase.getInstance().reference
        setUpTopBar()
        setUpBottomNavBar()
        setUpViews()
        setUpBotImageAnimation()
    }

    private fun setUpViews() {
        val botTitle = activity?.intent?.getStringExtra(BOT_TITLE)
        botTitle?.let {
            setUpBotTitle(it)
        }
        botTitle?.let {
            val botListener = baseValueEventListener { dataSnapshot ->
                val level = dataSnapshot.child("level").value
                val desc = dataSnapshot.child("botDescription").value
                val image = dataSnapshot.child("botImage").value
                if (context != null && (activity as? BaseChatActivity)?.canConnectToInternet(context as Context) == true) {
                    level?.let {
                        setUpLevel(it.toString())
                    }
                    desc?.let {
                        setUpBotDescription(it.toString())
                    }
                    image?.let {
                        setUpBotImage(it.toString())
                    }
                }
            }
            database.child("BotCatalog").child(it).addListenerForSingleValueEvent(botListener)
        }
    }

    private fun setUpLevel(level: String) {
        bot_story_bot_level.text = level
    }

    private fun setUpBotDescription(botDesc: String) {
        bot_story_bot_desc.text = botDesc
    }

    private fun setUpBotImage(image: String) {
        context?.let {
            Glide.with(it)
                .load(image)
                .into(bot_story_bot_image)
        }
    }

    private fun setUpBotTitle(title: String) {
        bot_story_bot_name.text = title
    }


    private fun setUpTopBar() {
        home.visibility = View.GONE
        back.visibility = View.VISIBLE
        back.setOnClickListener {
            activity?.finish()
        }
        top_bar_mic.visibility = View.GONE
        top_bar_quiz.visibility = View.GONE
        top_bar_jokebook.visibility = View.GONE
        top_bar_title.visibility = View.VISIBLE
        top_bar_title.text = "Story"
    }

    private fun setUpBottomNavBar() {
        button_back.visibility = View.GONE
        button_next.visibility = View.GONE
        button_start.visibility = View.VISIBLE
        button_start.setOnClickListener {
            fragmentManager?.popBackStack()
            (activity as? ChatterActivity)?.onStoriesFinished()
        }
    }

    private fun setUpBotImageAnimation() {
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