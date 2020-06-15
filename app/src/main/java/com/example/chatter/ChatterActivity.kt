package com.example.chatter

import android.graphics.Color
import android.graphics.drawable.AnimationDrawable
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.VibrationEffect
import android.os.Vibrator
import android.speech.SpeechRecognizer
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_chatter.*
import kotlinx.android.synthetic.main.bottom_nav_bar.*
import kotlinx.android.synthetic.main.top_bar.*

class ChatterActivity : BaseChatActivity(), StoryBoardFinishedInterface {

    private var constraintSet = ConstraintSet()

    private val messageOptionsFragment = MessageMenuOptionsFragment.newInstance()
    private val retrievingOptionsFragment =
        RetrievingOptionsFragment.newInstance("Retrieving options")
    private val chatInstructionsFragment = ChatInstructionsFragment()
    private val vocabFragment = VocabFragment()
    private lateinit var storyBoardOneFragment: StoryBoardOneFragment
    private lateinit var storyBoardTwoFragment: StoryBoardTwoFragment
    private lateinit var easterEggFragment: EasterEggFragment

    var currentPath = ""

    private var profileImgView: ImageView? = null
    private var translationImgView: CircleImageView? = null
    private var messageTextView: TextView? = null

    private lateinit var botTitle: String

    private var auth = FirebaseAuth.getInstance()
    private lateinit var database: DatabaseReference

    var botMessages = arrayListOf<String>()
    var userMessages = arrayListOf<String>()
    private var mediaPlayer = MediaPlayer()

    private var isMicActive = false
    lateinit var preferences: Preferences

    private var TEXT_SIZE_MESSAGE: Float = 15f
    private var MESSAGE_BUBBLE_WIDTH = 600
    private var MESSAGE_PADDING = 20
    private var MESSAGE_VERTICAL_SPACING = 50
    private var PROFILE_IMAGE_SIZE = 50

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chatter)
        database = FirebaseDatabase.getInstance().reference
        preferences = getMyPreferences() ?: Preferences(this)
        setUpDimensions()
        setUpTopBar()
        setUpStoryBoardFragments()
        setUpNavButtons()
        loadFirstStoryBoardFragment()
    }

    private fun setUpDimensions() {
        TEXT_SIZE_MESSAGE = 1.0f * (this.resources.getInteger(R.integer.message_bubble_text_size))
        MESSAGE_BUBBLE_WIDTH = this.resources.getInteger(R.integer.message_bubble_width)
        MESSAGE_PADDING = this.resources.getInteger(R.integer.message_bubble_padding)
        MESSAGE_VERTICAL_SPACING = this.resources.getInteger(R.integer.message_vertical_spacing)
        PROFILE_IMAGE_SIZE = this.resources.getInteger(R.integer.bot_profile_size)
    }

    override fun setUpTopBar() {
        top_bar_title.text = "Chatter"
        top_bar_mic.setOnClickListener {
            if (!isMicActive) {
                isMicActive = true
                toggleIsChatterActivity(true)
                top_bar_mic.setImageResource(R.drawable.microphone_listening)
                (top_bar_mic.drawable as AnimationDrawable).start()
                startListening()
            } else {
                isMicActive = false
                top_bar_mic.setImageResource(R.drawable.microphone_listening)
            }
        }
    }

    override fun showFirstBotMessage() {
        currentPath = "$BOT_CONVERSATIONS/$botTitle/"
        val pathReference = database.child(currentPath.plus("botMessage"))
        disableNextButton()
        val messageListener = baseValueEventListener { dataSnapshot ->
            val botMessage = dataSnapshot.value.toString()
            handleNewMessageLogic(botMessage)
            loadOptionsMenu()
        }
        pathReference.addListenerForSingleValueEvent(messageListener)
    }

    override fun initializeMessagesContainer() {
        constraintSet.clone(messagesInnerLayout)
    }

    override fun addMessage(msg: String) {
        setUpMessageTextView(msg)
        setupProfileImgView()
        addConstraintToProfileImageView()
        addConstraintsForMessageTextView()
        //addConstraintToTranslationImageView()
        addGeneralConstraintsForProfileImageAndMessageText()
        setConstraintsToLayout()
    }

    fun refreshChatMessages() {
        finish()
        overridePendingTransition(0, 0);
        startActivity(intent)
        overridePendingTransition(0, 0);
    }

    fun getBotStories(botTitle: String): ArrayList<String> {
        return getMyPreferences()?.getBotStories(botTitle) ?: arrayListOf(
            "I don't know ".plus(
                botTitle
            )
        )
    }

    private fun retrieveSavedChatState() {
        auth.currentUser?.uid?.let {
            showFirstBotMessage()
            /*currentPath = preferences.getStoredPath(it, botTitle) ?: ""
            if (currentPath.isNotEmpty()) {
                var result = preferences.getStoredMessages(it, botTitle)
                botMessages = result.first
                userMessages = result.second
                restoreMessages()
            } else {
                showFirstBotMessage()
            }*/
        }
    }

    private fun restoreMessages() {
        var bm = 0
        var um = 0
        var isBotsTurn = true
        while (bm < botMessages.size && um < userMessages.size) {
            Log.d("Whose turn ", "" + isBotsTurn)
            if (isBotsTurn) {
                handleNewMessageLogic(botMessages[bm++])
            } else {
                handleNewMessageLogic(userMessages[um++])
            }
            isBotsTurn = !isBotsTurn
        }

        messagesInnerLayout.findViewById<TextView>(newMsgId).isFocusableInTouchMode
        messagesInnerLayout.findViewById<TextView>(newMsgId).requestFocus()
        if ((bm + um) % 2 == 0) {
            getBotResponse(currentPath)
        } else {
            loadOptionsMenu()
        }
    }

    private fun getBotResponse(path: String) {
        val pathReference = database.child(path.plus("/botMessage"))
        disableNextButton()
        val messageListener = baseValueEventListener { dataSnapshot ->
            val botMessage = dataSnapshot.value.toString()
            setTimerTask("showBotIsTyping", 700, {
                addSpaceText()
                showBotIsTypingView()
            })
            setTimerTask("loadOptionsMenu", 2000, {
                replaceBotIsTyping(botMessage)
                //addBotMessages(botMessage)
                loadOptionsMenu()
                //storeChatMessages()
            })
        }
        pathReference.addListenerForSingleValueEvent(messageListener)
    }


    fun addUserMessage(str: String) {
        userMessages.add(str)
    }

    private fun setUpStoryBoardFragments() {
        botTitle = intent?.getStringExtra(BOT_TITLE) ?: ""
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
        chat_instructions_text.setOnClickListener {
            //TODO
        }
        //loadChatInstructionsFragment()
        retrieveSavedChatState()
    }

    /*private fun loadChatInstructionsFragment() {
        supportFragmentManager
            .beginTransaction()
            .replace(chat_instructions_container.id, chatInstructionsFragment)
            .addToBackStack(chatInstructionsFragment.javaClass.name)
            .commit()
    }*/

    fun closeEasterEggFragment() {
        supportFragmentManager?.popBackStack(
            easterEggFragment.javaClass.name,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
    }

    private fun loadBotDescriptionFragment() {
        loadFragmentIntoRoot(BotDescriptionFragment())
    }

    private fun loadVocabFragment() {
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

    private fun addGeneralConstraintsForProfileImageAndMessageText() {
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

    private fun addConstraintsForMessageTextView() {
        messageTextView?.apply {
            constraintSet.constrainHeight(id, ViewGroup.LayoutParams.WRAP_CONTENT)
            constraintSet.constrainWidth(id, MESSAGE_BUBBLE_WIDTH)
        }
        addViewToLayout(messageTextView as TextView)
    }

    private fun setUpMessageBubbleClickListener() {
        val textView = messageTextView
        textView?.setOnClickListener {
            val messageText = textView.text.toString()
            val translation = preferences.getTranslation(messageText)
            if (translation.startsWith("EN - ")) {
                textView.text = translation.subSequence(4, translation.length)
            } else {
                val translationEs = preferences.getTranslation("EN -".plus(messageText))
                textView.text = translationEs
            }
        }
        textView?.setOnLongClickListener {
            val messageText = textView.text.toString()
            val translation = preferences.getTranslation(messageText)
            if (translation.startsWith("EN - ")) {
                //read messageText
                readSpanishTranslation(messageText)
            } else {
                //read translation
                readSpanishTranslation(translation)
            }
            true
        }
    }

    private fun playMedia(audio: String) {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop()
            mediaPlayer.release()
        }
        mediaPlayer = MediaPlayer()
        mediaPlayer.setDataSource(audio)
        mediaPlayer.prepareAsync()
        mediaPlayer.setOnPreparedListener {
            mediaPlayer.start()
        }
    }

    private fun getMessageTextBubbleId(): Int {
        return 10 * msgCount
    }

    private fun showTranslation(side: String, path: String, msgId: Int) {
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

    private fun addConstraintToTranslationImageView() {
        translationImgView?.apply {
            constraintSet.constrainHeight(id, PROFILE_IMAGE_SIZE)
            constraintSet.constrainWidth(id, PROFILE_IMAGE_SIZE)
            addViewToLayout(this)
        }
    }

    private fun setupProfileImgView() {
        profileImgView = ImageView(this)
        profileImgView?.apply {
            id = getIdProfileImageView()
            setImageDrawable(ContextCompat.getDrawable(context, R.drawable.business_profile))
        }
    }

    private fun setUpMessageTextView(msg: String, shouldFocus: Boolean = true) {
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
        view.visibility = View.INVISIBLE
        messagesInnerLayout.addView(view)
        view.setAlpha(0f)
        view.animate().alpha(1f).setDuration(500)
    }

    private fun setConstraintsToLayout() {
        constraintSet.applyTo(messagesInnerLayout)
    }

    fun showBotIsTypingView() {
        val setStr = "bot is typing"
        handleNewMessageLogic(setStr)
        val botIsTypingId = getMessageTextBubbleId()
        var botIsTypingTextView: TextView = findViewById<TextView>(botIsTypingId)
        showTypingAnimation(botIsTypingTextView, 300)
    }

    private fun showTypingAnimation(botIsTypingTextView: TextView, delay: Long) {
        var typingMessage = "bot is typing ".plus("...")
        val handler = Handler()
        var start = "bot is typing ".length
        var pos = 0
        val characterAdder: Runnable = object : Runnable {
            override fun run() {
                if (botIsTypingTextView.text.contains("bot is typing")) {
                    var setStr = typingMessage.subSequence(0, start + pos + 1)
                    val spannableString = SpannableString(setStr)
                    spannableString.setSpan(
                        ForegroundColorSpan(Color.BLACK),
                        setStr.length - 1,
                        setStr.length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    botIsTypingTextView.setText(spannableString)
                    pos++
                    if (pos == 3) {
                        pos = 0
                    }
                    handler.postDelayed(this, delay)
                }
            }
        }

        handler.removeCallbacks(characterAdder);
        handler.postDelayed(characterAdder, delay)
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

    private fun setUpSpaceView(spaceId: Int, shouldFocus: Boolean = true) {
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
                    "dsfdsfdsfds" +
                    "helloddsklkdkdkdkdkkdkdkdkkdkdkkddsfsdfdsfdsfdsfdsfdsfd" +
                    "helloddsklkdkdkdkdkkdkdkdkkdkdkkddsfsdfdsfdsfdsfdsfdsfd"
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

    /*override fun onError(error: Int) {
        top_bar_mic.setImageResource(R.drawable.microphone_listening)
        isMicActive = false
    }*/

    override fun onResults(results: Bundle?) {
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        top_bar_mic.setImageResource(R.drawable.microphone_listening)
        isMicActive = false
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

    fun updateTotalScore(pointsAdded: Long) {
        val userUid = auth.currentUser?.uid.toString()
        val pathRef = database.child(USERS).child(userUid).child("pointsRemaining")
        val pointsListener = baseValueEventListener { dataSnapshot ->
            val totalScore = dataSnapshot.value as Long
            if (totalScore > pointsAdded) {
                addScoreBoostToTotalScore(userUid, totalScore - pointsAdded)
            } else {
                updateLevel(userUid)
            }
        }
        pathRef.addListenerForSingleValueEvent(pointsListener)
    }

    private fun addScoreBoostToTotalScore(userUid: String, score: Long) {
        database.child(USERS).child(userUid).child("pointsRemaining").setValue(score)
    }

    private fun setNextLevel(userUid: String, nextLevel: String) {
        database.child(USERS).child(userUid).child("level").setValue(nextLevel)
        preferences.storeUserLevel(nextLevel)
    }

    private fun updateLevel(userUid: String) {
        val pathRef = database.child(USERS).child(userUid).child("level")
        val levelListener = baseValueEventListener { dataSnapshot ->
            val curLevel = dataSnapshot.value.toString()
            val nextLevel = getNextLevel(curLevel)
            setNextLevel(userUid, nextLevel)
            addScoreBoostToTotalScore(userUid, getRemainingPoints(nextLevel))
        }
        pathRef.addListenerForSingleValueEvent(levelListener)
    }

    private fun getNextLevel(curLevel: String): String {
        return preferences.getNextLevel(curLevel)
    }

    private fun getRemainingPoints(curLevel: String): Long {
        return preferences.getCurrentLevelPoints(curLevel)
    }

    companion object {
        const val BOT_TITLE = "BOT_TITLE"
        const val BOT_CONVERSATIONS = "BotConversations"
        const val USERS = "Users"
    }
}
