package com.example.chatter.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.chatter.R
import com.example.chatter.ui.activity.ConcentrationActivity
import kotlinx.android.synthetic.main.fragment_quiz_description.*

class QuizDescriptionFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_quiz_description, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpButtons()
    }

    private fun setUpButtons() {
        quiz_back_button.setOnClickListener {
            activity?.finish()
        }
        start_quiz_button.setOnClickListener {
            val intent = Intent(context, ConcentrationActivity::class.java)
            startActivity(intent)
        }
    }
}
