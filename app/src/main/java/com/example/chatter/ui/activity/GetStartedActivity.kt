package com.example.chatter.ui.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.chatter.R
import kotlinx.android.synthetic.main.activity_login.*

class GetStartedActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        setUpButtons()
    }

    private fun setUpButtons() {
        get_started_button.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
        }
    }
}
