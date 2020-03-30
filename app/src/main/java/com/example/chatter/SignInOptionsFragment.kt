package com.example.chatter

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

    private fun setUpButtons() {
        sign_in.setOnClickListener {
            (activity as? SignInActivity)?.let {
                it.setScenario(SIGN_IN)
                it.runMessageFlow(sign_in.text.toString())
            }
        }
        proceed_as_guest.setOnClickListener {
            (activity as? SignInActivity)?.let {
                it.setScenario(PROCEED_AS_GUEST)
                it.signInAsGuest()
            }
        }
    }

    companion object {
        private const val SIGN_IN = 1
        private const val SIGN_UP = 2
        private const val PROCEED_AS_GUEST = 3
    }
}
