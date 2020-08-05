package com.example.chatter.ui.activity

import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.chatter.R
import com.example.chatter.data.MultipleChoiceQuestion
import com.example.chatter.data.QuestionList
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_multiple_choice.*

class MultipleChoiceActivity : BaseActivity() {
    private var multipleChoiceQuestions = arrayListOf<MultipleChoiceQuestion>()
    private var index = 0
    private var mediaPlayer = MediaPlayer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_multiple_choice)
        setUpTopBar()
        setUpQuestionArrayAndFirstQuestion()
        setUpAnswerButtons()
    }

    override fun setUpTopBar() {
        multiple_choice_back_button.setOnClickListener {
            finish()
        }
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

    private fun getNextQuestion() {
        setTimerTask("nextQuestionTask", 500, {
            if (index + 1 < multipleChoiceQuestions.size) {
                index++
                setUpQuestion()
            }
        })
    }

    private fun setUpAnswerButtons() {
        multiple_choice_answer1.setOnDebouncedClickListener {
            val correctAnswer = multipleChoiceQuestions[index].correctAnswer
            if (correctAnswer == 1) {
                playCorrectAnswerSound()
                multiple_choice_answer1.setBackgroundResource(R.drawable.correct_answer_background)
                getNextQuestion()
            } else {
                playWrongAnswerSound()
                multiple_choice_answer1.setBackgroundResource(R.drawable.wrong_answer_background)
            }
        }

        multiple_choice_answer2.setOnDebouncedClickListener {
            val correctAnswer = multipleChoiceQuestions[index].correctAnswer
            if (correctAnswer == 2) {
                playCorrectAnswerSound()
                multiple_choice_answer2.setBackgroundResource(R.drawable.correct_answer_background)
                getNextQuestion()
            } else {
                playWrongAnswerSound()
                multiple_choice_answer2.setBackgroundResource(R.drawable.wrong_answer_background)
            }
        }

        multiple_choice_answer3.setOnDebouncedClickListener {
            val correctAnswer = multipleChoiceQuestions[index].correctAnswer
            if (correctAnswer == 3) {
                playCorrectAnswerSound()
                multiple_choice_answer3.setBackgroundResource(R.drawable.correct_answer_background)
                getNextQuestion()
            } else {
                playWrongAnswerSound()
                multiple_choice_answer3.setBackgroundResource(R.drawable.wrong_answer_background)
            }
        }
    }
}