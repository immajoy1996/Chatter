package com.example.chatter.ui.fragment

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast
import com.example.chatter.extra.Preferences
import com.example.chatter.R
import com.example.chatter.ui.activity.ChatterActivity
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


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_message_options, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        database = FirebaseDatabase.getInstance().reference
        //setUpRestartButton()
        checkIfConversationEnd()
    }

    private fun setUpMenu() {
        makeLayoutVisible()
        initializeScrollContainerAndOptionButtons()
        checkForEasterEggs()
    }

    private fun makeLayoutVisible() {
        options_menu_layout?.visibility = View.VISIBLE
        conversation_end_message?.visibility = View.GONE
    }

    private fun checkIfConversationEnd() {
        val pathRef = database.child(chatterActivity.currentPath)
        val messageListener = baseValueEventListener { dataSnapshot ->
            if (!dataSnapshot.hasChild("optionA") && !dataSnapshot.hasChild("optionB") && !dataSnapshot.hasChild(
                    "optionC"
                )
            ) {
                checkForEasterEggs()
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

    private fun initializeScrollContainerAndOptionButtons() {
        chatterActivity.let {
            it.initializeMessagesContainer()
            setUpOptionsMenu(it.currentPath)
            setUpAllOptionClickListeners(it.currentPath)
        }
    }

    private fun setUpOptionsMenu(currentPath: String) {
        val pathA = currentPath + "/optionA"
        val pathB = currentPath + "/optionB"
        val pathC = currentPath + "/optionC"
        setUpOptionMenuText(pathA, optionA)
        setUpOptionMenuText(pathB, optionB)
        setUpOptionMenuText(pathC, optionC)
    }

    private fun setUpMenuOptionClickListener(
        currentPath: String,
        option: TextView,
        optionType: String
    ) {
        option.setOnDebouncedClickListener {
            handleOptionClick(currentPath, option, optionType)
        }
    }

    private fun handleOptionClick(path: String, option: TextView, optionType: String) {
        chatterActivity.let {
            val path = it.currentPath + "/" + optionType + "/"
            it.currentPath = path
            chatterActivity.removeOptionsMenu()
            chatterActivity.addUserMessage(option.text.toString())
            it.handleNewMessageLogic(option.text.toString())
            it.resetResponseVariables()
            Log.d("HELLO", it.currentPath)
            it.getBotResponse(it.currentPath)
        }
    }

    private fun setUpAllOptionClickListeners(currentPath: String) {
        setUpMenuOptionClickListener(currentPath, optionA, "optionA")
        setUpMenuOptionClickListener(currentPath, optionB, "optionB")
        setUpMenuOptionClickListener(currentPath, optionC, "optionC")
    }

    private fun checkForEasterEggs() {
        chatterActivity.currentPath.let {
            /*if (preferences.haveSeenCurrentPath(it) == "seen") {
                return
            }*/
            //preferences.storeCurrentPath(it)
            val pathReference = database.child(it)
            val easterEggListener = chatterActivity.baseChildEventListener { dataSnapshot ->
                if (dataSnapshot.hasChild("title") && dataSnapshot.hasChild("type")
                ) {
                    val type = dataSnapshot.child("type").value.toString()
                    val title = dataSnapshot.child("title").value.toString()
                    //val points = dataSnapshot.child("points").value as Long
                    val image = dataSnapshot.child("image").value.toString()
                    chatterActivity.loadEasterEggFragment(type, title, null, image)
                }
            }
            pathReference.addChildEventListener(easterEggListener)
        }
    }

    private fun showErrorMessage() {
        options_menu_layout.visibility = View.VISIBLE
        optionA.visibility = View.GONE
        optionB.visibility = View.GONE
        optionC.visibility = View.GONE
        conversation_end_message.visibility = View.GONE
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

    private fun isTextMatch(text: String, target: String): Boolean {
        val parts = target.split(" ")
        var count = 0
        for (str in parts) {
            val c = str.last()
            var temp = str
            if (!((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z'))) {
                //last char is punctuation
                temp = str.subSequence(0, str.length - 1).toString()
            }
            if (text.contains(temp, ignoreCase = true)) {
                count++
            }
        }
        Log.d("Text matching ", "${count} ${parts.size}")
        return count > MATCH_PERCENTAGE * parts.size
    }

    private fun performDebouncedOptionClick(optionType: String, userText: String) {
        var optionId: Int = R.id.optionA
        if (optionType == "optionB") optionId = R.id.optionB
        else if (optionType == "optionC") optionId = R.id.optionC

        val lastClickTime: Long = preferences.getLastClickTime(optionId)
        val currentTime = System.currentTimeMillis()

        if (lastClickTime == -1L || currentTime - lastClickTime > MIN_TIME_BETWEEN_CLICKS) {
            preferences.storeLastClickTime(optionId, currentTime)
            chatterActivity.let {
                val path = it.currentPath + optionType + "/"
                it.currentPath = path
                chatterActivity.removeOptionsMenu()
                chatterActivity.addUserMessage(userText)
                it.handleNewMessageLogic(userText)
                it.getBotResponse(it.currentPath)
            }
        }
    }

    fun selectOptionsWithVoice(text: String) {
        if (optionA.text.toString().isNotEmpty() && isTextMatch(text, optionA.text.toString())) {
            optionA.performClick()
            //performDebouncedOptionClick("optionA", optionA.text.toString())
            showScoreBoostAnimation(false)
            chatterActivity.updateTotalScore(25)
        } else if (optionB.text.toString().isNotEmpty() && isTextMatch(
                text,
                optionB.text.toString()
            )
        ) {
            optionB.performClick()
            //performDebouncedOptionClick("optionB", optionB.text.toString())
            showScoreBoostAnimation(false)
            chatterActivity.updateTotalScore(25)
        } else if (optionC.text.toString().isNotEmpty() && isTextMatch(
                text,
                optionC.text.toString()
            )
        ) {
            optionC.performClick()
            //performDebouncedOptionClick("optionC", optionC.text.toString())
            showScoreBoostAnimation(false)
            chatterActivity.updateTotalScore(25)
        } else {
            Toast.makeText(context, "Couldn't match your sentence", Toast.LENGTH_LONG).show()
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

    fun showMenu() {
        message_menu_root_layout.visibility = View.VISIBLE
    }

    fun hideMenu() {
        message_menu_root_layout.visibility = View.GONE
    }

    companion object {
        private const val MATCH_PERCENTAGE = 0.7
        fun newInstance(): MessageMenuOptionsFragment {
            return MessageMenuOptionsFragment()
        }
    }
}
