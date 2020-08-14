package com.example.chatter.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatter.R
import com.example.chatter.adapters.FlashCardDecksAdapter
import com.example.chatter.data.MultipleChoiceQuestion
import com.example.chatter.data.QuestionList
import com.example.chatter.interfaces.ConcentrationGameClickedInterface
import com.example.chatter.interfaces.MultipleChoiceClickedInterface
import com.example.chatter.interfaces.SpeechGameClickedInterface
import com.example.chatter.ui.SpeechGameActivity
import com.example.chatter.ui.fragment.LoadingAnimatedFragment
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_games.*
import kotlinx.android.synthetic.main.top_bar.*


class GamesActivity : BaseActivity(), ConcentrationGameClickedInterface,
    MultipleChoiceClickedInterface, SpeechGameClickedInterface {
    private var botTitles = arrayListOf<String>()
    private var botImages = arrayListOf<String>()
    private var gamesImages = arrayListOf<Int>()
    private var botDescriptions = arrayListOf<String>()
    private var loadingAnimatedFragment = LoadingAnimatedFragment()
    private var multipleChoiceQuestions = arrayListOf<MultipleChoiceQuestion>()

    private lateinit var database: DatabaseReference
    private var launched = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_games)
        database = FirebaseDatabase.getInstance().reference
        setUpTopBar()
        setUpGamesRecycler()
        fetchGames()
    }

    override fun setUpTopBar() {
        back.visibility = View.VISIBLE
        back.setOnClickListener {
            finish()
        }
        home.visibility = View.GONE
        top_bar_title.text = "Games!"
        top_bar_title.visibility = View.VISIBLE
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(games_root_layout.id, fragment)
            .addToBackStack(fragment.javaClass.name)
            .commit()
    }

    private fun setUpGamesRecycler() {
        games_recycler.apply {
            layoutManager = LinearLayoutManager(this@GamesActivity)
        }
    }

    private fun resetArrays() {
        botTitles.clear()
        gamesImages.clear()
        botDescriptions.clear()
    }

    private fun fetchGames() {
        resetArrays()

        botTitles.add("Listen to me!")
        botTitles.add("Multiple Choice")
        botTitles.add("Concentration")

        gamesImages.add(R.drawable.audio)
        gamesImages.add(R.drawable.quiz_icon)
        gamesImages.add(R.drawable.concentration)

        botDescriptions.add("Write what Helper Monkey says")
        botDescriptions.add("Choose the correct meaning of the expression or word")
        botDescriptions.add("Be quick and match the bot images")

        games_recycler.adapter = FlashCardDecksAdapter(
            this,
            botTitles,
            null,
            gamesImages,
            botDescriptions,
            true,
            this,
            this,
            this
        )
    }

    override fun onResume() {
        super.onResume()
        launched = false
    }

    override fun onConcentrationGameClicked() {
        val imageArray = arrayListOf<String>()
        database.child("BotCatalog").addChildEventListener(baseChildEventListener {
            if (!launched) {
                val botImage = it.child("botImage").value.toString()
                val intent = Intent(this, ConcentrationActivity::class.java)
                imageArray.add(botImage)
                if (imageArray.size >= NUM_BOTS) {
                    launched = true
                    intent.putStringArrayListExtra("botImages", imageArray)
                    startActivity(intent)
                }
            }
        })
    }

    private fun loadMultipleChoiceQuestions() {
        if (multipleChoiceQuestions.size < MAX_QUESTIONS) {
            database.child(MULTIPLE_CHOICE_PATH).addChildEventListener(baseChildEventListener {
                val questionTitle = it.child("questionTitle").value.toString()
                val question = it.child("question").value
                val answer1 = it.child("answer1").value.toString()
                val answer2 = it.child("answer2").value.toString()
                val answer3 = it.child("answer3").value.toString()
                val correctAnswer = it.child("correctAnswer").value.toString().toInt()
                val questionType = it.child("questionType").value.toString()
                val image = it.child("image").value
                if (questionType == "imageQuestion") {
                    image?.let {
                        multipleChoiceQuestions.add(
                            MultipleChoiceQuestion(
                                questionTitle,
                                null,
                                answer1,
                                answer2,
                                answer3,
                                correctAnswer,
                                questionType,
                                it.toString()
                            )
                        )
                    }
                } else {
                    question?.let {
                        multipleChoiceQuestions.add(
                            MultipleChoiceQuestion(
                                questionTitle,
                                it.toString(),
                                answer1,
                                answer2,
                                answer3,
                                correctAnswer,
                                questionType,
                                null
                            )
                        )
                    }
                }
                if (multipleChoiceQuestions.size >= MAX_QUESTIONS) {
                    removeLoadingFragment()
                    val intent = Intent(this, MultipleChoiceActivity::class.java)
                    intent.putExtra(
                        "quizQuestions",
                        Gson().toJson(QuestionList(multipleChoiceQuestions))
                    )
                    startActivity(intent)
                }
            })
        } else {
            removeLoadingFragment()
            val intent = Intent(this, MultipleChoiceActivity::class.java)
            intent.putExtra("quizQuestions", Gson().toJson(QuestionList(multipleChoiceQuestions)))
            startActivity(intent)
        }
    }

    private fun removeLoadingFragment() {
        supportFragmentManager.popBackStack()
    }

    override fun onMultipleChoiceClicked() {
        loadFragment(loadingAnimatedFragment)
        setTimerTask("loadingMultipleChoice", 1000L, {
            loadMultipleChoiceQuestions()
        })
    }

    private var speechGameSentenceArray = arrayListOf<String>()

    override fun onSpeechGameClicked() {
        loadFragment(loadingAnimatedFragment)
        database.child(SPEECH_GAME_PATH).addChildEventListener(baseChildEventListener { dataSnapshot ->
                val sentence = dataSnapshot.child("sentence").value.toString()
                speechGameSentenceArray.add(sentence)
            })
        setTimerTask("loadingSpeechGame", 1000L, {
            supportFragmentManager.popBackStack()
            loadSpeechGameActivity(speechGameSentenceArray)
        })
    }

    private fun loadSpeechGameActivity(sentenceArray: ArrayList<String>) {
        val intent = Intent(this, SpeechGameActivity::class.java)
        intent.putExtra("SentenceArray", sentenceArray)
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    companion object {
        private const val NUM_BOTS = 9
        private const val MULTIPLE_CHOICE_PATH = "Games/MultipleChoice/Easy/"
        private const val SPEECH_GAME_PATH = "Games/SpeechGame/Easy"
        private const val MAX_QUESTIONS = 2
    }
}