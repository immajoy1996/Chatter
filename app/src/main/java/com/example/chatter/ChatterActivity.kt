package com.example.chatter

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.firebase.database.*
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_chatter.*
import kotlinx.android.synthetic.main.bottom_nav_bar.*
import java.util.*
import kotlin.concurrent.schedule


class ChatterActivity : AppCompatActivity(), StoryBoardFinishedInterface {

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

    var prevMsgId = ConstraintSet.PARENT_ID
    var newMsgId = -1
    var newSide = "left"
    var isFirst = true
    var msgCount = 0

    private var profileImgView: ImageView? = null
    private var translationImgView: CircleImageView? = null
    private var audioImgView: CircleImageView? = null
    private var messageTextView: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chatter)
        database = FirebaseDatabase.getInstance().reference
        setUpStoryBoardFragments()
        setUpNavButtons()
        loadFirstStoryBoardFragment()
    }

    private fun setUpStoryBoardFragments() {
        val botTitle = intent.getStringExtra(BOT_TITLE)
        storyBoardOneFragment = StoryBoardOneFragment.newInstance(botTitle)
        storyBoardTwoFragment = StoryBoardTwoFragment.newInstance(botTitle)
    }

    private fun setUpNavButtons() {
        button_back.setOnClickListener { finish() }
        button_next.setOnClickListener {
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

    fun loadSecondStoryBoardFragment() {
        supportFragmentManager
            .beginTransaction()
            .replace(root_container.id, storyBoardTwoFragment)
            .addToBackStack(storyBoardTwoFragment.javaClass.name)
            .commit()
    }

    fun loadEasterEggFragment(title: String, points: Long,imageSrc:String) {
        easterEggFragment = EasterEggFragment.newInstance(title, points,imageSrc)
        supportFragmentManager
            .beginTransaction()
            .replace(root_container.id, easterEggFragment)
            .addToBackStack(easterEggFragment.javaClass.name)
            .commit()
    }

    private fun showFirstBotMessage() {
        val firstMessageReference = database.child("botMessage")

        val messageListener = messageOptionsFragment.createMessageListener { botMessage ->
            handleNewMessageLogic(botMessage)
            loadOptionsMenu()
        }
        firstMessageReference.addValueEventListener(messageListener)
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(optionsPopupContainer.id, fragment)
            .addToBackStack(fragment.javaClass.name)
            .commit()
    }

    fun loadOptionsMenu() {
        loadRetrievingOptionsFragment()
        Timer("showOptionsTimer", false).schedule(2000) {
            removeRetrievingOptionsFragment()
            loadFragment(messageOptionsFragment)
        }
    }

    fun removeOptionsMenu() {
        supportFragmentManager.popBackStack()
    }

    fun initializeMessagesContainer() {
        constraintSet.clone(messagesInnerLayout)
    }

    fun addMessage(msg: String) {
        setUpMessageTextView(msg)
        setupProfileImgView()
        //setupTranslationImage()
        //setupAudioImage()
        addConstraintToProfileImageView()
        addConstraintsForMessageTextView()
        addConstraintToTranslationImageView()
        //addConstraintToAudioImageView()
        addGeneralConstraintsForProfileImageAndMessageText()
        //addGeneralConstraintsForTranslationImage()
        //addGeneralConstraintsForAudioImage()
        setConstraintsToLayout()
    }

    fun addGeneralConstraintsForTranslationImage() {

        val textView = messageTextView as TextView
        val translationImg = translationImgView as ImageView
        val audioImgView = audioImgView as CircleImageView

        if (newSide == "left") {
            constraintSet.connect(
                translationImg.id,
                ConstraintSet.BOTTOM,
                textView.id,
                ConstraintSet.BOTTOM,
                0
            )

            constraintSet.connect(
                translationImg.id,
                ConstraintSet.TOP,
                textView.id,
                ConstraintSet.TOP,
                0
            )

            constraintSet.connect(
                translationImg.id,
                ConstraintSet.START,
                textView.id,
                ConstraintSet.END,
                TRANSLATION_IMAGE_SPACING
            )

        } else if (newSide == "right") {

            constraintSet.connect(
                translationImg.id,
                ConstraintSet.BOTTOM,
                textView.id,
                ConstraintSet.BOTTOM,
                0
            )

            constraintSet.connect(
                translationImg.id,
                ConstraintSet.TOP,
                textView.id,
                ConstraintSet.TOP,
                0
            )

            constraintSet.connect(
                translationImg.id,
                ConstraintSet.END,
                audioImgView.id,
                ConstraintSet.START,
                TRANSLATION_IMAGE_SPACING
            )
        }
    }

    fun addGeneralConstraintsForAudioImage() {
        val textView = messageTextView as TextView
        val audioImgView = audioImgView as CircleImageView
        val translationImgView = translationImgView as CircleImageView

        if (newSide == "left") {
            constraintSet.connect(
                audioImgView.id,
                ConstraintSet.BOTTOM,
                textView.id,
                ConstraintSet.BOTTOM,
                0
            )

            constraintSet.connect(
                audioImgView.id,
                ConstraintSet.TOP,
                textView.id,
                ConstraintSet.TOP,
                0
            )

            constraintSet.connect(
                audioImgView.id,
                ConstraintSet.START,
                translationImgView.id,
                ConstraintSet.END,
                TRANSLATION_IMAGE_SPACING
            )

        } else if (newSide == "right") {

            constraintSet.connect(
                audioImgView.id,
                ConstraintSet.BOTTOM,
                textView.id,
                ConstraintSet.BOTTOM,
                0
            )

            constraintSet.connect(
                audioImgView.id,
                ConstraintSet.TOP,
                textView.id,
                ConstraintSet.TOP,
                0
            )

            constraintSet.connect(
                audioImgView.id,
                ConstraintSet.END,
                textView.id,
                ConstraintSet.START,
                TRANSLATION_IMAGE_SPACING
            )
        }
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

    fun setupTranslationImage() {
        translationImgView = CircleImageView(this)
        translationImgView?.apply {
            id = getIdForTranslationImageView()
            setImageDrawable(ContextCompat.getDrawable(context, R.drawable.translation))
            //setBackground(getDrawable(R.drawable.circular_background))
        }
        setUpTranslationIconClickListener()
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

    fun setUpTranslationIconClickListener() {
        val copyPath = currentPath
        val copySide = newSide
        val copyMessageId = getMessageTextBubbleId()
        (translationImgView as ImageView).setOnClickListener {
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

        val messageListener = messageOptionsFragment.createMessageListener { translation ->
            val customText = SpannableString(translation)
            if (shouldTranslateToSpanish) {
                customText.setSpan(RelativeSizeSpan(.1f), 0, 2, 0)
            }
            (messagesInnerLayout.getViewById(msgId) as TextView).text = customText
        }
        firstMessageReference.addValueEventListener(messageListener)
    }

    fun setupAudioImage() {
        audioImgView = CircleImageView(this)
        audioImgView?.apply {
            id = getIdForAudioImageView()
            setImageDrawable(ContextCompat.getDrawable(context, R.drawable.audio))
            //setBackground(getDrawable(R.drawable.circular_background))
        }
    }

    fun addConstraintToTranslationImageView() {
        translationImgView?.apply {
            constraintSet.constrainHeight(id, TRANSLATION_IMAGE_SIZE)
            constraintSet.constrainWidth(id, TRANSLATION_IMAGE_SIZE)
            addViewToLayout(this)
        }
    }

    fun addConstraintToAudioImageView() {
        audioImgView?.apply {
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

    fun removeBotIsTypingView() {
        val textViewId = getIdForBotIsTypingTextView()
        val textView = findViewById<TextView>(textViewId)
        messagesInnerLayout.removeView(textView)
    }

    fun getIdForBotIsTypingTextView(): Int {
        return 10 * msgCount + 9
    }

    fun getIdProfileImageView(): Int {
        return 10 * msgCount + 1
    }

    fun getIdForTranslationImageView(): Int {
        return 10 * msgCount + 3
    }

    fun getIdForAudioImageView(): Int {
        return 10 * msgCount + 7
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

    fun applyConstraintsToBotIsTypingTextView() {
        messageTextView?.let {
            constraintSet.constrainHeight(it.id, ViewGroup.LayoutParams.WRAP_CONTENT)
            constraintSet.constrainWidth(it.id, MESSAGE_BUBBLE_WIDTH)
        }
        var position = -1
        if (isFirst) position = ConstraintSet.TOP
        else position = ConstraintSet.BOTTOM

        if (newSide == "left") {
            constraintSet.connect(
                (messageTextView as TextView).id,
                ConstraintSet.TOP,
                prevMsgId,
                position,
                MESSAGE_VERTICAL_SPACING
            )
        }
    }

    fun showBotIsTypingTextView() {
        val textViewId = getIdForBotIsTypingTextView()
        messageTextView = TextView(this)
        messageTextView?.apply {
            setPadding(MESSAGE_PADDING)
            setId(textViewId)
            text = "CleverBot is typing ..."
            isFocusableInTouchMode = true
            textSize = BOT_IS_TYPING_MESSAGE_SIZE
            requestFocus()
            setTextColor(Color.parseColor("#0084FF"))
        }
        addViewToLayout(messageTextView as TextView)
        applyConstraintsToBotIsTypingTextView()
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

    fun handleNewMessageLogic(str: String) {
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
