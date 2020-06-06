package com.example.chatter

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.view.animation.AnimationUtils
import android.widget.TextView
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_chatter.*
import kotlinx.android.synthetic.main.fragment_message_options.*
import java.util.*
import kotlin.concurrent.schedule

class MessageMenuOptionsFragment : BaseFragment() {
    private val chatterActivity by lazy {
        activity as ChatterActivity
    }

    private lateinit var database: DatabaseReference
    private lateinit var timerTask: TimerTask
    private lateinit var preferences: Preferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_message_options, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        database = FirebaseDatabase.getInstance().reference
        context?.let {
            preferences = Preferences(it)
        }
        setUpRestartButton()
        checkIfConversationEnd()
    }

    private fun setUpMenu() {
        makeLayoutVisible()
        initializeScrollContainerAndOptionButtons()
        checkForEasterEggs()
    }

    private fun makeLayoutVisible() {
        options_menu_layout.visibility = View.VISIBLE
        conversation_end_message.visibility = View.GONE
    }

    private fun checkIfConversationEnd() {
        val pathRef = database.child(chatterActivity.currentPath)
        val messageListener = baseValueEventListener { dataSnapshot ->
            if (!dataSnapshot.hasChild("optionA")) {
                showErrorMessage()
            } else {
                setUpMenu()
            }
        }
        pathRef.addListenerForSingleValueEvent(messageListener)
    }

    private fun setUpRestartButton() {
        restart.setOnClickListener {
            chatterActivity.refreshChatMessages()
        }
    }

    val setTimerTask: (name: String, delay: Long, () -> Unit) -> TimerTask = { name, delay, doit ->
        timerTask = Timer(name, false).schedule(delay) {
            chatterActivity.runOnUiThread {
                doit()
            }
        }
        chatterActivity.timerTaskArray.add(timerTask)
        timerTask
    }

    private fun initializeScrollContainerAndOptionButtons() {
        chatterActivity.let {
            it.initializeMessagesContainer()
            setUpOptionsMenu(it.currentPath)
            setUpAllOptionClickListeners(it.currentPath)
        }
    }

    private fun setUpOptionsMenu(currentPath: String) {
        val pathA = currentPath + "optionA"
        val pathB = currentPath + "optionB"
        val pathC = currentPath + "optionC"
        setUpOptionMenuText(pathA, optionA)
        setUpOptionMenuText(pathB, optionB)
        setUpOptionMenuText(pathC, optionC)
    }

    private fun setUpMenuOptionClickListener(
        currentPath: String,
        option: TextView,
        optionType: String
    ) {
        val path = currentPath + optionType + "/"
        option.setOnClickListener {
            chatterActivity.let {
                it.currentPath = path
                chatterActivity.removeOptionsMenu()
                val userText = option.text.toString()
                chatterActivity.addUserMessage(userText)
                it.handleNewMessageLogic(userText)
                getBotResponse(it.currentPath)
            }
        }
    }

    private fun setUpAllOptionClickListeners(currentPath: String) {
        setUpMenuOptionClickListener(currentPath, optionA, "optionA")
        setUpMenuOptionClickListener(currentPath, optionB, "optionB")
        setUpMenuOptionClickListener(currentPath, optionC, "optionC")
    }

    private fun getBotResponse(path: String) {
        chatterActivity.removeOptionsMenu()
        val pathReference = database.child(path + "/botMessage")
        chatterActivity.disableNextButton()
        val messageListener = chatterActivity.baseValueEventListener { dataSnapshot ->
            val botMessage = dataSnapshot.value.toString()
            setTimerTask("showBotIsTyping", 700, {
                chatterActivity.addSpaceText()
                chatterActivity.showBotIsTypingView()
            })
            setTimerTask("loadOptionsMenu", 3000, {
                chatterActivity.replaceBotIsTyping(botMessage)
                chatterActivity.loadOptionsMenu()
            })
        }
        pathReference.addListenerForSingleValueEvent(messageListener)
    }

    private fun checkForEasterEggs() {
        chatterActivity.currentPath.let {
            val pathReference = database.child(it)
            val easterEggListener = chatterActivity.baseChildEventListener { dataSnapshot ->
                if (dataSnapshot.hasChild("title") && dataSnapshot.hasChild("points") && dataSnapshot.hasChild(
                        "image"
                    )
                ) {
                    val title = dataSnapshot.child("title").value.toString()
                    val points = dataSnapshot.child("points").value as Long
                    val image = dataSnapshot.child("image").value.toString()
                    chatterActivity.loadEasterEggFragment(title, points, image)
                    chatterActivity.enableNextButton()
                }
            }
            pathReference.addChildEventListener(easterEggListener)
            chatterActivity.disableNextButton()
        }
    }

    private fun showErrorMessage() {
        options_menu_layout.visibility = View.VISIBLE
        optionA.visibility = View.GONE
        optionB.visibility = View.GONE
        optionC.visibility = View.GONE
        conversation_end_message.visibility = View.VISIBLE

    }

    private fun setUpOptionMenuText(path: String, option: TextView) {
        val pathReference = database.child(path + "/text")
        val messageListener = chatterActivity.baseValueEventListener { dataSnapshot ->
            val optionText = dataSnapshot.value.toString()
            option.text = optionText
            chatterActivity.enableNextButton()
        }
        pathReference.addListenerForSingleValueEvent(messageListener)
        chatterActivity.disableNextButton()
    }

    fun selectOptionsWithVoice(text: String) {
        when (text) {
            "hermano" -> {
                optionA.performClick()
                showScoreBoostAnimation(false)
            }
            "amigo" -> {
                optionB.performClick()
                showScoreBoostAnimation(false)
            }
            "gato" -> {
                optionC.performClick()
                showScoreBoostAnimation(false)
            }
        }
    }

    private fun showScoreBoostAnimation(fromEasterEgg: Boolean) {
        chatterActivity.score_boost_layout.visibility = View.VISIBLE
        if (fromEasterEgg) {
            chatterActivity.score_boost.setTextColor(Color.WHITE)
        }
        val aniFade = AnimationUtils.loadAnimation(
            context,
            R.anim.fade_out
        )
        chatterActivity.score_boost_layout.startAnimation(aniFade)
        aniFade.setAnimationListener(object : AnimationListener {
            override fun onAnimationStart(arg0: Animation) {}
            override fun onAnimationRepeat(arg0: Animation) {}
            override fun onAnimationEnd(arg0: Animation) {
                chatterActivity.score_boost_layout.visibility = View.GONE
            }
        })
    }

    companion object {
        fun newInstance(): MessageMenuOptionsFragment {
            return MessageMenuOptionsFragment()
        }
    }
}
