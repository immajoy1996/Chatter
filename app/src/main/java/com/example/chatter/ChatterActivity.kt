package com.example.chatter

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_chatter.*
import kotlinx.android.synthetic.main.bottom_nav_bar.*
import kotlinx.android.synthetic.main.fragment_message_options.*
import kotlinx.android.synthetic.main.top_bar.*
import java.util.*
import kotlin.concurrent.schedule


class ChatterActivity : BaseChatActivity(), StoryBoardFinishedInterface {

    private var constraintSet = ConstraintSet()

    private val messageOptionsFragment = MessageMenuOptionsFragment.newInstance()
    private val retrievingOptionsFragment =
        RetrievingOptionsFragment.newInstance("Retrieving options")
    private val vocabFragment = VocabFragment()
    private lateinit var storyBoardOneFragment: StoryBoardOneFragment
    private lateinit var storyBoardTwoFragment: StoryBoardTwoFragment
    private lateinit var easterEggFragment: EasterEggFragment

    private lateinit var database: DatabaseReference

    var currentPath = ""

    private var profileImgView: ImageView? = null
    private var translationImgView: CircleImageView? = null
    private var messageTextView: TextView? = null

    private lateinit var timerTask: TimerTask
    private lateinit var botTitle: String

    private var isPressed: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chatter)
        database = FirebaseDatabase.getInstance().reference
        setUpTopBar()
        setUpStoryBoardFragments()
        setUpNavButtons()
        loadFirstStoryBoardFragment()
    }

    override fun setUpTopBar() {
        top_bar_title.text = "Chatter"
        top_bar_mic.setOnClickListener {
            toggleIsChatterActivity(true)
            startListening()
        }
    }

    override fun showFirstBotMessage() {
        val pathReference = database.child("botMessage")
        disableNextButton()
        val messageListener = baseValueEventListener { dataSnapshot ->
            val botMessage = dataSnapshot.value.toString()
            handleNewMessageLogic(botMessage)
            loadOptionsMenu()
        }
        pathReference.addValueEventListener(messageListener)
    }

    override fun initializeMessagesContainer() {
        constraintSet.clone(messagesInnerLayout)
    }

    override fun addMessage(msg: String) {
        setUpMessageTextView(msg)
        setupProfileImgView()
        addConstraintToProfileImageView()
        addConstraintsForMessageTextView()
        addConstraintToTranslationImageView()
        addGeneralConstraintsForProfileImageAndMessageText()
        setConstraintsToLayout()
    }

    val setTimerTask: (name: String, delay: Long, () -> Unit) -> TimerTask = { name, delay, doit ->
        timerTask = Timer(name, false).schedule(delay) {
            runOnUiThread {
                doit()
            }
        }
        timerTaskArray.add(timerTask)
        timerTask
    }

    private fun setUpStoryBoardFragments() {
        botTitle = intent.getStringExtra(BOT_TITLE)
        storyBoardOneFragment = StoryBoardOneFragment.newInstance(botTitle)
        storyBoardTwoFragment = StoryBoardTwoFragment.newInstance(botTitle)
    }

    private fun setUpNavButtons() {
        button_back.setOnClickListener {
            toggleRestartFlag(false)
            finish()
        }
        button_next.setOnClickListener {
            disposeListeners()
            supportFragmentManager.popBackStack()
            loadVocabFragment()
        }
    }

    override fun onStoriesFinished() {
        initializeMessagesContainer()
        showFirstBotMessage()
    }

    fun closeEasterEggFragment() {
        supportFragmentManager?.popBackStack(
            easterEggFragment.javaClass.name,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
    }

    fun loadBotDescriptionFragment() {
        loadFragmentIntoRoot(BotDescriptionFragment())
    }

    fun loadVocabFragment() {
        supportFragmentManager
            .beginTransaction()
            .replace(root_container.id, vocabFragment)
            .addToBackStack(vocabFragment.javaClass.name)
            .commit()
    }

    fun loadFirstStoryBoardFragment() {
        storyBoardOneFragment.let {
            supportFragmentManager
                .beginTransaction()
                .replace(root_container.id, it)
                .addToBackStack(it.javaClass.name)
                .commit()
        }
    }

    fun loadEasterEggFragment(title: String, points: Long, imageSrc: String) {
        easterEggFragment = EasterEggFragment.newInstance(title, points, imageSrc)
        supportFragmentManager
            .beginTransaction()
            .replace(root_container.id, easterEggFragment)
            .addToBackStack(easterEggFragment.javaClass.name)
            .commit()
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(optionsPopupContainer.id, fragment)
            .addToBackStack(fragment.javaClass.name)
            .commit()
    }

    private fun loadFragmentIntoRoot(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(root_container.id, fragment)
            .addToBackStack(fragment.javaClass.name)
            .commit()
    }

    fun loadOptionsMenu() {
        disableNextButton()
        loadRetrievingOptionsFragment()
        setTimerTask("loadMessageOptionsFragment", 2000, {
            removeRetrievingOptionsFragment()
            loadFragment(messageOptionsFragment)
            enableNextButton()
        })
    }

    fun removeOptionsMenu() {
        supportFragmentManager.popBackStack()
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
                MESSAGE_VERTICAL_SPACING
            )

        } else if (newSide == "right") {
            constraintSet.connect(
                textView.id,
                ConstraintSet.TOP,
                prevMsgId,
                position,
                MESSAGE_VERTICAL_SPACING
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

    fun addConstraintsForMessageTextView() {
        messageTextView?.apply {
            constraintSet.constrainHeight(id, ViewGroup.LayoutParams.WRAP_CONTENT)
            constraintSet.constrainWidth(id, MESSAGE_BUBBLE_WIDTH)
        }
        addViewToLayout(messageTextView as TextView)
    }

    fun setUpMessageBubbleClickListener() {
        val copyPath = currentPath
        val copySide = newSide
        val copyMessageId = getMessageTextBubbleId()
        val textView = messageTextView
        textView?.setOnClickListener {
            //(messagesInnerLayout.getViewById(copyMessageId) as TextView).text = "translating ... "
            showTranslation(copySide, copyPath, copyMessageId)
        }
    }

    fun getMessageTextBubbleId(): Int {
        return 10 * msgCount
    }

    fun showTranslation(side: String, path: String, msgId: Int) {
        val curText = (messagesInnerLayout.getViewById(msgId) as TextView).text
        var translationPath = ""
        var shouldTranslateToSpanish: Boolean = false
        if (!curText.startsWith("T:")) {
            shouldTranslateToSpanish = true
            if (side == "right") translationPath = "userMessageTranslation"
            else translationPath = "botMessageTranslation"
        } else {
            shouldTranslateToSpanish = false
            if (side == "right") translationPath = "text"
            else translationPath = "botMessage"
        }
        val firstMessageReference = database.child(path).child(translationPath)

        val messageListener = baseValueEventListener { dataSnapshot ->
            val translation = dataSnapshot.value.toString()
            val customText = SpannableString(translation)
            if (shouldTranslateToSpanish) {
                customText.setSpan(RelativeSizeSpan(.1f), 0, 2, 0)
            }
            (messagesInnerLayout.getViewById(msgId) as TextView).text = customText
        }
        firstMessageReference.addValueEventListener(messageListener)
    }

    fun addConstraintToTranslationImageView() {
        translationImgView?.apply {
            constraintSet.constrainHeight(id, TRANSLATION_IMAGE_SIZE)
            constraintSet.constrainWidth(id, TRANSLATION_IMAGE_SIZE)
            addViewToLayout(this)
        }
    }

    fun setupProfileImgView() {
        profileImgView = ImageView(this)
        profileImgView?.apply {
            id = getIdProfileImageView()
            setImageDrawable(ContextCompat.getDrawable(context, R.drawable.business_profile))
            setOnClickListener { loadBotDescriptionFragment() }
        }
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
            setPadding(MESSAGE_PADDING)
            setId(newMsgId)

            text = msg
            textSize = TEXT_SIZE_MESSAGE
            if (newSide == "right") {
                isFocusableInTouchMode = true
                requestFocus()
            }
        }
        setUpMessageBubbleClickListener()
    }

    fun addConstraintToProfileImageView() {
        if (newSide == "left") {
            profileImgView?.apply {
                constraintSet.constrainHeight(id, PROFILE_IMAGE_SIZE)
                constraintSet.constrainWidth(id, PROFILE_IMAGE_SIZE)
                addViewToLayout(this)
            }
        }
    }

    fun getIdProfileImageView(): Int {
        return 10 * msgCount + 1
    }

    private fun getIdForSpaceView(): Int {
        return 1000 * msgCount
    }

    fun loadRetrievingOptionsFragment() {
        loadFragment(retrievingOptionsFragment)
    }

    fun removeRetrievingOptionsFragment() {
        supportFragmentManager.popBackStack()
    }

    private fun addViewToLayout(view: View) {
        messagesInnerLayout.addView(view)
    }

    private fun setConstraintsToLayout() {
        constraintSet.applyTo(messagesInnerLayout)
    }

    fun showBotIsTypingView() {
        handleNewMessageLogic("bot is typing ...")
    }

    fun replaceBotIsTyping(msg: String) {
        val textView = messagesInnerLayout.getViewById(getMessageTextBubbleId()) as TextView
        textView.text = msg
    }

    fun addSpaceText() {
        val spaceMsgId = getIdForSpaceView()
        addSpaceMessage(spaceMsgId)
    }

    private fun addSpaceMessage(id: Int) {
        setUpSpaceView(id)
        addConstraintsForSpaceView()
        addGeneralConstraintsForSpaceView()
        setConstraintsToLayout()
    }

    fun getCurrentBotTitle(): String {
        return botTitle
    }

    private fun setUpSpaceView(spaceId: Int) {
        messageTextView = TextView(this)
        messageTextView?.apply {
            if (newSide == "left") {
                setBackgroundColor(Color.parseColor("#dcdcdc"))
                setTextColor(Color.parseColor("#dcdcdc"))
            }
            setPadding(MESSAGE_PADDING)
            setId(spaceId)
            text = "helloddsklkdkdkdkdkkdkdkdkkdkdkkddsfsdfdsfdsfdsfdsfdsfd" +
                    "dfsdfdsfdsfdsfdsfdsfdsdsfdsfds" +
                    "dsfdsfdsfds"
            textSize = TEXT_SIZE_MESSAGE
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
                MESSAGE_VERTICAL_SPACING
            )
        }
    }

    private fun addConstraintsForSpaceView() {
        messageTextView?.apply {
            constraintSet.constrainHeight(id, ViewGroup.LayoutParams.WRAP_CONTENT)
            constraintSet.constrainWidth(id, MESSAGE_BUBBLE_WIDTH)
        }
        addViewToLayout(messageTextView as TextView)
    }

    override fun onResults(results: Bundle?) {
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (isVocabFragment()) {
            matches?.let {
                vocabFragment.setUpSearch(it[0])
            }
            toggleIsVocabFragmentFlag(false)
        } else if (isChatterActivity()) {
            if (messageOptionsFragment.isVisible) {
                matches?.let {
                    messageOptionsFragment.selectOptionsWithVoice(it[0])
                    toggleIsVocabFragmentFlag(false)
                }
            }
        }
    }

    companion object {
        const val MESSAGE_VERTICAL_SPACING = 100
        const val MESSAGE_BUBBLE_WIDTH = 800
        const val TEXT_SIZE_MESSAGE = 20f
        const val PROFILE_IMAGE_SIZE = 100
        const val MESSAGE_PADDING = 35
        const val BOT_IS_TYPING_MESSAGE_SIZE = 17f
        const val TRANSLATION_IMAGE_SPACING = 70
        const val TRANSLATION_IMAGE_SIZE = 100
        const val BOT_CATALOG = "BOT_CATALOG"
        const val BOT_TITLE = "BOT_TITLE"
    }
}
