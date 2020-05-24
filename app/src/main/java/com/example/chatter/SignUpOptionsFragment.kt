package com.example.chatter

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_sign_up_options.*

class SignUpOptionsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sign_up_options, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpButtons()
    }

    private fun setUpButtons() {
        sign_up_individual.setOnClickListener {
            (activity as? SignInActivity)?.let {
                it.setScenario(SIGN_UP_INDIVIDUAL)
                it.setUserAndBotResponseArrays()
                it.runMessageFlow(sign_up_individual.text.toString())
            }
        }
        sign_up_student.setOnClickListener {
            (activity as? SignInActivity)?.let {
                it.setScenario(SIGN_UP_STUDENT)
                it.setUserAndBotResponseArrays()
                it.runMessageFlow(sign_up_student.text.toString())
            }
        }
        sign_up_school.setOnClickListener {
            (activity as? SignInActivity)?.let {
                it.setScenario(SIGN_UP_SCHOOL)
                it.setUserAndBotResponseArrays()
                //it.runMessageFlowSignUp(sign_up_school.text.toString())
            }
        }
    }

    companion object {
        const val SIGN_UP_INDIVIDUAL = 4
        const val SIGN_UP_STUDENT = 5
        const val SIGN_UP_SCHOOL = 6
    }

}
