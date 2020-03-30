package com.example.chatter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import com.example.chatter.EnterUsernameFragment.Companion.ENTER_PASSWORD
import com.example.chatter.EnterUsernameFragment.Companion.ENTER_USERNAME
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_chatter.*
import kotlinx.android.synthetic.main.fragment_enter_username.*
import java.util.*
import kotlin.concurrent.schedule

class SignInActivity : AppCompatActivity() {
    private var scenario: Int? = null
    private var menuOptionsFragment = SignInOptionsFragment()
    private var signInErrorFragment = SignInErrorFragment()
    private var usernameFragment = EnterUsernameFragment.newInstance(ENTER_USERNAME)
    private var passwordFragment = EnterUsernameFragment.newInstance(ENTER_PASSWORD)

    var username: String? = null
    var password: String? = null
    var reenterPassword: String? = null

    private var retrievingOptionsFragment =
        RetrievingOptionsFragment.newInstance("Retrieving options")

    var prevMsgId = ConstraintSet.PARENT_ID
    var newMsgId = -1
    var newSide = "left"
    var isFirst = true
    var msgCount = 0

    private var profileImgView: ImageView? = null
    private var translationImgView: CircleImageView? = null
    private var messageTextView: TextView? = null

    private var constraintSet = ConstraintSet()

    val userMessagesFragmentArray = arrayListOf<Int>(0, 1)
    val botMessagesArray = arrayListOf<String>(
        "Ok. What's your username?",
        "Ok. What's your password?",
        "Ok, you're all set! Logging in."
    )

    var userPosition = 0
    var botPosition = 0

    var login_error:Boolean=false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)
        initializeMessagesContainer()
        showFirstBotMessage()
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(optionsPopupContainer.id, fragment)
            .addToBackStack(fragment.javaClass.name)
            .commit()
    }

    fun setScenario(curScenario: Int) {
        scenario = curScenario
    }

    private fun replaceBotIsTyping(msg: String) {
        val textView = messagesInnerLayout.getViewById(getMessageTextBubbleId()) as TextView
        textView.text = msg
    }

    private fun getMessageTextBubbleId(): Int {
        return 10 * msgCount
    }

    private fun showBotIsTypingView() {
        handleNewMessageLogic("bot is typing ...")
    }

    fun runMessageFlow(msg: String) {
        handleNewMessageLogic(msg)
        getBotResponse()
    }

    fun removeOptionsMenu() {
        supportFragmentManager.popBackStack()
    }

    fun getBotResponse() {
        removeOptionsMenu()
        Timer("showOptionsTimer", false).schedule(700) {
            runOnUiThread {
                addSpaceText()
                showBotIsTypingView()
            }
        }

        Timer("showOptionsTimer", false).schedule(2000) {
            runOnUiThread {
                replaceBotIsTyping(botMessagesArray[botPosition++])
                getCurrentOptionsMenu()
            }
        }
    }

    fun loadRetrievingOptionsFragment() {
        loadFragment(retrievingOptionsFragment)
    }

    fun removeRetrievingOptionsFragment() {
        supportFragmentManager.popBackStack()
    }

    fun loadOptionsFragment(fragment: Fragment) {
        loadRetrievingOptionsFragment()
        Timer("showOptionsTimer", false).schedule(2000) {
            removeRetrievingOptionsFragment()
            loadFragment(fragment)
        }
    }

    private fun getCurrentOptionsMenu() {
        if (userPosition > userMessagesFragmentArray.size) return
        else if (userPosition == userMessagesFragmentArray.size) {
            if (credentialsAreValid()) signIn()
            else {
                loadSignInErrorFragment()
            }
        } else {
            when (userMessagesFragmentArray[userPosition]) {
                0 -> {
                    loadUsernameFragment()
                }
                1 -> {
                    loadPasswordFragment()
                }
            }
            userPosition++
        }
    }

    private fun credentialsAreValid(): Boolean {
        return (username == "test" && password == "password")
    }

    fun signIn() {
        retrievingOptionsFragment = RetrievingOptionsFragment.newInstance("Logging In")
        loadRetrievingOptionsFragment()
        Timer("showOptionsTimer", false).schedule(2000) {
            removeRetrievingOptionsFragment()
            startActivity(Intent(this@SignInActivity, DashboardActivity::class.java))
        }
    }

    fun signInAsGuest() {
        retrievingOptionsFragment = RetrievingOptionsFragment.newInstance("Guest Mode")
        loadRetrievingOptionsFragment()
        Timer("showOptionsTimer", false).schedule(2000) {
            removeRetrievingOptionsFragment()
            startActivity(Intent(this@SignInActivity, DashboardActivity::class.java))
        }
    }

    private fun loadPasswordFragment() {
        loadOptionsFragment(passwordFragment)
    }

    private fun loadSignInOptions() {
        supportFragmentManager
            .beginTransaction()
            .replace(optionsPopupContainer.id, menuOptionsFragment)
            .addToBackStack(menuOptionsFragment.javaClass.name)
            .commit()
    }

    private fun loadUsernameFragment() {
        loadOptionsFragment(usernameFragment)
    }

    private fun loadSignInErrorFragment(){
        supportFragmentManager
            .beginTransaction()
            .replace(optionsPopupContainer.id, signInErrorFragment)
            .addToBackStack(signInErrorFragment.javaClass.name)
            .commit()
    }

    private fun showFirstBotMessage() {
        handleNewMessageLogic("Hello, what would you like to do?")
        loadSignInOptions()
    }

    private fun initializeMessagesContainer() {
        constraintSet.clone(messagesInnerLayout)
    }

    private fun handleNewMessageLogic(str: String) {
        msgCount++
        newMsgId = 10 * msgCount
        addMessage(str)
        if (newSide == "left") {
            newSide = "right"
        } else {
            newSide = "left"
        }
        if (isFirst) isFirst = false
        prevMsgId = newMsgId
    }

    private fun addMessage(msg: String) {
        setUpMessageTextView(msg)
        setupProfileImgView()
        addConstraintToProfileImageView()
        addConstraintsForMessageTextView()
        addConstraintToTranslationImageView()
        addGeneralConstraintsForProfileImageAndMessageText()
        setConstraintsToLayout()
    }

    fun setUpMessageTextView(msg: String) {
        messageTextView = TextView(this)
        messageTextView?.apply {
            if (newSide == "right") {
                setBackgroundResource(R.drawable.option_bubble)
                setTextColor(Color.parseColor("#FFFFFF"))
            } else {
                setBackgroundResource(R.drawable.message_bubble_selector)
                setTextColor(Color.parseColor("#696969"))
            }
            setPadding(ChatterActivity.MESSAGE_PADDING)
            setId(newMsgId)

            text = msg
            textSize = ChatterActivity.TEXT_SIZE_MESSAGE
            if (newSide == "right") {
                isFocusableInTouchMode = true
                requestFocus()
            }
        }
    }

    fun setupProfileImgView() {
        profileImgView = ImageView(this)
        profileImgView?.apply {
            id = getIdProfileImageView()
            setImageDrawable(ContextCompat.getDrawable(context, R.drawable.business_profile))
        }
    }

    fun getIdProfileImageView(): Int {
        return 10 * msgCount + 1
    }

    fun addConstraintToProfileImageView() {
        if (newSide == "left") {
            profileImgView?.apply {
                constraintSet.constrainHeight(id, ChatterActivity.PROFILE_IMAGE_SIZE)
                constraintSet.constrainWidth(id, ChatterActivity.PROFILE_IMAGE_SIZE)
                addViewToLayout(this)
            }
        }
    }

    fun addConstraintsForMessageTextView() {
        messageTextView?.apply {
            constraintSet.constrainHeight(id, ViewGroup.LayoutParams.WRAP_CONTENT)
            constraintSet.constrainWidth(id, ChatterActivity.MESSAGE_BUBBLE_WIDTH)
        }
        addViewToLayout(messageTextView as TextView)
    }

    fun addConstraintToTranslationImageView() {
        translationImgView?.apply {
            constraintSet.constrainHeight(id, ChatterActivity.TRANSLATION_IMAGE_SIZE)
            constraintSet.constrainWidth(id, ChatterActivity.TRANSLATION_IMAGE_SIZE)
            addViewToLayout(this)
        }
    }

    private fun addViewToLayout(view: View) {
        messagesInnerLayout.addView(view)
    }

    fun addGeneralConstraintsForProfileImageAndMessageText() {
        var position = -1
        if (isFirst) position = ConstraintSet.TOP
        else position = ConstraintSet.BOTTOM

        val textView = messageTextView as TextView
        val profileImg = profileImgView as ImageView

        if (newSide == "left") {
            constraintSet.connect(
                profileImg.id,
                ConstraintSet.BOTTOM,
                textView.id,
                ConstraintSet.BOTTOM,
                0
            )

            constraintSet.connect(
                textView.id,
                ConstraintSet.START,
                profileImg.id,
                ConstraintSet.END,
                0
            )

            constraintSet.connect(
                textView.id,
                ConstraintSet.TOP,
                prevMsgId,
                position,
                ChatterActivity.MESSAGE_VERTICAL_SPACING
            )

        } else if (newSide == "right") {
            constraintSet.connect(
                textView.id,
                ConstraintSet.TOP,
                prevMsgId,
                position,
                ChatterActivity.MESSAGE_VERTICAL_SPACING
            )
            constraintSet.connect(
                textView.id,
                ConstraintSet.END,
                ConstraintSet.PARENT_ID,
                ConstraintSet.END,
                0
            )
        }
    }

    private fun setConstraintsToLayout() {
        constraintSet.applyTo(messagesInnerLayout)
    }

    fun addSpaceText() {
        val spaceMsgId = getIdForSpaceView()
        addSpaceMessage(spaceMsgId)
    }

    private fun getIdForSpaceView(): Int {
        return 1000 * msgCount
    }

    private fun addSpaceMessage(id: Int) {
        setUpSpaceView(id)
        addConstraintsForSpaceView()
        addGeneralConstraintsForSpaceView()
        setConstraintsToLayout()
    }

    private fun setUpSpaceView(spaceId: Int) {
        messageTextView = TextView(this)
        messageTextView?.apply {
            if (newSide == "left") {
                setBackgroundColor(Color.parseColor("#dcdcdc"))
                setTextColor(Color.parseColor("#dcdcdc"))
            }
            setPadding(ChatterActivity.MESSAGE_PADDING)
            setId(spaceId)
            text = "helloddsklkdkdkdkdkkdkdkdkkdkdkkddsfsdfdsfdsfdsfdsfdsfd" +
                    "dfsdfdsfdsfdsfdsfdsfdsdsfdsfds" +
                    "dsfdsfdsfds"
            textSize = ChatterActivity.TEXT_SIZE_MESSAGE
            isFocusableInTouchMode = true
            requestFocus()
        }
    }

    private fun addGeneralConstraintsForSpaceView() {
        val textView = messageTextView as TextView
        if (newSide == "left") {
            constraintSet.connect(
                textView.id,
                ConstraintSet.TOP,
                prevMsgId,
                ConstraintSet.BOTTOM,
                ChatterActivity.MESSAGE_VERTICAL_SPACING
            )
        }
    }

    private fun addConstraintsForSpaceView() {
        messageTextView?.apply {
            constraintSet.constrainHeight(id, ViewGroup.LayoutParams.WRAP_CONTENT)
            constraintSet.constrainWidth(id, ChatterActivity.MESSAGE_BUBBLE_WIDTH)
        }
        addViewToLayout(messageTextView as TextView)
    }

    fun refreshSignInFlow(){
        finish()
        overridePendingTransition(0, 0);
        startActivity(intent)
        overridePendingTransition(0, 0);
    }

    override fun onBackPressed() {
        //deactivate back button
    }

    companion object {
        const val SIGN_IN_SEQUENCE = 0
    }
}
