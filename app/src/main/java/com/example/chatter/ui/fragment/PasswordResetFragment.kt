package com.example.chatter.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.chatter.R
import com.example.chatter.ui.activity.SettingsActivity
import kotlinx.android.synthetic.main.top_bar.*

class PasswordResetFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_password_reset, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpTopBar()
        setUpDots()
    }

    private fun setUpTopBar() {
        (activity as? SettingsActivity)?.back?.setOnClickListener {
            fragmentManager?.popBackStack()
        }
    }

    private fun setUpDots() {
        (activity as? SettingsActivity)?.let {
            it.setDotPointer(0)
            it.setUpDots()
        }
    }
}
