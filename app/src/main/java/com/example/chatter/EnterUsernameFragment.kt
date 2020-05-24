package com.example.chatter

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_enter_username.*

class EnterUsernameFragment : Fragment() {

    private var title: String? = null
    private var hint: String? = null
    private var fragmentType: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_enter_username, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpView()
        setUpButtons()
    }

    private fun setUpView() {
        user_input_title.text = title
    }

    fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }


    private fun setUpButtons() {
        submit.setOnClickListener {
            var userInput = user_input.text.toString()
            user_input.hideKeyboard()
            when (fragmentType) {
                ENTER_USERNAME, ENTER_EMAIL -> (activity as? SignInActivity)?.username = userInput
                ENTER_PASSWORD, CHOOSE_PASSWORD -> (activity as? SignInActivity)?.password =
                    userInput
                REENTER_PASSWORD -> (activity as? SignInActivity)?.reenterPassword = userInput
                ENTER_SCHOOL_NAME -> (activity as? SignInActivity)?.schoolName = userInput
            }
            (activity as? SignInActivity)?.let {
                when (fragmentType) {
                    ENTER_SCHOOL_NAME -> {
                        if (isValidSchoolName(userInput)) {
                            it.runMessageFlow(userInput, true)
                        }
                    }
                    ENTER_USERNAME, ENTER_EMAIL -> {
                        if (isValidEmail(userInput)) {
                            it.runMessageFlow(userInput)
                        }
                    }
                    ENTER_PASSWORD, CHOOSE_PASSWORD -> {
                        if (isValidPassword(userInput)) {
                            it.runMessageFlow(userInput)
                        }
                    }
                    REENTER_PASSWORD -> {
                        var myPassword = (activity as? SignInActivity)?.password
                        if (myPassword != null) {
                            if (isValidReenterPassword(userInput, myPassword)) {
                                it.runMessageFlow(userInput)
                            }
                        }
                    }
                }
            }
        }
        restart.setOnClickListener {
            (activity as? SignInActivity)?.refreshSignInFlow()
        }
    }

    private fun isValidSchoolName(school: String): Boolean {
        if (school.isEmpty()) {
            showError("Your school name cannot be empty")
            return false
        }
        hideErrorView()
        return true
    }

    private fun isValidEmail(email: String): Boolean {
        if (email.isEmpty()) {
            showError("Your email cannot be empty")
            return false
        }
        hideErrorView()
        return true
    }

    private fun isValidPassword(password: String): Boolean {
        if (password.isEmpty()) {
            showError("Your password cannot be empty")
            return false
        } else if (password.length < 6) {
            showError("Your password must be at least 6 characters")
            return false
        }
        hideErrorView()
        return true
    }

    private fun isValidReenterPassword(reenterPassword: String, password: String): Boolean {
        if (reenterPassword != password) {
            showError("Your passwords must match")
            return false
        }
        hideErrorView()
        return true
    }

    private fun showError(errorMsg: String) {
        error.visibility = View.VISIBLE
        error.text = errorMsg
    }

    private fun hideErrorView() {
        error.visibility = View.GONE
    }

    companion object {
        fun newInstance(fragmentType: String): EnterUsernameFragment {
            when (fragmentType) {
                ENTER_USERNAME -> return newInstance(fragmentType, "Enter your email")
                ENTER_PASSWORD -> return newInstance(fragmentType, "Enter your password")
                ENTER_EMAIL -> return newInstance(fragmentType, "Enter your email")
                CHOOSE_PASSWORD -> return newInstance(fragmentType, "Choose your password")
                REENTER_PASSWORD -> return newInstance(fragmentType, "Reenter your password")
                ENTER_SCHOOL_NAME -> return newInstance(fragmentType, "Enter your school name")
            }
            return newInstance("ERROR", "")
        }

        fun newInstance(
            fragmentType: String,
            titleStr: String,
            hintStr: String? = null
        ): EnterUsernameFragment {
            var fragment = EnterUsernameFragment()
            fragment.fragmentType = fragmentType
            fragment.title = titleStr
            fragment.hint = hintStr
            return fragment
        }

        const val ENTER_USERNAME = "Enter username"
        const val ENTER_PASSWORD = "Enter password"
        const val ENTER_EMAIL = "Enter your email"
        const val CHOOSE_PASSWORD = "Choose your password"
        const val REENTER_PASSWORD = "Renter password"
        const val ENTER_SCHOOL_NAME = "Enter school name"
    }
}
