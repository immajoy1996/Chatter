package com.example.chatter

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintSet
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_chatter.*
import kotlinx.android.synthetic.main.fragment_message_options.*
import kotlinx.android.synthetic.main.fragment_message_options.view.*
import java.util.*
import kotlin.concurrent.schedule
import kotlin.concurrent.timer


class MessageMenuOptionsFragment : BaseFragment() {

    private val chatterActivity by lazy {
        activity as ChatterActivity
    }

    private lateinit var database: DatabaseReference
    private lateinit var timerTask: TimerTask

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_message_options, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        database = FirebaseDatabase.getInstance().reference
        initializeScrollContainerAndOptionButtons()
        checkForEasterEggs()
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

    fun setUpOptionsMenu(currentPath: String) {
        val pathA = currentPath + "optionA"
        val pathB = currentPath + "optionB"
        val pathC = currentPath + "optionC"
        setUpOptionMenuText(pathA, optionA)
        setUpOptionMenuText(pathB, optionB)
        setUpOptionMenuText(pathC, optionC)
    }

    fun setUpMenuOptionClickListener(currentPath: String, option: TextView, optionType: String) {
        val path = currentPath + optionType + "/"
        option.setOnClickListener {
            chatterActivity.let {
                it.currentPath = path
                chatterActivity.removeOptionsMenu()
                it.handleNewMessageLogic(option.text.toString())
                getBotResponse(it.currentPath)
            }
        }
    }

    fun setUpAllOptionClickListeners(currentPath: String) {
        setUpMenuOptionClickListener(currentPath, optionA, "optionA")
        setUpMenuOptionClickListener(currentPath, optionB, "optionB")
        setUpMenuOptionClickListener(currentPath, optionC, "optionC")
    }

    fun getBotResponse(path: String) {
        chatterActivity.removeOptionsMenu()
        val pathReference = database.child(path + "/botMessage")
        chatterActivity.disableNextButton()
        val messageListener = chatterActivity.baseValueEventListener { dataSnapshot ->
            val botMessage = dataSnapshot.value.toString()
            setTimerTask("showBotIsTyping", 700, {
                chatterActivity.addSpaceText()
                chatterActivity.showBotIsTypingView()
            })
            setTimerTask("loadOptionsMenu", 2000, {
                chatterActivity.replaceBotIsTyping(botMessage)
                chatterActivity.loadOptionsMenu()
            })
        }
        pathReference.addValueEventListener(messageListener)
    }

    fun checkForEasterEggs() {
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

    fun setUpOptionMenuText(path: String, option: TextView) {
        val pathReference = database.child(path + "/text")
        val messageListener = chatterActivity.baseValueEventListener { dataSnapshot ->
            val optionText = dataSnapshot.value.toString()
            option.text = optionText
            chatterActivity.enableNextButton()
        }
        pathReference.addValueEventListener(messageListener)
        chatterActivity.disableNextButton()
    }

    fun selectOptionsWithVoice(text: String) {
        when (text) {
            "hermano" -> {
                optionA.performClick()
            }
            "amigo" -> {
                optionB.performClick()
            }
            "gato" -> {
                optionC.performClick()
            }
        }
    }

    companion object {
        fun newInstance(): MessageMenuOptionsFragment {
            return MessageMenuOptionsFragment()
        }
    }
}
