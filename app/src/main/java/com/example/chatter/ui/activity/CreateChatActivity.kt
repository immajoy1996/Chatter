package com.example.chatter.ui.activity

import android.content.Context
import android.graphics.Color
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.speech.SpeechRecognizer
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.bumptech.glide.Glide
import com.example.chatter.*
import com.example.chatter.extra.Preferences
import com.example.chatter.interfaces.ExpressionClickInterface
import com.example.chatter.interfaces.StoryBoardFinishedInterface
import com.example.chatter.ui.fragment.*
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_create_chat.*
import kotlinx.android.synthetic.main.bottom_nav_bar.*
import kotlinx.android.synthetic.main.top_bar.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CreateChatActivity : BaseChatActivity(),
    StoryBoardFinishedInterface,
    ExpressionClickInterface {
    private var constraintSet = ConstraintSet()

    private val createBotOptionsFragment = CreateBotOptionsFragment.newInstance()
    private val retrievingOptionsFragment =
        RetrievingOptionsFragment.newInstance("Retrieving options")
    private val chatInstructionsFragment =
        ChatInstructionsFragment()
    private val vocabFragment = VocabFragment()
    private lateinit var storyBoardOneFragment: StoryBoardOneFragment
    private lateinit var storyBoardTwoFragment: StoryBoardTwoFragment
    private lateinit var easterEggFragment: CreateEasterEggFragment
    private val navigationDrawerFragment =
        NavigationDrawerFragment()

    var currentPath = ""

    private var profileImgView: ImageView? = null
    private var translationImgView: CircleImageView? = null
    private var messageTextView: TextView? = null
    private var translationTextView: TextView? = null
    private var submitButtonImageView: ImageView? = null

    private lateinit var botTitle: String
    private lateinit var botImagePath: String

    private var auth = FirebaseAuth.getInstance()
    private lateinit var database: DatabaseReference

    var botMessages = arrayListOf<String>()
    var userMessages = arrayListOf<String>()
    private var mediaPlayer = MediaPlayer()

    private var isMicActive = false

    private var TEXT_SIZE_MESSAGE: Float = 15f
    private var MESSAGE_BUBBLE_WIDTH = 600
    private var MESSAGE_PADDING = 20
    private var MESSAGE_VERTICAL_SPACING = 50
    private var PROFILE_IMAGE_SIZE = 50

    var executorService: ExecutorService? = null
    private var isRefreshed = false
    private lateinit var audioManager: AudioManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_chat)
        database = FirebaseDatabase.getInstance().reference
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        executorService = Executors.newFixedThreadPool(5)
        setUpDimensions()
        setUpTopBar()
        setUpBotDetails()
        setUpNavButtons()
        showFirstBotMessage()
        //loadFirstStoryBoardFragment()
    }

    private fun setUpDimensions() {
        TEXT_SIZE_MESSAGE = 1.0f * (this.resources.getInteger(R.integer.message_bubble_text_size))
        MESSAGE_BUBBLE_WIDTH = this.resources.getInteger(R.integer.message_bubble_width)
        MESSAGE_PADDING = this.resources.getInteger(R.integer.message_bubble_padding)
        MESSAGE_VERTICAL_SPACING = this.resources.getInteger(R.integer.message_vertical_spacing)
        PROFILE_IMAGE_SIZE = this.resources.getInteger(R.integer.bot_profile_size)
    }

    override fun setUpTopBar() {
        top_bar_title.text = "Create Bot"
        top_bar_mic.visibility = View.GONE
        top_bar_easter_egg.visibility = View.VISIBLE
        home.setOnClickListener {
            refreshChatMessages()
        }
        top_bar_easter_egg.setOnClickListener {
            loadEasterEggFragment()
        }
    }

    private fun loadNavigationDrawer() {
        supportFragmentManager
            .beginTransaction()
            .replace(chatter_activity_root_container.id, navigationDrawerFragment)
            .addToBackStack(navigationDrawerFragment.javaClass.name)
            .commit()
    }

    override fun showFirstBotMessage() {
        currentPath = "$BOT_CONVERSATIONS/$botTitle/"
        val pathReference = database.child(currentPath.plus("botMessage"))
        disableNextButton()
        handleNewMessageLogic("")
        val messageListener = baseValueEventListener { dataSnapshot ->
            dataSnapshot.value?.let {
                replaceMessageText(it.toString())
            }
            loadOptionsMenu()
            if (isRefreshed) isRefreshed = false
        }
        pathReference.addListenerForSingleValueEvent(messageListener)
    }

    private fun replaceMessageText(newMsg: String) {
        findViewById<EditText>(getMessageTextBubbleId()).setText(newMsg)
    }

    override fun initializeMessagesContainer() {
        //initializeVariables()
        //messagesInnerLayout.removeAllViewsInLayout()
        constraintSet.clone(messagesInnerLayout)
    }

    private fun setUpTranslationForMessage(
        msgId: Int,
        originalMsg: String,
        targetLanguage: String
    ) {
        setUpTranslationTextView("getting translation ...")
        if (originalMsg.contains("bot is typing")) return
        executorService?.submit {
            val result = translate(originalMsg, targetLanguage)
            runOnUiThread {
                val translationId = msgId + 9
                val translationView = findViewById<TextView>(translationId)
                translationView.text = result
            }
        }
    }

    override fun addMessage(msg: String) {
        setUpMessageTextView(msg)
        //setUpTranslationForMessage(getMessageTextBubbleId(), msg, "ru")
        //setUpTranslationTextView("Sample translation")
        setupProfileImgView()
        setUpCreateBotSubmitButtonView()
        addConstraintToProfileImageView()
        addConstraintsForMessageTextView()
        addConstraintToSubmitButtonImageView()
        //addConstraintsForTranslationTextView()
        //addConstraintToTranslationImageView()
        //addGeneralConstraintsForProfileImageAndTranslationText()
        addGeneralConstraintsForProfileImageAndMessageText()
        addGeneralConstraintsForSubmitButtonImageAndMessageText()
        setConstraintsToLayout()
    }

    fun refreshChatMessages() {
        //isRefreshed = true
        /*finish()
        overridePendingTransition(0, 0)
        startActivity(intent)
        overridePendingTransition(0, 0)*/
        messagesInnerLayout.removeAllViews()
        initializeVariables()
        startChatting()
    }

    fun getBotStories(botTitle: String): ArrayList<String> {
        return getMyPreferences()?.getBotStories(botTitle) ?: arrayListOf(
            "I don't know ".plus(
                botTitle
            )
        )
    }

    private fun startChatting() {
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

    private fun setUpBotDetails() {
        botTitle = intent?.getStringExtra(BOT_TITLE) ?: ""
        botImagePath = intent?.getStringExtra(IMAGE_PATH) ?: ""
        storyBoardOneFragment =
            StoryBoardOneFragment.newInstance(
                botTitle
            )
        storyBoardTwoFragment =
            StoryBoardTwoFragment.newInstance(
                botTitle
            )
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
        //loadChatInstructionsFragment()
        startChatting()
    }

    /*private fun loadChatInstructionsFragment() {
        supportFragmentManager
            .beginTransaction()
            .replace(chat_instructions_container.id, chatInstructionsFragment)
            .addToBackStack(chatInstructionsFragment.javaClass.name)
            .commit()
    }*/

    fun closeEasterEggFragment() {
        supportFragmentManager.popBackStack(
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
            .replace(chatter_activity_root_container.id, vocabFragment)
            .addToBackStack(vocabFragment.javaClass.name)
            .commit()
    }

    fun loadFirstStoryBoardFragment() {
        storyBoardOneFragment.let {
            supportFragmentManager
                .beginTransaction()
                .replace(chatter_activity_root_container.id, it)
                .addToBackStack(it.javaClass.name)
                .commit()
        }
    }

    fun loadEasterEggFragment() {
        //playMedia(NOTIFICATION_SOUND_EFFECT_PATH)
        easterEggFragment =
            CreateEasterEggFragment()
        supportFragmentManager
            .beginTransaction()
            .replace(chatter_activity_root_container.id, easterEggFragment)
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
            .replace(chatter_activity_root_container.id, fragment)
            .addToBackStack(fragment.javaClass.name)
            .commit()
    }

    fun loadOptionsMenu() {
        disableNextButton()
        loadRetrievingOptionsFragment()
        setTimerTask("loadMessageOptionsFragment", 2000, {
            removeRetrievingOptionsFragment()
            loadFragment(createBotOptionsFragment)
            enableNextButton()
        })
    }

    fun removeOptionsMenu() {
        supportFragmentManager.popBackStack()
    }

    private fun addGeneralConstraintsForProfileImageAndTranslationText() {
        var position = -1
        if (isFirst) position = ConstraintSet.TOP
        else position = ConstraintSet.BOTTOM

        val textView = translationTextView as TextView
        val profileImg = profileImgView as ImageView
        //constraintSet.setVisibility(textView.id, View.GONE)

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

    private fun addGeneralConstraintsForSubmitButtonImageAndMessageText() {
        var position = -1
        if (isFirst) position = ConstraintSet.TOP
        else position = ConstraintSet.BOTTOM

        val textView = messageTextView as TextView
        val profileImg = submitButtonImageView as ImageView

        if (newSide == "left") {
            constraintSet.connect(
                profileImg.id,
                ConstraintSet.BOTTOM,
                textView.id,
                ConstraintSet.BOTTOM,
                0
            )

            constraintSet.connect(
                profileImg.id,
                ConstraintSet.START,
                textView.id,
                ConstraintSet.END,
                10
            )

            constraintSet.connect(
                profileImg.id,
                ConstraintSet.TOP,
                textView.id,
                ConstraintSet.TOP
            )

        } else if (newSide == "right") {
            constraintSet.connect(
                profileImg.id,
                ConstraintSet.BOTTOM,
                textView.id,
                ConstraintSet.BOTTOM,
                0
            )

            constraintSet.connect(
                profileImg.id,
                ConstraintSet.TOP,
                textView.id,
                ConstraintSet.TOP,
                0
            )

            constraintSet.connect(
                profileImg.id,
                ConstraintSet.END,
                textView.id,
                ConstraintSet.START,
                10
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

    private fun addConstraintsForTranslationTextView() {
        translationTextView?.apply {
            constraintSet.constrainHeight(id, ViewGroup.LayoutParams.WRAP_CONTENT)
            constraintSet.constrainWidth(id, MESSAGE_BUBBLE_WIDTH)
        }
        addTranslationViewToLayout(translationTextView as TextView)
    }

    private fun setUpMessageBubbleClickListener() {
        val textView = messageTextView
        textView?.setOnClickListener {
            //Toast.makeText(this,"clicked",Toast.LENGTH_SHORT).show()
            val otherView = findViewById<TextView>(textView.id + 9)
            val str1 = textView.text.toString()
            textView.text = otherView.text.toString()
            otherView.text = str1
        }
        textView?.setOnLongClickListener {
            val messageText = textView.text.toString()
            readMessageBubble(messageText)
            true
        }
    }

    private fun setUpTranslationBubbleClickListener() {
        val textView = translationTextView
        textView?.setOnClickListener {
            val otherView = findViewById<TextView>(textView.id - 9)
            val str1 = textView.text.toString()
            textView.text = otherView.text.toString()
            otherView.text = str1
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

    fun getMessageTextBubbleId(): Int {
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
            //setImageDrawable(ContextCompat.getDrawable(context, R.drawable.business_profile))
        }
        profileImgView?.let {
            Glide.with(this)
                .load(botImagePath)
                .into(it)
        }
    }

    private fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }

    private fun setUpCreateBotSubmitButtonView() {
        submitButtonImageView = ImageView(this)
        submitButtonImageView?.apply {
            id = getIdSubmitButton()
            setImageDrawable(
                ContextCompat.getDrawable(
                    context,
                    R.drawable.create_bot_submit
                )
            )
        }
        submitButtonImageView?.setOnClickListener {
            var updateDataTask: Task<Void>? = null
            val msgTextView = findViewById<TextView>(it.id - 9)
            msgTextView.hideKeyboard()
            if (msgTextView.text.toString().isNotEmpty()) {
                if (msgCount % 2 == 1) {
                    //left side
                    updateDataTask = database.child(currentPath).child("botMessage")
                        .setValue(msgTextView.text.toString())
                } else {
                    //right side
                    updateDataTask = database.child(currentPath).child("text")
                        .setValue(msgTextView.text.toString())
                }
                updateDataTask?.addOnSuccessListener {
                    Toast.makeText(this@CreateChatActivity, "Update successful", Toast.LENGTH_LONG)
                        .show()
                }?.addOnFailureListener {
                    Toast.makeText(this@CreateChatActivity, it.localizedMessage, Toast.LENGTH_LONG)
                        .show()
                }
            } else {
                val animWobble = AnimationUtils.loadAnimation(
                    this,
                    R.anim.wobble
                )
                msgTextView.startAnimation(animWobble)
            }
        }
    }

    private fun setUpMessageTextView(msg: String, shouldFocus: Boolean = true) {
        messageTextView = EditText(this)
        messageTextView?.apply {
            if (newSide == "right") {
                setBackgroundResource(R.drawable.option_bubble)
                setTextColor(Color.parseColor("#FFFFFF"))
                //setHintTextColor(Color.parseColor("#FFFFFF"))
            } else {
                setBackgroundResource(R.drawable.message_bubble_selector)
                setTextColor(Color.parseColor("#696969"))
                setHintTextColor(Color.parseColor("#D3D3D3"))
            }

            setPadding(MESSAGE_PADDING)
            setId(newMsgId)
            if (msg.isEmpty()) {
                hint = "Type message here"
            } else {
                text = msg
            }
            textSize = TEXT_SIZE_MESSAGE
            /*if (newSide == "right") {
                isFocusableInTouchMode = true
                requestFocus()
            }*/
        }
        //setUpMessageBubbleClickListener()
    }

    private fun setUpTranslationTextView(translation: String, shouldFocus: Boolean = true) {
        translationTextView = TextView(this)
        translationTextView?.apply {
            if (newSide == "right") {
                setBackgroundResource(R.drawable.option_bubble)
                setTextColor(Color.parseColor("#FFFFFF"))
            } else {
                setBackgroundResource(R.drawable.message_bubble_selector)
                setTextColor(Color.parseColor("#696969"))
            }
            setPadding(MESSAGE_PADDING)
            setId(newMsgId + 9)

            text = translation
            textSize = TEXT_SIZE_MESSAGE
            if (newSide == "right") {
                isFocusableInTouchMode = true
                requestFocus()
            }
        }
        translationTextView?.id?.let {
            constraintSet.setVisibility(it, View.GONE)
        }
        setUpTranslationBubbleClickListener()
    }

    private fun addConstraintToProfileImageView() {
        if (newSide == "left") {
            profileImgView?.apply {
                constraintSet.constrainHeight(id, PROFILE_IMAGE_SIZE)
                constraintSet.constrainWidth(id, PROFILE_IMAGE_SIZE)
                addViewToLayout(this)
            }
        }
    }

    private fun addConstraintToSubmitButtonImageView() {
        submitButtonImageView?.apply {
            constraintSet.constrainHeight(id, PROFILE_IMAGE_SIZE)
            constraintSet.constrainWidth(id, PROFILE_IMAGE_SIZE)
            addViewToLayout(this)
        }
    }

    private fun getIdProfileImageView(): Int {
        return 10 * msgCount + 1
    }

    private fun getIdSubmitButton(): Int {
        return 10 * msgCount + 9
    }

    private fun getIdForSpaceView(): Int {
        return 1000 * msgCount
    }

    private fun loadRetrievingOptionsFragment() {
        loadFragment(retrievingOptionsFragment)
    }

    private fun removeRetrievingOptionsFragment() {
        supportFragmentManager.popBackStack()
    }

    private fun addTranslationViewToLayout(view: View) {
        messagesInnerLayout.addView(view)
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
        val botIsTypingTextView: TextView = findViewById<TextView>(botIsTypingId)
        showTypingAnimation(botIsTypingTextView, 300)
    }

    private fun showTypingAnimation(botIsTypingTextView: TextView, delay: Long) {
        val typingMessage = "bot is typing ".plus("...")
        val handler = Handler()
        val start = "bot is typing ".length
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
        //setUpTranslationForMessage(getMessageTextBubbleId(), msg, "ru")
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

    override fun onError(error: Int) {
        top_bar_mic.setImageResource(R.drawable.microphone_listening)
        isMicActive = false
    }

    override fun onResults(results: Bundle?) {
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        top_bar_mic.setImageResource(R.drawable.microphone_listening)
        isMicActive = false
        if (createBotOptionsFragment.isVisible) {
            matches?.let {
                Log.d("Voice String", it[0])
                createBotOptionsFragment.selectOptionsWithVoice(it[0])
                toggleIsVocabFragmentFlag(false)
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

    override fun onExpressionClicked(expression: String) {
        if (audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) == 0) {
            Toast.makeText(this, "Turn up your volume", Toast.LENGTH_LONG).show()
        }
        readMessageBubble(expression)
    }

    override fun onPause() {
        super.onPause()
        executorService?.shutdown()
    }

    override fun onDestroy() {
        super.onDestroy()
        executorService?.shutdown()
    }

    companion object {
        const val NOTIFICATION_SOUND_EFFECT_PATH =
            "https://firebasestorage.googleapis.com/v0/b/chatter-f7ae2.appspot.com/o/vocabAudio%2Fnotification.mp3?alt=media&token=8b63396a-10a4-4f0a-a9c6-3a740196031c"
        const val BOT_TITLE = "BOT_TITLE"
        const val IMAGE_PATH = "IMAGE_PATH"
        const val BOT_CONVERSATIONS = "BotConversations"
        const val USERS = "Users"
    }


}