package com.example.chatter.ui.activity

import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.chatter.R
import com.example.chatter.data.Joke
import com.example.chatter.ui.fragment.JokesExplanationFragment
import com.example.chatter.ui.fragment.LoadingAnimatedFragment
import com.example.chatter.ui.fragment.QuizDescriptionFragment
import com.example.chatter.ui.fragment.StoryBoardOneFragment
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_jokes.*
import kotlinx.android.synthetic.main.fragment_story_board_one.*

class JokesActivity : BaseActivity() {
    private var jokesFragment = QuizDescriptionFragment.newInstance(false)
    private lateinit var jokesExplanationFragment: JokesExplanationFragment
    private var actualJokesPage = StoryBoardOneFragment()
    private var loadingAnimatedFragment = LoadingAnimatedFragment()
    private var jokesArray = arrayListOf<Joke>()
    private var index = 0
    private lateinit var database: DatabaseReference
    private var levelsArray = arrayListOf<String>("Easy", "Medium", "Hard")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_jokes)
        database = FirebaseDatabase.getInstance().reference
        loadJokes()
    }

    private fun resetJokesArray() {
        jokesArray.clear()
    }

    private fun loadJokes() {
        loadFragment(loadingAnimatedFragment)
        resetJokesArray()
        val userLevel = intent?.getStringExtra("userLevel") ?: "Easy"
        val jokeListener = baseChildEventListener { dataSnapshot ->
            val joke = dataSnapshot.child("joke").value.toString()
            val answer = dataSnapshot.child("answer").value.toString()
            val explanation = dataSnapshot.child("explanation").value.toString()
            jokesArray.add(Joke(joke, answer, explanation))
        }
        for (item in levelsArray) {
            if (userLevel.compareTo(item) >= 0) {
                database.child("Jokes").child(item).addChildEventListener(jokeListener)
            }
        }
        setTimerTask("fetchJokes", 1500, {
            removeLoadingFragment()
            loadActualJokesPage()
        })
    }

    fun showTheShowAnswerButton() {
        if (actualJokesPage.isVisible) {
            show_answer_button.visibility = View.VISIBLE
        }
    }

    fun fetchNextJoke(): Joke {
        val size = jokesArray.size
        if (size > 0) {
            return jokesArray[(index + 1) % size]
        } else {
            return Joke("", "", "")
        }
    }

    private fun removeLoadingFragment() {
        supportFragmentManager.popBackStack()
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

    fun loadJokesExplanationFragment(explanation: String) {
        jokesExplanationFragment = JokesExplanationFragment.newInstance(explanation)
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
        if (actualJokesPage.isVisible) {
            show_answer_button.visibility = View.VISIBLE
            answer_textview.visibility = View.GONE
            actualJokesPage.currentJokeItem =
                fetchNextJoke()
            actualJokesPage.currentJokeItem?.let {
                showTypingAnimation(joke_textview, 50, it.joke)
            }
        }
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