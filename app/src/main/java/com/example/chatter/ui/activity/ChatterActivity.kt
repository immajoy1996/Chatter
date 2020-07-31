package com.example.chatter.ui.activity

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.AnimationDrawable
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
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
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.chatter.R
import com.example.chatter.adapters.WordByWordAdapter
import com.example.chatter.extra.Preferences
import com.example.chatter.interfaces.ExpressionClickInterface
import com.example.chatter.interfaces.StoryBoardFinishedInterface
import com.example.chatter.ui.activity.DashboardActivity.Companion.TARGET_LANGUAGE
import com.example.chatter.ui.fragment.*
import com.example.chatter.ui.fragment.StoryBoardOneFragment.Companion.PERMISSION_REQUEST_CODE
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_chatter.*
import kotlinx.android.synthetic.main.bottom_nav_bar.*
import kotlinx.android.synthetic.main.top_bar.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ChatterActivity : BaseChatActivity(),
    StoryBoardFinishedInterface,
    ExpressionClickInterface {

    private var constraintSet = ConstraintSet()

    private val messageOptionsFragment =
        MessageMenuOptionsFragment.newInstance()
    private val retrievingOptionsFragment =
        RetrievingOptionsFragment.newInstance("Retrieving options")
    private val chatInstructionsFragment =
        ChatInstructionsFragment()
    private val vocabFragment = VocabFragment()
    private lateinit var storyBoardOneFragment: StoryBoardOneFragment
    private lateinit var storyBoardTwoFragment: StoryBoardTwoFragment
    private lateinit var easterEggFragment: EasterEggFragment
    private val navigationDrawerFragment =
        NavigationDrawerFragment()

    var currentPath = ""

    private var profileImgView: ImageView? = null
    private var bookImgView: ImageView? = null
    private var translationImgView: CircleImageView? = null
    private var messageTextView: TextView? = null
    private var translationTextView: TextView? = null

    private lateinit var botTitle: String
    private lateinit var botImagePath: String

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
    private var BOOK_IMAGE_SIZE = 70
    private var THREE_BUTTONS_VIEW_WIDTH = 400
    private var THREE_BUTTONS_VIEW_HEIGHT = 80
    private var TRANSLATE_BUTTON_DIMENSION = 60

    var executorService: ExecutorService? = null
    private var isRefreshed = false
    private var targetLanguage = ""
    private lateinit var audioManager: AudioManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chatter)
        database = FirebaseDatabase.getInstance().reference
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        preferences = getMyPreferences() ?: Preferences(
            this
        )
        executorService = Executors.newFixedThreadPool(5)
        targetLanguage = intent.getStringExtra(TARGET_LANGUAGE) ?: ""
        setUpDimensions()
        setUpTopBar()
        setUpStoryBoardFragments()
        setUpNavButtons()
        setUpWordByWordRecycler()
        loadBotStoryFragment()
    }

    override fun onUserInteraction() {
        dismissThreeImagesViewIfVisible()
    }

    private fun setUpDimensions() {
        TEXT_SIZE_MESSAGE = 1.0f * (this.resources.getInteger(R.integer.message_bubble_text_size))
        MESSAGE_BUBBLE_WIDTH = this.resources.getInteger(R.integer.message_bubble_width)
        MESSAGE_PADDING = this.resources.getInteger(R.integer.message_bubble_padding)
        MESSAGE_VERTICAL_SPACING = this.resources.getInteger(R.integer.message_vertical_spacing)
        PROFILE_IMAGE_SIZE = this.resources.getInteger(R.integer.bot_profile_size)
    }

    override fun setUpTopBar() {
        back.visibility = View.VISIBLE
        home.visibility = View.GONE
        back.setOnClickListener {
            finish()
        }
        top_bar_title.visibility = View.GONE
        top_bar_messaging_image_container.visibility = View.VISIBLE
        top_bar_title_desc.text = "Helper Monkey"
        top_bar_mic.visibility = View.VISIBLE
        top_bar_mic.setOnClickListener {
            if (!isMicActive) {
                if (audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) == 0) {
                    Toast.makeText(this, "Turn up your volume", Toast.LENGTH_LONG).show()
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !recordAudioPermissionGranted()) {
                    requestRecordAudioPermission()
                } else {
                    runMicLogic()
                }
            } else {
                isMicActive = false
                top_bar_mic.setImageResource(R.drawable.microphone_listening)
            }
        }
        home.setOnClickListener {
            refreshChatMessages()
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
        val messageListener = baseValueEventListener { dataSnapshot ->
            val botMessage = dataSnapshot.value.toString()
            handleNewMessageLogic(botMessage)
            loadOptionsMenu()
            if (isRefreshed) isRefreshed = false
        }
        pathReference.addListenerForSingleValueEvent(messageListener)
    }

    override fun initializeMessagesContainer() {
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
        setUpTranslationForMessage(getMessageTextBubbleId(), msg, targetLanguage)
        setupProfileImgView()
        addConstraintToProfileImageView()
        addConstraintsForMessageTextView()
        addConstraintsForTranslationTextView()
        addGeneralConstraintsForProfileImageAndMessageText()
        setUpThreeButtonsView()
        setUpWordByWordImageInThreeButtonsView()
        setUpAudioImageInThreeButtonsView()
        setUpTranslateImageInThreeButtonsView()
        setUpMessageBubbleClickListener()
        //setupBookImgView()
        //addConstraintToBookImageView()
        //addGeneralConstraintsForBookImageAndMessageText()
        setConstraintsToLayout()
    }

    fun refreshChatMessages() {
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
        showFirstBotMessage()
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
        //word_by_word_translate_button.visibility = View.VISIBLE
        button_back.visibility = View.GONE
        button_refresh.visibility = View.VISIBLE
        button_refresh.setOnDebouncedClickListener {
            refreshChatMessages()
        }
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

    fun loadBotStoryFragment() {
        val botStoryFragment = BotStoryFragment()
        botStoryFragment.let {
            supportFragmentManager
                .beginTransaction()
                .replace(chatter_activity_root_container.id, it)
                .addToBackStack(it.javaClass.name)
                .commit()
        }
    }

    private fun loadNewGemAquiredFragment(title: String, imageGem: Int) {
        playNotificationSound()
        easterEggFragment =
            EasterEggFragment.newInstance(title, imageGem)
        supportFragmentManager
            .beginTransaction()
            .replace(chatter_activity_root_container.id, easterEggFragment)
            .addToBackStack(easterEggFragment.javaClass.name)
            .commit()
    }

    fun loadEasterEggFragment(title: String, points: Long, imageSrc: String) {
        playNotificationSound()
        easterEggFragment =
            EasterEggFragment.newInstance(
                title,
                points,
                imageSrc
            )
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
            loadFragment(messageOptionsFragment)
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

    private fun addGeneralConstraintsForBookImageAndMessageText() {
        var position = -1
        if (isFirst) position = ConstraintSet.TOP
        else position = ConstraintSet.BOTTOM

        val textView = messageTextView as TextView
        val profileImg = bookImgView as ImageView

        if (newSide == "right") {
            constraintSet.connect(
                profileImg.id,
                ConstraintSet.BOTTOM,
                textView.id,
                ConstraintSet.BOTTOM,
                0
            )

            constraintSet.connect(
                profileImg.id,
                ConstraintSet.END,
                textView.id,
                ConstraintSet.START,
                10
            )

            constraintSet.connect(
                profileImg.id,
                ConstraintSet.TOP,
                textView.id,
                ConstraintSet.TOP,
                0
            )

        } else if (newSide == "left") {
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
                ConstraintSet.TOP,
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

    private fun toggleBookImage(msgId: Int) {
        val msgCount = msgId / 10
        val bookId = 1000 * msgCount + 1
        val bookView = findViewById<ImageView>(bookId)
        if (bookView.visibility == View.VISIBLE) {
            bookView?.id?.let {
                constraintSet.setVisibility(it, View.GONE)
                setConstraintsToLayout()
            }
        } else {
            bookView?.id?.let {
                constraintSet.setVisibility(it, View.VISIBLE)
                setConstraintsToLayout()
                bookView.visibility = View.VISIBLE
                bookView.setAlpha(0f)
                bookView.animate().alpha(1f).setDuration(500)
            }
        }
    }

    private fun setUpMessageBubbleClickListener() {
        val textView = messageTextView
        /*textView?.setOnClickListener {
            val otherView = findViewById<TextView>(textView.id + 9)
            val str1 = textView.text.toString()
            textView.text = otherView.text.toString()
            otherView.text = str1
            //toggleBookImage(textView.id)
        }*/
        textView?.setOnLongClickListener {
            /*if (audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) == 0) {
                Toast.makeText(this, "Turn up your volume", Toast.LENGTH_LONG).show()
            }
            val messageText = textView.text.toString()
            readMessageBubble(messageText)*/
            val msgCount = textView.id / 10
            val threeImagesView = findViewById<ConstraintLayout>(1000 * msgCount + 7)
            if (threeImagesView.visibility == View.GONE) {
                showThreeButtonsView(threeImagesView.id)
            }
            true
        }
    }

    private fun showThreeButtonsView(id: Int) {
        //id=1000*msgCount+7
        constraintSet.setVisibility(id, View.VISIBLE)
        constraintSet.setVisibility(id - 6, View.VISIBLE)
        constraintSet.setVisibility(id - 4, View.VISIBLE)
        constraintSet.setVisibility(id - 2, View.VISIBLE)
        setConstraintsToLayout()
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

    private fun playNotificationSound() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop()
            mediaPlayer.release()
        }
        mediaPlayer = MediaPlayer.create(this, R.raw.notification)
        mediaPlayer.start()
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
            //setImageDrawable(ContextCompat.getDrawable(context, R.drawable.business_profile))
        }
        profileImgView?.let {
            Glide.with(this)
                .load(botImagePath)
                .into(it)
        }
    }

    private fun setupBookImgView() {
        bookImgView = ImageView(this)
        bookImgView?.apply {
            id = getIdBookImageView()
            setImageDrawable(ContextCompat.getDrawable(context, R.drawable.translation))
        }
        setupBookImgClickListener()
    }

    private fun setupBookImgClickListener() {
        val bookView = bookImgView
        bookView?.setOnDebouncedClickListener {
            if (wordByWordContainer.visibility == View.VISIBLE) {
                wordByWordContainer.visibility = View.GONE
            } else {
                wordByWordContainer.visibility = View.VISIBLE
                val bookId = bookView.id
                val msgCount = (bookId - 1) / 1000
                val msgId = 10 * msgCount
                val msgTextView = findViewById<TextView>(msgId)
                val wordString = msgTextView.text.toString()
                val words = wordString.split(" ", ",", ".", "?", "!") as ArrayList<String>
                val translations = arrayListOf<String>()
                for (word in words) {
                    translations.add("mommia")
                }
                wordByWordRecycler.adapter = WordByWordAdapter(this, words, translations)
            }
        }
    }

    private fun getIdBookImageView(): Int {
        return 1000 * msgCount + 1
    }

    private fun getIdThreeButtonsView(): Int {
        return 1000 * msgCount + 7
    }

    private fun setUpTranslateImageInThreeButtonsView() {
        val translateImageView = ImageView(this)
        translateImageView.apply {
            setId(getIdTranslateImage())
            constraintSet.constrainHeight(id, TRANSLATE_BUTTON_DIMENSION)
            constraintSet.constrainWidth(id, TRANSLATE_BUTTON_DIMENSION)
            constraintSet.setVisibility(id, View.GONE)
            elevation = 10f
            setImageResource(R.drawable.translate_circular)
        }
        addViewToLayout(translateImageView)
        val constraintLayout = findViewById<ConstraintLayout>(getIdThreeButtonsView())
        val wordByWordImage = findViewById<ImageView>(getIdWordByWordImage())
        val aniWobble = AnimationUtils.loadAnimation(this@ChatterActivity, R.anim.wobble_forever)
        wordByWordImage.startAnimation(aniWobble)

        constraintSet.connect(
            translateImageView.id,
            ConstraintSet.BOTTOM,
            constraintLayout.id,
            ConstraintSet.BOTTOM
        )

        constraintSet.connect(
            translateImageView.id,
            ConstraintSet.TOP,
            constraintLayout.id,
            ConstraintSet.TOP
        )
        constraintSet.connect(
            translateImageView.id,
            ConstraintSet.START,
            constraintLayout.id,
            ConstraintSet.START
        )

        constraintSet.connect(
            translateImageView.id,
            ConstraintSet.END,
            wordByWordImage.id,
            ConstraintSet.START
        )
    }

    private fun setUpAudioImageInThreeButtonsView() {
        val translateImageView = ImageView(this)
        translateImageView.apply {
            setId(getIdAudioImage())
            constraintSet.constrainHeight(id, TRANSLATE_BUTTON_DIMENSION)
            constraintSet.constrainWidth(id, TRANSLATE_BUTTON_DIMENSION)
            constraintSet.setVisibility(id, View.GONE)
            elevation = 10f
            setImageResource(R.drawable.audio_circular)
        }
        addViewToLayout(translateImageView)
        val aniWobble = AnimationUtils.loadAnimation(this, R.anim.wobble_forever)
        translateImageView.startAnimation(aniWobble)
        val constraintLayout = findViewById<ConstraintLayout>(getIdThreeButtonsView())
        val wordByWordImage = findViewById<ImageView>(getIdWordByWordImage())

        constraintSet.connect(
            translateImageView.id,
            ConstraintSet.BOTTOM,
            constraintLayout.id,
            ConstraintSet.BOTTOM
        )

        constraintSet.connect(
            translateImageView.id,
            ConstraintSet.TOP,
            constraintLayout.id,
            ConstraintSet.TOP
        )
        constraintSet.connect(
            translateImageView.id,
            ConstraintSet.END,
            constraintLayout.id,
            ConstraintSet.END
        )

        constraintSet.connect(
            translateImageView.id,
            ConstraintSet.START,
            wordByWordImage.id,
            ConstraintSet.END
        )
    }

    private fun setUpWordByWordImageInThreeButtonsView() {
        val translateImageView = ImageView(this)
        translateImageView.apply {
            setId(getIdWordByWordImage())
            constraintSet.constrainHeight(id, TRANSLATE_BUTTON_DIMENSION)
            constraintSet.constrainWidth(id, TRANSLATE_BUTTON_DIMENSION)
            constraintSet.setVisibility(id, View.GONE)
            elevation = 10f
            setImageResource(R.drawable.word_by_word_circular)
        }
        addViewToLayout(translateImageView)
        val aniWobble = AnimationUtils.loadAnimation(this, R.anim.wobble_forever)
        translateImageView.startAnimation(aniWobble)
        val constraintLayout = findViewById<ConstraintLayout>(getIdThreeButtonsView())

        constraintSet.connect(
            translateImageView.id,
            ConstraintSet.BOTTOM,
            constraintLayout.id,
            ConstraintSet.BOTTOM
        )

        constraintSet.connect(
            translateImageView.id,
            ConstraintSet.TOP,
            constraintLayout.id,
            ConstraintSet.TOP
        )
        constraintSet.connect(
            translateImageView.id,
            ConstraintSet.START,
            constraintLayout.id,
            ConstraintSet.START
        )

        constraintSet.connect(
            translateImageView.id,
            ConstraintSet.END,
            constraintLayout.id,
            ConstraintSet.END
        )
    }

    private fun getIdTranslateImage(): Int {
        return 1000 * msgCount + 1
    }

    private fun getIdWordByWordImage(): Int {
        return 1000 * msgCount + 3
    }

    private fun getIdAudioImage(): Int {
        return 1000 * msgCount + 5
    }

    private fun setUpThreeButtonsView() {
        val constraintLayout = ConstraintLayout(this)
        constraintLayout.apply {
            setId(getIdThreeButtonsView())
            constraintSet.constrainHeight(id, THREE_BUTTONS_VIEW_HEIGHT)
            constraintSet.constrainWidth(id, THREE_BUTTONS_VIEW_WIDTH)
            constraintSet.setVisibility(id, View.GONE)
            setBackgroundResource(R.drawable.three_buttons_background)
            elevation = 10f
        }
        addViewToLayout(constraintLayout)
        //val constraintSetThreeButtons = ConstraintSet()
        //constraintSetThreeButtons.clone(constraintLayout)

        val messageView = findViewById<TextView>(getMessageTextBubbleId())

        constraintSet.connect(
            constraintLayout.id,
            ConstraintSet.BOTTOM,
            messageView.id,
            ConstraintSet.BOTTOM
        )

        constraintSet.connect(
            constraintLayout.id,
            ConstraintSet.TOP,
            messageView.id,
            ConstraintSet.TOP
        )
        constraintSet.connect(
            constraintLayout.id,
            ConstraintSet.START,
            messageView.id,
            ConstraintSet.START
        )
        constraintSet.connect(
            constraintLayout.id,
            ConstraintSet.END,
            messageView.id,
            ConstraintSet.END
        )
    }

    private fun dismissThreeImagesViewIfVisible() {
        for (x in 1 until (msgCount + 1)) {
            val id = 1000 * x + 7
            val threeImagesView = findViewById<ConstraintLayout>(id)
            if (threeImagesView.visibility == View.VISIBLE) {
                constraintSet.setVisibility(id, View.GONE)
                constraintSet.setVisibility(id - 2, View.GONE)
                constraintSet.setVisibility(id - 4, View.GONE)
                constraintSet.setVisibility(id - 6, View.GONE)
                setConstraintsToLayout()
            }
        }
    }

    private fun setUpMessageTextView(msg: String, shouldFocus: Boolean = true) {
        messageTextView = TextView(this)
        val typeface: Typeface? =
            ResourcesCompat.getFont(this.applicationContext, R.font.avenir_next)
        messageTextView?.apply {
            if (newSide == "right") {
                setBackgroundResource(R.drawable.option_bubble)
                setTextColor(Color.parseColor("#FFFFFF"))
            } else {
                setBackgroundResource(R.drawable.message_bubble_selector)
                setTextColor(Color.parseColor("#000000"))
            }
            setTypeface(typeface)
            setPadding(MESSAGE_PADDING)
            setId(newMsgId)

            text = msg
            textSize = TEXT_SIZE_MESSAGE
        }
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

    private fun addConstraintToBookImageView() {
        bookImgView?.apply {
            constraintSet.constrainHeight(id, BOOK_IMAGE_SIZE)
            constraintSet.constrainWidth(id, BOOK_IMAGE_SIZE)
            addBookViewToLayout(this)
        }
        /*bookImgView?.id?.let {
            constraintSet.setVisibility(it, View.INVISIBLE)
        }*/
    }

    private fun getIdProfileImageView(): Int {
        return 10 * msgCount + 1
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

    private fun addBookViewToLayout(view: View) {
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
        setUpTranslationForMessage(getMessageTextBubbleId(), msg, targetLanguage)
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
                setBackgroundColor(Color.parseColor("#ffffff"))
                setTextColor(Color.parseColor("#ffffff"))
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
        if (messageOptionsFragment.isVisible) {
            matches?.let {
                Log.d("Voice String", it[0])
                messageOptionsFragment.selectOptionsWithVoice(it[0])
            }
        }
    }

    fun updateTotalScore(pointsAdded: Long) {
        var oldScore: Int? = null
        var latestScore: Int? = null
        if (auth.currentUser != null) {
            auth.currentUser?.uid?.let {
                val userUid = it
                val pathRef = database.child(USERS).child(userUid).child("points")
                val pointsListener = baseValueEventListener { dataSnapshot ->
                    val currentScore = dataSnapshot.value as Long
                    oldScore = currentScore.toInt()
                    val newScore = currentScore + pointsAdded
                    latestScore = newScore.toInt()
                    database.child(USERS).child(userUid).child("points").setValue(newScore)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Points added", Toast.LENGTH_SHORT).show()
                            if (oldScore != null && latestScore != null && newGemAquired(
                                    oldScore as Int,
                                    latestScore as Int
                                )
                            ) {
                                getNewGem(newScore.toInt())?.let {
                                    loadNewGemAquiredFragment(
                                        "New gem acquired!",
                                        it
                                    )
                                }
                            }
                        }.addOnFailureListener {
                            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show()
                        }
                }
                pathRef.addListenerForSingleValueEvent(pointsListener)
            }
        } else {
            //Guest mode
            val currentScore = preferences.getCurrentScore()
            oldScore = currentScore
            val newScore = currentScore + pointsAdded.toInt()
            latestScore = newScore
            preferences.storeCurrentScore(newScore)
            if (oldScore != null && latestScore != null && newGemAquired(
                    oldScore as Int,
                    latestScore as Int
                )
            ) {
                getNewGem(newScore)?.let {
                    loadNewGemAquiredFragment(
                        "New gem acquired!",
                        it
                    )
                }
            }
            Toast.makeText(this, "Points added", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getNewGem(score: Int): Int? {
        if (score < preferences.gemPrices[0]) {
            return null
        }
        for (index in 1..preferences.gemImages.size - 1) {
            if (score < preferences.gemPrices[index]) {
                return preferences.gemImages[index - 1]
            }
        }
        return preferences.gemImages.last()
    }

    private fun newGemAquired(oldScore: Int, newScore: Int): Boolean {
        for (score in preferences.gemPrices) {
            if (oldScore < score && newScore >= score) return true
        }
        return false
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

    private fun requestRecordAudioPermission() {
        requestPermissions(
            this,
            arrayOf(Manifest.permission.RECORD_AUDIO),
            PERMISSION_REQUEST_CODE
        )
    }

    private fun recordAudioPermissionGranted(): Boolean {
        return if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            false
        } else true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults.first() == PackageManager.PERMISSION_GRANTED) {
                    //Permission Granted
                    runMicLogic()
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                            != PackageManager.PERMISSION_GRANTED
                        ) {
                            //Permission Denied
                        }
                    }
                }
            }
        }
    }

    private fun runMicLogic() {
        letBearSpeak("Please say an option at the tone")
        setTimerTask("voicePrompt", 3000) {
            isMicActive = true
            toggleIsChatterActivity(true)
            top_bar_mic.setImageResource(R.drawable.microphone_listening)
            (top_bar_mic.drawable as AnimationDrawable).start()
            startListening()
        }
    }

    private fun showMessageOKCancel(
        message: String,
        okListener: DialogInterface.OnClickListener
    ) {
        AlertDialog.Builder(this)
            .setMessage(message)
            .setPositiveButton("OK", okListener)
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }

    private fun setUpWordByWordRecycler() {
        wordByWordRecycler.layoutManager = LinearLayoutManager(this)
    }

    override fun onResume() {
        super.onResume()
        if (targetLanguage.isEmpty()) {
            targetLanguage = "hi"
        }
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
