package com.example.chatter.ui

import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.example.chatter.R
import com.example.chatter.ui.activity.BotStoryActivity
import com.example.chatter.ui.fragment.BaseFragment
import com.example.chatter.ui.fragment.BotStoryFragment
import kotlinx.android.synthetic.main.activity_bot_story.*
import kotlinx.android.synthetic.main.bottom_nav_bar.*
import kotlinx.android.synthetic.main.fragment_bot_story_layout.*

class BotStoryFragmentUsed : BaseFragment() {
    var cardTitle: String? = null
    var cardText: String? = null
    var cardImage: String? = null
    var soundEffect: String? = null
    var order: Int? = null
    private var mediaPlayer = MediaPlayer()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_bot_story_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (cardTitle != null && cardImage != null && cardText != null && order != null) {
            loadViews(cardTitle as String, cardText as String, cardImage as String)
        } else {
            (activity as? BotStoryActivity)?.hideBottomNavBar()
        }
    }

    override fun onResume() {
        super.onResume()
        soundEffect?.let {
            //playSoundEffect(it)
        }
        initializeViews()
        setImagePath()
    }

    override fun onPause() {
        super.onPause()
        stopSoundEffect()
    }

    private fun setImagePath(){
        cardImage?.let {
            (activity as? BotStoryActivity)?.setBotImagePath(it)
        }
    }

    private fun initializeViews() {
        (activity as? BotStoryActivity)?.let {
            it.showToolbar()
            it.showStoryProgressAnimation()
            it.showBottomNavBar()
            it.setUpBottomNavButtons()
            if (order != null) {
                it.setUpBottomNavBar(order as Int)
            }
        }
    }

    private fun playSoundEffect(audio: String) {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop()
            mediaPlayer.release()
        }
        mediaPlayer = MediaPlayer()
        mediaPlayer.setDataSource(audio)
        mediaPlayer.prepare()
        mediaPlayer.isLooping = true
        mediaPlayer.start()
    }

    private fun stopSoundEffect() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
        }
    }

    private fun setUpBottomBar(order: Int) {
        (activity as? BotStoryActivity)?.setUpBottomNavBar(order)
    }

    private fun loadViews(
        cardTitle: String,
        cardText: String,
        image: String
    ) {
        bot_story_title.text = cardTitle
        bot_story_card_text.text = cardText
        context?.let {
            Glide.with(it)
                .load(image)
                .into(bot_story_image)
        }
    }

    companion object {
        fun newInstance(
            cardTitle: String,
            cardText: String,
            cardImage: String,
            soundEffect: String,
            order: Int
        ): BotStoryFragmentUsed {
            val fragment = BotStoryFragmentUsed()
            fragment.cardTitle = cardTitle
            fragment.cardText = cardText
            fragment.cardImage = cardImage
            fragment.soundEffect = soundEffect
            fragment.order = order
            return fragment
        }
    }
}