package com.example.chatter.ui.activity

import ProgressBarAnimation
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.chatter.R
import com.example.chatter.data.MultipleChoiceQuestion
import com.example.chatter.data.QuestionList
import com.example.chatter.ui.fragment.EasterEggFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_chatter.*
import kotlinx.android.synthetic.main.activity_multiple_choice.*
import java.util.*

class MultipleChoiceActivity : BaseActivity() {
    private var multipleChoiceQuestions = arrayListOf<MultipleChoiceQuestion>()
    private var index = 0
    private var mediaPlayer = MediaPlayer()
    var count = 0
    var ncorrect = 0
    var gotItWrong = false
    private var wonGameFragment =
        EasterEggFragment.newInstance("Congrats! You've passed.", 100L)
    private var lostGameFragment =
        EasterEggFragment.newInstance("Better luck next time")
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private var botTitle: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_multiple_choice)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
        intent.getStringExtra("botTitle")?.let {
            botTitle = it
        }
        setUpTopBar()
        initProgressBar()
        setUpQuestionArrayAndFirstQuestion()
        setUpAnswerButtons()
    }

    override fun setUpTopBar() {
        multiple_choice_back_button.setOnClickListener {
            finish()
        }
    }

    fun updateTotalScore(pointsAdded: Long) {
        var oldScore: Int? = null
        var latestScore: Int? = null
        if (auth.currentUser != null) {
            auth.currentUser?.uid?.let {
                val userUid = it
                val pathRef = database.child(ChatterActivity.USERS).child(userUid).child("points")
                val pointsListener = baseValueEventListener { dataSnapshot ->
                    val currentScore = dataSnapshot.value as Long
                    oldScore = currentScore.toInt()
                    val newScore = currentScore + pointsAdded
                    latestScore = newScore.toInt()
                    database.child(ChatterActivity.USERS).child(userUid).child("points")
                        .setValue(newScore)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Points added", Toast.LENGTH_SHORT).show()
                        }
                }
                pathRef.addListenerForSingleValueEvent(pointsListener)
            }
        } else {
            //Guest mode
            val currentScore = preferences.getCurrentScore()
            oldScore = currentScore
            val newScore = currentScore + pointsAdded.toInt()
            latestScore = newScore
            preferences.storeCurrentScore(newScore)
            Toast.makeText(this, "Points added", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(R.anim.slide_in, R.anim.slide_out)
            .replace(multiple_choice_root_layout.id, fragment)
            .addToBackStack(fragment.javaClass.name)
            .commit()
    }

    fun startGame() {
        initProgressBar()
        ncorrect = 0
        count = 0
        index = 0
        Collections.shuffle(multipleChoiceQuestions)
        setUpQuestion()
    }

    fun removeStartGameFragment() {
        supportFragmentManager.popBackStack()
    }

    private fun initProgressBar() {
        multiple_choice_progress_bar.setProgress(0)
    }

    private fun setUpLotsOfQuestions() {
        while (multipleChoiceQuestions.size < 50) {
            val size = multipleChoiceQuestions.size
            if (size % 2 == 0) {
                multipleChoiceQuestions.add(multipleChoiceQuestions[0])
            } else {
                multipleChoiceQuestions.add(multipleChoiceQuestions[1])
            }
        }
    }

    private fun playCorrectAnswerSound() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
            mediaPlayer.release()
        }
        mediaPlayer = MediaPlayer.create(this, R.raw.correct_answer)
        mediaPlayer.start()
    }

    private fun playWrongAnswerSound() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
            mediaPlayer.release()
        }
        mediaPlayer = MediaPlayer.create(this, R.raw.wrong_answer)
        mediaPlayer.start()
    }

    private fun setUpQuestionArrayAndFirstQuestion() {
        val jsonStringObject = intent.extras?.getString("quizQuestions")
        jsonStringObject?.let {
            val questionObject = Gson().fromJson(it, QuestionList::class.java)
            multipleChoiceQuestions = questionObject.list
            Collections.shuffle(multipleChoiceQuestions)
            setUpLotsOfQuestions()
            setUpQuestion()
        }
    }

    private fun setUpQuestion() {
        val question = multipleChoiceQuestions[index]
        multiple_choice_answer1.setBackgroundResource(R.drawable.multiple_choice_answer_background)
        multiple_choice_answer2.setBackgroundResource(R.drawable.multiple_choice_answer_background)
        multiple_choice_answer3.setBackgroundResource(R.drawable.multiple_choice_answer_background)

        if (question.questionType == "textQuestion") {
            multiple_choice_text_container.visibility = View.VISIBLE
            multiple_choice_image_container.visibility = View.GONE
            multiple_choice_question_title.text = question.questionTitle
            multiple_choice_question.text = question.question
            multiple_choice_answer1.text = question.answer1
            multiple_choice_answer2.text = question.answer2
            multiple_choice_answer3.text = question.answer3
        } else {
            multiple_choice_text_container.visibility = View.GONE
            multiple_choice_image_container.visibility = View.VISIBLE
            multiple_choice_question_title.text = question.questionTitle
            multiple_choice_image?.let {
                Glide.with(this)
                    .load(question.image)
                    .into(it)
            }
            multiple_choice_answer1.text = question.answer1
            multiple_choice_answer2.text = question.answer2
            multiple_choice_answer3.text = question.answer3
        }
    }

    private fun showProgressAnimation(from: Int, to: Int, progressBar: ProgressBar) {
        val anim = ProgressBarAnimation(progressBar, from.toFloat(), to.toFloat())
        anim.duration = 1000
        progressBar.startAnimation(anim)
    }

    private fun getNextQuestion() {
        setTimerTask("nextQuestionTask", 500, {
            val from = (100.0 * count / TOTAL_SIZE).toInt()
            count++
            val to = (100.0 * count / TOTAL_SIZE).toInt()
            showProgressAnimation(from, to, multiple_choice_progress_bar)
            index = (index + 1) % multipleChoiceQuestions.size
            if (count < TOTAL_SIZE) {
                setUpQuestion()
            } else {
                if (isGameWon()) {
                    loadFragment(wonGameFragment)
                    botTitle?.let {
                        preferences.storeMultipleChoiceDeckComplete(it)
                    }
                } else {
                    loadFragment(lostGameFragment)
                }
            }
        })
    }

    private fun isGameWon(): Boolean {
        return (count == 5 && ncorrect >= 4)
    }

    private fun runGotItRightLogic() {
        if (!gotItWrong) {
            ncorrect++
        }
        gotItWrong = false
    }

    private fun runGotItWrongLogic() {
        gotItWrong = true
    }

    private fun setUpAnswerButtons() {
        multiple_choice_answer1.setOnDebouncedClickListener {
            val correctAnswer = multipleChoiceQuestions[index].correctAnswer
            if (correctAnswer == 1) {
                playCorrectAnswerSound()
                runGotItRightLogic()
                multiple_choice_answer1.setBackgroundResource(R.drawable.correct_answer_background)
                getNextQuestion()
            } else {
                playWrongAnswerSound()
                runGotItWrongLogic()
                multiple_choice_answer1.setBackgroundResource(R.drawable.wrong_answer_background)
            }
        }

        multiple_choice_answer2.setOnDebouncedClickListener {
            val correctAnswer = multipleChoiceQuestions[index].correctAnswer
            if (correctAnswer == 2) {
                playCorrectAnswerSound()
                runGotItRightLogic()
                multiple_choice_answer2.setBackgroundResource(R.drawable.correct_answer_background)
                getNextQuestion()
            } else {
                playWrongAnswerSound()
                runGotItWrongLogic()
                multiple_choice_answer2.setBackgroundResource(R.drawable.wrong_answer_background)
            }
        }

        multiple_choice_answer3.setOnDebouncedClickListener {
            val correctAnswer = multipleChoiceQuestions[index].correctAnswer
            if (correctAnswer == 3) {
                playCorrectAnswerSound()
                runGotItRightLogic()
                multiple_choice_answer3.setBackgroundResource(R.drawable.correct_answer_background)
                getNextQuestion()
            } else {
                playWrongAnswerSound()
                runGotItWrongLogic()
                multiple_choice_answer3.setBackgroundResource(R.drawable.wrong_answer_background)
            }
        }
    }

    companion object {
        private const val TOTAL_SIZE = 5
    }
}