package com.example.chatter

import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_navigation_drawer.*

class NavigationDrawerFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_navigation_drawer, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpNavigationOptionButtons()
        setUpButtons()
    }

    private fun setUpNavigationOptionButtons() {
        drawer_my_subscription_image.setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_ATOP)
        drawer_settings_image.setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_ATOP)
    }



    private fun setUpButtons() {
        drawer_logout_text.setOnClickListener {
            activity?.finish()
            startActivity(Intent(context, SignInActivity::class.java))
        }
        drawer_close_layout.setOnClickListener {
            fragmentManager?.popBackStack()
        }
        drawer_close_image.setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_ATOP)
    }
}
