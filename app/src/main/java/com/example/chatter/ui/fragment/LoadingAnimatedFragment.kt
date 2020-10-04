package com.example.chatter.ui.fragment

import android.media.MediaPlayer
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.chatter.R
import kotlinx.android.synthetic.main.fragment_loading_animated.*

class LoadingAnimatedFragment : BaseFragment() {
    private var message: String? = null
    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_loading_animated, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mediaPlayer = MediaPlayer.create(context, R.raw.typewriter_sound)
        setUpLoadingMessage()
    }

    override fun onResume() {
        super.onResume()
        //playTypewriterSound()
    }

    override fun onPause() {
        super.onPause()
        //stopTypewriterSound()
    }

    private fun setUpLoadingMessage() {
        message?.let {
            loading_typewriter_message.text = it
        }
    }

    private fun playTypewriterSound() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop()
            mediaPlayer.release()
        }
        mediaPlayer = MediaPlayer.create(context, R.raw.typewriter_sound)
        mediaPlayer.isLooping = true
        mediaPlayer.start()
    }

    private fun stopTypewriterSound() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
        }
    }

    companion object {
        fun newInstance(msg: String): LoadingAnimatedFragment {
            val fragment = LoadingAnimatedFragment()
            fragment.message = msg
            return fragment
        }
    }
}