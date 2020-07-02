package com.example.chatter

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.fragment_quiz.*

class QuizFragment : Fragment() {
    private lateinit var database: DatabaseReference
    private val questionArray = arrayListOf<QuizQuestion>()
    private var currentQuestion: QuizQuestion? = null
    private var questionIndex = 0
    private var totalQuestions = 0
    private var totalCorrect = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_quiz, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpTopBar()
        fetchQuizQuestions()
        setUpOptionClick()
    }

    private fun setUpTopBar(){
        (activity as? BotQuizSelectionActivity)?.setUpQuizTopBar()
    }

    private fun fetchQuizQuestions() {
        database = FirebaseDatabase.getInstance().reference
        val quizReference = database.child("Quiz")
        val questionListener = (activity as? BotQuizSelectionActivity)?.let {
            it.baseChildEventListener { dataSnapshot ->
                val questionItem = convertDataToQuestionItem(dataSnapshot)
                questionArray.add(questionItem)
                if (questionArray.size > ITEM_COUNT) {
                    setUpFirstQuestion()
                }
            }
        }
        questionListener?.let {
            quizReference.addChildEventListener(it)
        }
    }

    private fun convertDataToQuestionItem(dataSnapshot: DataSnapshot): QuizQuestion {
        val type = dataSnapshot.child("type").value.toString()
        val question = dataSnapshot.child("question").value.toString()
        val image = dataSnapshot.child("image").value.toString()
        val optionA = dataSnapshot.child("optionA").value.toString()
        val optionB = dataSnapshot.child("optionB").value.toString()
        val optionC = dataSnapshot.child("optionC").value.toString()
        val optionD = dataSnapshot.child("optionD").value.toString()
        val correct = dataSnapshot.child("correct").value.toString()
        val sentence = dataSnapshot.child("sentence").value.toString()
        val questionItem =
            QuizQuestion(
                image,
                correct,
                question,
                type,
                optionA,
                optionB,
                optionC,
                optionD,
                sentence
            )
        return questionItem
    }

    private fun setUpFirstQuestion() {
        questionIndex = 0
        currentQuestion = questionArray[0]
        setUpQuestionDetails()
    }

    private fun setUpQuestionDetails() {
        if (currentQuestion?.type == "image") {
            image_question_layout.visibility = View.VISIBLE
            long_question_layout.visibility = View.GONE
            val resId =
                resources.getIdentifier(currentQuestion?.image, "drawable", "com.example.chatter")
            image_question_text.text = currentQuestion?.question
            question_image.setImageResource(resId)
            optionA_text.text = currentQuestion?.optionA
            optionB_text.text = currentQuestion?.optionB
            optionC_text.text = currentQuestion?.optionC
            optionD_text.text = currentQuestion?.optionD
        } else if (currentQuestion?.type == "sentence") {
            image_question_layout.visibility = View.GONE
            long_question_layout.visibility = View.VISIBLE
            question_text.text = currentQuestion?.question
            text_to_translate.text = currentQuestion?.sentence
            long_optionA_text.text = currentQuestion?.optionA
            long_optionB_text.text = currentQuestion?.optionB
            long_optionC_text.text = currentQuestion?.optionC
        }
    }

    private fun setUpOptionClick() {
        long_optionA.setOnClickListener {
            long_optionA_check_mark.visibility = View.VISIBLE
            if (currentQuestion?.correct == "optionA") {
                long_optionA_check_mark.setImageResource(R.drawable.green_check)
                runNextQuestionLogic()
            } else {
                long_optionA_check_mark.setImageResource(R.drawable.x_icon)
            }
        }
        long_optionB.setOnClickListener {
            long_optionB_check_mark.visibility = View.VISIBLE
            if (currentQuestion?.correct == "optionB") {
                long_optionB_check_mark.setImageResource(R.drawable.green_check)
                runNextQuestionLogic()
            } else {
                long_optionB_check_mark.setImageResource(R.drawable.x_icon)
            }
        }
        long_optionC.setOnClickListener {
            long_optionC_check_mark.visibility = View.VISIBLE
            if (currentQuestion?.correct == "optionC") {
                long_optionC_check_mark.setImageResource(R.drawable.green_check)
                runNextQuestionLogic()
            } else {
                long_optionC_check_mark.setImageResource(R.drawable.x_icon)
            }
        }
        optionA.setOnClickListener {
            optionA_check_mark.visibility = View.VISIBLE
            if (currentQuestion?.correct == "optionA") {
                optionA_check_mark.setImageResource(R.drawable.green_check)
                runNextQuestionLogic()
            } else {
                optionA_check_mark.setImageResource(R.drawable.x_icon)
            }
        }
        optionB.setOnClickListener {
            optionB_check_mark.visibility = View.VISIBLE
            if (currentQuestion?.correct == "optionB") {
                optionB_check_mark.setImageResource(R.drawable.green_check)
                runNextQuestionLogic()
            } else {
                optionB_check_mark.setImageResource(R.drawable.x_icon)
            }
        }
        optionC.setOnClickListener {
            optionC_check_mark.visibility = View.VISIBLE
            if (currentQuestion?.correct == "optionC") {
                optionC_check_mark.setImageResource(R.drawable.green_check)
                runNextQuestionLogic()
            } else {
                optionC_check_mark.setImageResource(R.drawable.x_icon)
            }
        }
        optionD.setOnClickListener {
            optionD_check_mark.visibility = View.VISIBLE
            if (currentQuestion?.correct == "optionD") {
                optionD_check_mark.setImageResource(R.drawable.green_check)
                runNextQuestionLogic()
            } else {
                optionD_check_mark.setImageResource(R.drawable.x_icon)
            }
        }
    }

    private fun goToNextQuestion() {
        questionIndex++
        if (questionIndex < questionArray.size) {
            optionA_check_mark.visibility = View.GONE
            optionB_check_mark.visibility = View.GONE
            optionC_check_mark.visibility = View.GONE
            optionD_check_mark.visibility = View.GONE
            long_optionA_check_mark.visibility = View.GONE
            long_optionB_check_mark.visibility = View.GONE
            long_optionC_check_mark.visibility = View.GONE
            currentQuestion = questionArray[questionIndex]
            setUpQuestionDetails()
        }
    }

    private fun runNextQuestionLogic() {
        (activity as? BotQuizSelectionActivity)?.let {
            it.setTimerTask("pauseBeforeNextQuestion", 700, {
                goToNextQuestion()
            })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        (activity as? BotQuizSelectionActivity)?.setUpTopBar()
    }

    companion object {
        private const val ITEM_COUNT = 1
    }
}
