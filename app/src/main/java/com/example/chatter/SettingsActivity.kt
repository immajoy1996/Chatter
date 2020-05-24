package com.example.chatter

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.android.synthetic.main.password_reset_dots_layout.*
import kotlinx.android.synthetic.main.top_bar.*

class SettingsActivity : BaseActivity() {
    private var settingsOptionsFragment = SettingsOptionsFragment()
    private var passwordResetFragment = PasswordResetFragment()
    var dotPointer: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        setUpTopBar()
        loadSettingsOptions()
    }

    override fun setUpTopBar() {
        top_bar_title.text = "Settings"
        top_bar_mic.visibility = View.GONE
        home.visibility = View.GONE
        back.visibility = View.VISIBLE
        back.setOnClickListener {
            finish()
        }
    }

    fun setDotPointer(count: Int) {
        dotPointer = count
    }

    fun setUpDots() {
        dotPointer?.let {
            when (it) {
                0 -> {
                    circles_layout.visibility = View.VISIBLE
                    circle1.visibility = View.GONE
                    circle1_filled.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun loadSettingsOptions() {
        loadFragment(settingsOptionsFragment)
    }

    fun loadPasswordResetFragment() {
        loadFragment(passwordResetFragment)
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(fragment_container.id, fragment)
            .addToBackStack(fragment.javaClass.name)
            .commit()
    }

    override fun onBackPressed() {
        finish()
    }
}
