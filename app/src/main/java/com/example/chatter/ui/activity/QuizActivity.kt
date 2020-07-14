package com.example.chatter.ui.activity

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.chatter.ui.fragment.QuizDescriptionFragment
import com.example.chatter.ui.fragment.QuizFragment
import com.example.chatter.R

import kotlinx.android.synthetic.main.activity_quiz.*
import kotlinx.android.synthetic.main.top_bar.*

class QuizActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)
        setUpTopBar()
        loadFragment(QuizDescriptionFragment())
    }

    override fun setUpTopBar() {
        top_bar_title.text = "Quiz"
        home.visibility = View.GONE
        back.visibility = View.VISIBLE
        back.setOnClickListener {
            finish()
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(quiz_root_container.id, fragment)
            .addToBackStack(fragment.javaClass.name)
            .commit()
    }

    fun loadQuizFragment() {
        loadFragmentIntoInnerContainer(QuizFragment())
    }

    private fun loadFragmentIntoInnerContainer(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(quiz_inner_container.id, fragment)
            .addToBackStack(fragment.javaClass.name)
            .commit()
    }
}
