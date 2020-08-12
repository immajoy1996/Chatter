package com.example.chatter.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.chatter.R
import com.example.chatter.ui.activity.JokesActivity
import kotlinx.android.synthetic.main.fragment_jokes_explanation.*
import kotlinx.android.synthetic.main.fragment_story_board_one.*

class JokesExplanationFragment : BaseFragment() {
    var jokeExplanationText: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_jokes_explanation, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpJokeExplanation()
        setUpContinueButton()
    }

    private fun setUpJokeExplanation() {
        jokeExplanationText?.let {
            jokes_explanation_desc.text = it
        }
    }

    private fun setUpContinueButton() {
        jokes_explanation_continue_button.setOnDebouncedClickListener {
            (activity as? JokesActivity)?.let {
                it.showNextJoke()
                it.showTheShowAnswerButton()
            }
            fragmentManager?.popBackStack()
        }
    }

    companion object {
        fun newInstance(text: String): JokesExplanationFragment {
            val fragment = JokesExplanationFragment()
            fragment.jokeExplanationText = text
            return fragment
        }
    }
}