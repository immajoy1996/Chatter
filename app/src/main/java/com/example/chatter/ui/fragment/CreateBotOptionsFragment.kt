package com.example.chatter.ui.fragment

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.example.chatter.ui.activity.CreateChatActivity
import com.example.chatter.extra.Preferences
import com.example.chatter.R
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_chatter.*
import kotlinx.android.synthetic.main.fragment_create_bot_options.*
import kotlinx.android.synthetic.main.fragment_message_options.conversation_end_message
import kotlinx.android.synthetic.main.fragment_message_options.optionA
import kotlinx.android.synthetic.main.fragment_message_options.optionB
import kotlinx.android.synthetic.main.fragment_message_options.optionC
import kotlinx.android.synthetic.main.fragment_message_options.options_menu_layout
import kotlinx.android.synthetic.main.fragment_message_options.restart
import java.util.*
import kotlin.concurrent.schedule

class CreateBotOptionsFragment : BaseFragment() {
    private val chatterActivity by lazy {
        activity as CreateChatActivity
    }

    private lateinit var database: DatabaseReference
    private lateinit var timerTask: TimerTask
    private lateinit var preferences: Preferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_create_bot_options, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        database = FirebaseDatabase.getInstance().reference
        context?.let {
            preferences = Preferences(it)
        }
        setUpCreateBotSubmitButton()
        checkForConversation()
    }

    override fun onResume() {
        super.onResume()
        optionA.setHint("Type message here")
        optionB.setHint("Type message here")
        optionC.setHint("Type message here")
        optionA.setText("")
        optionB.setText("")
        optionC.setText("")
    }

    private fun setUpCreateBotSubmitButton() {
        create_bot_submit.setOnClickListener {
            optionA.hideKeyboard()
            updateOptionText(optionA, "optionA")
            updateOptionText(optionB, "optionB")
            updateOptionText(optionC, "optionC")
        }
    }

    private fun updateOptionText(option: TextView, optionType: String) {
        if (option.text.toString().isNotEmpty()) {
            database.child(chatterActivity.currentPath).child(optionType).child("text")
                .setValue(option.text.toString())
                .addOnSuccessListener {
                    Toast.makeText(context, "Update successful", Toast.LENGTH_LONG)
                        .show()
                }.addOnFailureListener {
                    Toast.makeText(context, it.localizedMessage, Toast.LENGTH_LONG)
                        .show()
                }
        } else {
            val animWobble = AnimationUtils.loadAnimation(
                context,
                R.anim.wobble
            )
            option.startAnimation(animWobble)
        }
    }

    private fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }

    private fun setUpMenu() {
        makeLayoutVisible()
        initializeScrollContainerAndOptionButtons()
        //checkForEasterEggs()
    }

    private fun makeLayoutVisible() {
        options_menu_layout.visibility = View.VISIBLE
        conversation_end_message.visibility = View.GONE
    }

    private fun checkForConversation() {
        val pathRef = database.child(chatterActivity.currentPath)
        val messageListener = baseValueEventListener { dataSnapshot ->
            setUpMenu()
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
        option_submit: ImageView,
        optionType: String
    ) {
        val path = currentPath + optionType + "/"
        option_submit.setOnClickListener {
            chatterActivity.let {
                option.hideKeyboard()
                it.currentPath = path
                it.removeOptionsMenu()
                var userText = option.text.toString()
                //it.addUserMessage(userText)
                it.handleNewMessageLogic(userText)
                getBotResponse(it.currentPath)
            }
        }
    }

    private fun setUpAllOptionClickListeners(currentPath: String) {
        setUpMenuOptionClickListener(currentPath, optionA, optionA_submit, "optionA")
        setUpMenuOptionClickListener(currentPath, optionB, optionB_submit, "optionB")
        setUpMenuOptionClickListener(currentPath, optionC, optionC_submit, "optionC")
    }

    private fun getBotResponse(path: String) {
        //Toast.makeText(context, path, Toast.LENGTH_SHORT).show()
        chatterActivity.removeOptionsMenu()
        val pathReference = database.child(path + "/botMessage")
        chatterActivity.disableNextButton()
        chatterActivity.addSpaceText()
        chatterActivity.handleNewMessageLogic("")
        val messageListener = chatterActivity.baseValueEventListener { dataSnapshot ->
            /*setTimerTask("showBotIsTyping", 700, {
                chatterActivity.addSpaceText()
                chatterActivity.showBotIsTypingView()
            })*/
            setTimerTask("loadOptionsMenu", 1000, {
                dataSnapshot.value?.let {
                    chatterActivity.replaceBotIsTyping(it.toString())
                }
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
                    //chatterActivity.loadEasterEggFragment(title, points, image)
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
            dataSnapshot.value?.let {
                option.text = it.toString()
            }
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

    private fun performOptionClick(optionType: String, userText: String) {
        chatterActivity.let {
            val path = it.currentPath + optionType + "/"
            it.currentPath = path
            chatterActivity.removeOptionsMenu()
            //chatterActivity.addUserMessage(userText)
            it.handleNewMessageLogic(userText)
            getBotResponse(it.currentPath)
        }
    }

    fun selectOptionsWithVoice(text: String) {
        if (isTextMatch(text, optionA.text.toString())) {
            optionA.performClick()
            //performOptionClick("optionA", optionA.text.toString())
            showScoreBoostAnimation(false)
        } else if (isTextMatch(text, optionB.text.toString())) {
            optionB.performClick()
            //performOptionClick("optionB", optionB.text.toString())
            showScoreBoostAnimation(false)
        } else if (isTextMatch(text, optionC.text.toString())) {
            optionC.performClick()
            showScoreBoostAnimation(false)
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

    companion object {
        private const val MATCH_PERCENTAGE = 0.7
        fun newInstance(): CreateBotOptionsFragment {
            return CreateBotOptionsFragment()
        }
    }
}
