package com.example.chatter

import android.content.Context
import java.util.*
import kotlin.concurrent.schedule

class SignInPresenter(val context: Context) {
    private val signInActivity by lazy { context as SignInActivity }

    fun setUpTimer(name: String, delay: Long): TimerTask {
        return Timer(name, false).schedule(delay) {
            signInActivity.runOnUiThread {
                signInActivity.addSpaceText()
            }
        }
    }
}