package com.example.chatter.ui.activity

import android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.chatter.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class GetStartedActivity : BaseActivity() {
    private lateinit var auth: FirebaseAuth
    private var mediaPlayer = MediaPlayer()
    private var landingSequenceStarted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        auth = FirebaseAuth.getInstance()
        setUpButtons()
        showBlinkingDots()
    }

    private fun showLandingPageSequence() {
        typeWelcomeMesssage()
        typeSecondWelcomeMessage()
    }

    override fun onResume() {
        super.onResume()
        //playTypewriterSound()
        if (!landingSequenceStarted) {
            landingSequenceStarted = true
            showLandingPageSequence()
        }
    }

    private fun playTypewriterSound() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop()
            mediaPlayer.release()
        }
        mediaPlayer = MediaPlayer.create(this, R.raw.typewriter_sound)
        mediaPlayer.isLooping = true
        mediaPlayer.start()
    }

    private fun stopTypewriterSound() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
        }
    }

    private fun showBlinkingDots() {
        three_blinking_dots.visibility = View.VISIBLE
        showThreeBlinkingDotsAnimation(three_blinking_dots, 250, "...")
    }

    private fun hideBlinkingDots() {
        three_blinking_dots.visibility = View.GONE
    }

    private fun typeWelcomeMesssage() {
        setTimerTask("appNameTimer", 3000, {
            hideBlinkingDots()
            showTypingAnimation(app_name, 200, "Chatter")
        })

    }

    private fun typeSecondWelcomeMessage() {
        setTimerTask("appDescriptionTimer", 5000, {
            showTypingAnimation(textView, 100, "Speak like an American!")
        })
    }

    private fun setUpButtons() {
        get_started_button.setOnDebouncedClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
        }
    }

    private fun showTypingAnimation(textView: TextView, delay: Long, message: String) {
        val handler = Handler()
        var pos = 0
        var numDots = 0
        val characterAdder: Runnable = object : Runnable {
            override fun run() {
                val setStr = message.subSequence(0, pos + 1)
                var dotString = ""
                for (i in 1..numDots) {
                    dotString = "${dotString}"
                }

                if (pos < message.length - 1) {
                    numDots = (numDots + 1) % 3
                    textView.setText("${setStr}${dotString}")
                } else {
                    textView.setText(setStr)
                }
                pos++
                if (pos == message.length) {
                    handler.removeCallbacksAndMessages(null)
                } else {
                    handler.postDelayed(this, delay)
                }
            }
        }

        handler.removeCallbacks(characterAdder);
        handler.postDelayed(characterAdder, delay)
    }

    private fun showThreeBlinkingDotsAnimation(textView: TextView, delay: Long, message: String) {
        val handler = Handler()
        var pos = 0
        var numDots = 0
        val characterAdder: Runnable = object : Runnable {
            override fun run() {
                val setStr = message.subSequence(0, pos + 1)
                var dotString = ""
                for (i in 1..numDots) {
                    dotString = "${dotString}"
                }

                if (pos < message.length - 1) {
                    numDots = (numDots + 1) % 3
                    textView.setText("${setStr}${dotString}")
                } else {
                    textView.setText(setStr)
                }
                pos++
                if (pos == message.length) {
                    //handler.removeCallbacksAndMessages(null)
                    pos = 0
                }
                handler.postDelayed(this, delay)

            }
        }

        handler.removeCallbacks(characterAdder);
        handler.postDelayed(characterAdder, delay)
    }

    private fun showFinalTextFields() {
        hideBlinkingDots()
        app_name.text = "Chatter"
        textView.text = "Speak like an American!"
    }

    override fun onPause() {
        super.onPause()
        //stopTypewriterSound()
        if (landingSequenceStarted) {
            showFinalTextFields()
        }
    }

    override fun setUpTopBar() {
        //TODO("Not yet implemented")
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 500
    }
}
