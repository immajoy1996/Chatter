package com.example.chatter.ui.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.chatter.R
import com.example.chatter.ui.activity.BotStoryActivity
import com.example.chatter.ui.activity.ChatterActivity
import kotlinx.android.synthetic.main.fragment_begin_your_journey.*

class BeginYourJourneyFragment : BaseFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_begin_your_journey, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpButtons()
    }

    private fun setUpButtons() {
        begin_your_journey_button.setOnDebouncedClickListener {
            (activity as? BotStoryActivity)?.let {
                it.resetBeginYourJourneyFlag()
                it.setUpFirstStoryScreen()
                it.weDontWantToSeeBeginYourJourneyScreenAgain()
            }
        }
        begin_your_journey_back.setOnDebouncedClickListener {
            activity?.finish()
        }
    }
}