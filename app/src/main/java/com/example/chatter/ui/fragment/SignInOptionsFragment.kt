package com.example.chatter.ui.fragment

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.example.chatter.R
import com.example.chatter.ui.activity.HomeNavigationActivity
import com.example.chatter.ui.activity.SignInActivity
import kotlinx.android.synthetic.main.fragment_sign_in_options.*

class SignInOptionsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sign_in_options, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpButtons()
    }

    override fun onResume() {
        super.onResume()
        resetProceedAsGuestColor()
    }

    private fun resetProceedAsGuestColor() {
        context?.let {
            proceed_as_guest.setTextColor(Color.parseColor("#000000"))
        }
    }

    private fun setUpButtons() {
        sign_in.setOnClickListener {
            (activity as? SignInActivity)?.let {
                if (it.auth.currentUser != null) {
                    //already signed in
                    it.loginAutomatically()
                } else {
                    it.setScenario(SIGN_IN)
                    it.setUserAndBotResponseArrays()
                    it.runMessageFlow(sign_in.text.toString())
                }
            }
        }
        sign_up.setOnClickListener {
            (activity as? SignInActivity)?.let {
                it.runMessageFlowSignUp(sign_up.text.toString())
            }
        }
        proceed_as_guest.setOnClickListener {
            (activity as? SignInActivity)?.let {
                it.setScenario(PROCEED_AS_GUEST)
                it.signInAsGuest()
                context?.let {
                    proceed_as_guest.setTextColor(Color.parseColor("#0084FF"))
                }
            }
        }
    }

    companion object {
        const val SIGN_IN = 1
        const val PROCEED_AS_GUEST = 3
    }
}
