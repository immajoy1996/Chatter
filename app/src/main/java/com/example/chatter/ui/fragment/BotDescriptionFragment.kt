package com.example.chatter.ui.fragment

import ProgressBarAnimation
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.chatter.R
import kotlinx.android.synthetic.main.fragment_bot_description.*


class BotDescriptionFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_bot_description, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpExitButton()
        val botDifficulty: Float = 70f
        val anim = ProgressBarAnimation(bot_difficulty_meter, 0.0f, botDifficulty)
        anim.duration = 1000
        bot_difficulty_meter.startAnimation(anim)
    }

    private fun setUpExitButton() {
        bot_description_exit_button.setOnClickListener {
            fragmentManager?.popBackStack()
        }
    }
}
