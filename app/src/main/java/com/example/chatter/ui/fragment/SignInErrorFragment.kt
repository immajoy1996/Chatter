package com.example.chatter.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.chatter.R
import com.example.chatter.ui.activity.SignInActivity
import kotlinx.android.synthetic.main.fragment_enter_username.*


class SignInErrorFragment : Fragment() {
    var errorMessage: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sign_in_error, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpErrorMessage()
        restart.setOnClickListener {
            (activity as? SignInActivity)?.refreshSignInFlow()
        }
    }

    private fun setUpErrorMessage() {
        errorMessage?.let {
            error.visibility = View.VISIBLE
            error.text = errorMessage
        }
    }

    companion object {
        fun newInstance(errorMessage: String): SignInErrorFragment {
            val fragment = SignInErrorFragment()
            fragment.errorMessage = errorMessage
            return fragment
        }
    }
}
