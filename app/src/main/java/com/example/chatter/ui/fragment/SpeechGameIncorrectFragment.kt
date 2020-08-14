package com.example.chatter.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.chatter.R
import com.example.chatter.ui.SpeechGameActivity
import kotlinx.android.synthetic.main.fragment_speech_game_correct.*
import kotlinx.android.synthetic.main.fragment_speech_game_incorrect_game.*

class SpeechGameIncorrectFragment : BaseFragment() {
    private var correctText = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_speech_game_incorrect_game, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showCorrectSentence()
        setUpContinueButton()
    }

    private fun showCorrectSentence() {
        speech_game_incorrect_explanation.text = correctText
    }

    private fun setUpContinueButton() {
        speech_game_incorrect_continue_button.setOnDebouncedClickListener {
            fragmentManager?.popBackStack()
            (activity as? SpeechGameActivity)?.startNextSentence()
        }
    }

    companion object {
        fun newInstance(text: String): SpeechGameIncorrectFragment {
            val fragment = SpeechGameIncorrectFragment()
            fragment.correctText = "\'$text\'"
            return fragment
        }
    }
}