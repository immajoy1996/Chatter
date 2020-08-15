package com.example.chatter.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.chatter.R
import com.example.chatter.ui.activity.SpeechGameActivity
import kotlinx.android.synthetic.main.fragment_speech_game_correct.*

class SpeechGameCorrectFragment : BaseFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_speech_game_correct, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpContinueButton()
    }

    private fun setUpContinueButton(){
        speech_game_continue_button.setOnDebouncedClickListener {
            fragmentManager?.popBackStack()
            (activity as? SpeechGameActivity)?.startNextSentence()
        }
    }
}