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
            when (fragmentType) {
                ENTER_USERNAME -> (activity as? SignInActivity)?.username = userInput
                ENTER_PASSWORD -> (activity as? SignInActivity)?.password = userInput
            }
            (activity as? SignInActivity)?.let {
                user_input.hideKeyboard()
                it.runMessageFlow(user_input.text.toString())
            }
        }
        restart.setOnClickListener{
            (activity as? SignInActivity)?.refreshSignInFlow()
        }
    }

    companion object {
        fun newInstance(fragmentType: String): EnterUsernameFragment {
            when (fragmentType) {
                ENTER_USERNAME -> return newInstance(fragmentType, "Enter your username")
                ENTER_PASSWORD -> return newInstance(fragmentType, "Enter your password")
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

        const val ENTER_USERNAME = "ENTER_USERNAME"
        const val ENTER_PASSWORD = "ENTER_PASSWORD"
    }
}
