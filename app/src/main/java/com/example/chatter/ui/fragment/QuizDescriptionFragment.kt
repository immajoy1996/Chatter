package com.example.chatter.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.chatter.R
import com.example.chatter.ui.activity.ConcentrationActivity
import com.example.chatter.ui.activity.DashboardActivity
import com.example.chatter.ui.activity.QuizActivity
import kotlinx.android.synthetic.main.fragment_quiz_description.*

class QuizDescriptionFragment : Fragment() {
    private var isQuizFragment: Boolean = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_quiz_description, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpViews(isQuizFragment)
        setUpButtons()
    }

    private fun setUpViews(isQuiz: Boolean) {
        if (isQuiz) {
            quiz_icon.setImageResource(R.drawable.concentration)
            welcome_to_quiz_message.text = "Concentration"
            start_quiz_button.text = "Start Game"
            quiz_instruction_one.text = "Move fast and earn coins"
        } else {
            //Jokebook
            quiz_icon.setImageResource(R.drawable.funny_guy)
            welcome_to_quiz_message.text = "Jokebook"
            start_quiz_button.text = "Start"
            quiz_instruction_one.text = "Test your comprehension with some good jokes"
        }
    }

    private fun setUpButtons() {
        if(isQuizFragment) {
            quiz_back_button.setOnClickListener {
                activity?.finish()
            }
            start_quiz_button.setOnClickListener {
                val botImages =
                    (activity as? QuizActivity)?.intent?.getStringArrayListExtra("botImages")
                val intent = Intent(context, ConcentrationActivity::class.java)
                intent.putStringArrayListExtra("botImages", botImages)
                startActivity(intent)
            }
        }else{
            quiz_back_button.setOnClickListener {
                fragmentManager?.popBackStack()
            }
            start_quiz_button.setOnClickListener {
                fragmentManager?.popBackStack()
                (activity as? DashboardActivity)?.loadFragment(StoryBoardOneFragment())
            }
        }
    }

    companion object {
        fun newInstance(isQuizFragment: Boolean): QuizDescriptionFragment {
            val fragment = QuizDescriptionFragment()
            fragment.isQuizFragment = isQuizFragment
            return fragment
        }
    }
}
