package com.example.chatter

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_enter_pin.*
import kotlinx.android.synthetic.main.fragment_enter_pin.restart

class EnterPinFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_enter_pin, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpButtons()
        setUpPinView()
    }

    private fun setUpPinView() {
        firstPinView.setOnClickListener {
            setUpPinErrorView(false)
        }
    }

    private fun setUpButtons() {
        validate_button.setOnClickListener {
            val pin = (activity as? SignInActivity)?.pin
            pin?.let {
                checkPinAndSignIn(firstPinView.text.toString(), pin)
            }
        }
        restart.setOnClickListener {
            (activity as? SignInActivity)?.refreshSignInFlow()
        }
        resend_pin.setOnClickListener {
            val email = (activity as? SignInActivity)?.username
            email?.let {
                (activity as? SignInActivity)?.sendEmail(it)
            }
        }
    }

    private fun isUserInputPinCorrect(userInput: String, correctPin: String): Boolean {
        return userInput == correctPin
    }

    private fun setUpPinErrorView(isVisible: Boolean) {
        if (isVisible) pin_error.visibility = View.VISIBLE
        else pin_error.visibility = View.GONE
    }

    private fun checkPinAndSignIn(userInput: String, correctPin: String) {
        if (isUserInputPinCorrect(userInput, correctPin)) {
            (activity as? SignInActivity)?.signUpNewUser()
        } else {
            setUpPinErrorView(true)
        }
    }
}
