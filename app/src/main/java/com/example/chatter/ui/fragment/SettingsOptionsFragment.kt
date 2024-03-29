package com.example.chatter.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.chatter.R
import com.example.chatter.ui.activity.SettingsActivity
import kotlinx.android.synthetic.main.fragment_settings_options.*
import kotlinx.android.synthetic.main.top_bar.*

class SettingsOptionsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings_options, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpTopBar()
        setUpButtons()
    }

    private fun setUpButtons() {
        settings_options_reset_password.setOnClickListener {
            (activity as? SettingsActivity)?.loadPasswordResetFragment()
        }
    }

    private fun setUpTopBar() {
        (activity as? SettingsActivity)?.back?.setOnClickListener {
            activity?.finish()
        }
    }
}
