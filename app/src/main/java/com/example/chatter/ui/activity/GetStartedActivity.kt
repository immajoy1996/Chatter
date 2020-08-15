package com.example.chatter.ui.activity

import android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        auth = FirebaseAuth.getInstance()
        showBlinkingDots()
        typeWelcomeMesssage()
        typeSecondWelcomeMessage()
        setUpButtons()
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

    private fun requestRecordAudioPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.RECORD_AUDIO),
            PERMISSION_REQUEST_CODE
        )
    }

    private fun recordAudioPermissionGranted(): Boolean {
        return if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            false
        } else true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(
                        this,
                        "Permission Granted",
                        Toast.LENGTH_SHORT
                    ).show();
                    // main logic
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                            != PackageManager.PERMISSION_GRANTED
                        ) {
                            showMessageOKCancel("You need to allow access permissions",
                                DialogInterface.OnClickListener { dialog, which ->
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        requestRecordAudioPermission()
                                    }
                                })
                        }
                    }
                }
            }
        }
    }

    private fun showMessageOKCancel(
        message: String,
        okListener: DialogInterface.OnClickListener
    ) {
        AlertDialog.Builder(this)
            .setMessage(message)
            .setPositiveButton("OK", okListener)
            .setNegativeButton("Cancel", null)
            .create()
            .show()
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

    override fun setUpTopBar() {
        //TODO("Not yet implemented")
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 500
    }
}
