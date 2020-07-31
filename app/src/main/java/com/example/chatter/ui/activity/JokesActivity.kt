package com.example.chatter.ui.activity

import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.chatter.R
import com.example.chatter.ui.fragment.JokesExplanationFragment
import com.example.chatter.ui.fragment.QuizDescriptionFragment
import com.example.chatter.ui.fragment.StoryBoardOneFragment
import kotlinx.android.synthetic.main.activity_jokes.*
import kotlinx.android.synthetic.main.fragment_story_board_one.*

class JokesActivity : BaseActivity() {
    private var jokesFragment = QuizDescriptionFragment.newInstance(false)
    private var jokesExplanationFragment = JokesExplanationFragment()
    private var actualJokesPage = StoryBoardOneFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_jokes)
        loadFragment(jokesFragment)
    }

    fun loadFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(jokes_root_layout.id, fragment)
            .addToBackStack(fragment.javaClass.name)
            .commit()
    }

    fun showSmilesButtonsLayout() {
        happy_layout.visibility = View.VISIBLE
        sad_layout.visibility = View.VISIBLE
        did_you_get_it.visibility = View.VISIBLE
    }

    fun loadJokesExplanationFragment() {
        supportFragmentManager
            .beginTransaction()
            .replace(jokes_explanation_root_container.id, jokesExplanationFragment)
            .addToBackStack(jokesExplanationFragment.javaClass.name)
            .commit()
    }

    fun loadActualJokesPage() {
        loadFragment(actualJokesPage)
    }

    fun showNextJoke() {
        val joke = getNextJoke()
        showTypingAnimation(joke_textview, 50, joke)
    }

    private fun showTypingAnimation(textView: TextView, delay: Long, message: String) {
        val handler = Handler()
        var pos = 0
        val characterAdder: Runnable = object : Runnable {
            override fun run() {
                val setStr = message.subSequence(0, pos + 1)
                textView.setText(setStr)
                pos++
                if (pos == message.length) {
                    handler.removeCallbacksAndMessages(null)
                } else {
                    handler.postDelayed(this, delay)
                }
            }
        }

        handler.removeCallbacks(characterAdder);
        handler.postDelayed(characterAdder, delay)
    }

    override fun setUpTopBar() {
        //TODO("Not yet implemented")
    }
}