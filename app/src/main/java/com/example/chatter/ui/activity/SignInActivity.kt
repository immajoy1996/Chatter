package com.example.chatter.ui.activity

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import com.example.chatter.R
import com.example.chatter.ui.fragment.EnterUsernameFragment.Companion.CHOOSE_PASSWORD
import com.example.chatter.ui.fragment.EnterUsernameFragment.Companion.ENTER_EMAIL
import com.example.chatter.ui.fragment.EnterUsernameFragment.Companion.ENTER_PASSWORD
import com.example.chatter.ui.fragment.EnterUsernameFragment.Companion.ENTER_SCHOOL_NAME
import com.example.chatter.ui.fragment.EnterUsernameFragment.Companion.ENTER_USERNAME
import com.example.chatter.ui.fragment.EnterUsernameFragment.Companion.REENTER_PASSWORD
import com.example.chatter.ui.fragment.NavigationDrawerFragment.Companion.USERS
import com.example.chatter.ui.fragment.SignInOptionsFragment.Companion.SIGN_IN
import com.example.chatter.ui.fragment.SignUpOptionsFragment.Companion.SIGN_UP_INDIVIDUAL
import com.example.chatter.ui.fragment.SignUpOptionsFragment.Companion.SIGN_UP_STUDENT
import com.example.chatter.data.User
import com.example.chatter.ui.fragment.*
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_chatter.*
import kotlinx.android.synthetic.main.activity_chatter.messagesInnerLayout
import kotlinx.android.synthetic.main.activity_chatter.optionsPopupContainer
import kotlinx.android.synthetic.main.activity_sign_in.*
import kotlinx.android.synthetic.main.top_bar.*
import pl.droidsonroids.gif.GifImageView
import java.util.*
import javax.mail.*
import javax.mail.Message
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class SignInActivity : BaseChatActivity() {
    private var scenario: Int? = null
    private var menuOptionsFragment =
        SignInOptionsFragment()
    private var signUpOptionsFragment =
        SignUpOptionsFragment()
    private var signInErrorFragment =
        SignInErrorFragment()
    private var usernameFragment = EnterUsernameFragment.newInstance(ENTER_USERNAME)
    private var passwordFragment = EnterUsernameFragment.newInstance(ENTER_PASSWORD)
    private var emailFragment = EnterUsernameFragment.newInstance(ENTER_EMAIL)
    private var choosePasswordFragment = EnterUsernameFragment.newInstance(CHOOSE_PASSWORD)
    private var reenterPasswordFragment = EnterUsernameFragment.newInstance(REENTER_PASSWORD)
    private var enterSchoolNameFragment = EnterUsernameFragment.newInstance(ENTER_SCHOOL_NAME)
    private var enterPinFragment =
        EnterPinFragment()
    private var retrievingOptionsFragment =
        RetrievingOptionsFragment.newInstance("Retrieving options")

    private var loadingAnimatedFragment = LoadingAnimatedFragment.newInstance("Loading ...")

    private lateinit var timerTask: TimerTask

    var schoolName: String? = null
    var username: String? = null
    var password: String? = null
    var reenterPassword: String? = null

    private var profileImgView: ImageView? = null
    private var translationImgView: CircleImageView? = null
    private var messageTextView: TextView? = null

    private var constraintSet = ConstraintSet()

    lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference

    val signInUserMessagesFragments =
        arrayListOf<Int>(
            FRAGMENT_ENTER_USERNAME,
            FRAGMENT_ENTER_PASSWORD
        )
    val signInbotMessages = arrayListOf<String>(
        "Ok. What's your username?",
        "Ok. What's your password?",
        "Ok, you're all set! Logging in."
    )

    val signUpIndividualUserMessagesFragments =
        arrayListOf<Int>(
            FRAGMENT_ENTER_EMAIL,
            FRAGMENT_CHOOSE_PASSWORD,
            FRAGMENT_REENTER_PASSWORD,
            FRAGMENT_ENTER_PIN
        )
    private val signUpIndividualBotMessages = arrayListOf<String>(
        "Ok. Enter your email.",
        "Ok. Choose your password?",
        "Ok, please reenter your password for confirmation."
        //"Ok, you're all set! We sent a 4 digit pin to your email. Please enter it below."
    )

    private val signUpThroughMySchoolUserMessages = arrayListOf<Int>(
        FRAGMENT_ENTER_YOUR_SCHOOL_NAME,
        FRAGMENT_ENTER_EMAIL,
        FRAGMENT_CHOOSE_PASSWORD,
        FRAGMENT_REENTER_PASSWORD
        //FRAGMENT_ENTER_PIN
    )

    val signUpThroughMySchoolBotMessages = arrayListOf<String>(
        "Ok. Enter your school name.",
        "Ok. Enter your email",
        "Ok. Choose your password?",
        "Ok, please reenter your password for confirmation.",
        "Ok, you're all set! We sent a 4 digit pin to your email. Please enter it below."
    )

    var userMessagesFragmentArray =
        arrayListOf<Int>()
    var botMessagesArray = arrayListOf<String>()

    var userPosition = 0
    var botPosition = 0
    var pin: String? = null

    private lateinit var appExecutors: AppExecutors
    private var TEXT_SIZE_MESSAGE: Float = 15f
    private var MESSAGE_BUBBLE_WIDTH = 600
    private var MESSAGE_PADDING = 20
    private var MESSAGE_VERTICAL_SPACING = 50
    private var PROFILE_IMAGE_SIZE = 50
    private var FIRST_VERTICAL_SPACING = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)
        setUpDimensions()
        appExecutors = AppExecutors()
        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference
        setUpTopBar()
        initializeMessagesContainer()
        showFirstBotMessage()
    }

    private fun setUpDimensions() {
        TEXT_SIZE_MESSAGE = 1.0f * (this.resources.getInteger(R.integer.message_bubble_text_size))
        MESSAGE_BUBBLE_WIDTH = this.resources.getInteger(R.integer.message_bubble_width)
        MESSAGE_PADDING = this.resources.getInteger(R.integer.message_bubble_padding)
        MESSAGE_VERTICAL_SPACING = this.resources.getInteger(R.integer.message_vertical_spacing)
        FIRST_VERTICAL_SPACING = 2 * MESSAGE_VERTICAL_SPACING
        PROFILE_IMAGE_SIZE = this.resources.getInteger(R.integer.bot_profile_size)
    }

    override fun setUpTopBar() {
        top_bar_title.visibility = View.GONE
        top_bar_messaging_image_container.visibility = View.VISIBLE
        top_bar_title_desc.text = "Samson"
        top_bar_mic.visibility = View.INVISIBLE
        home.visibility = View.GONE
        home_refresh.visibility = View.VISIBLE
        home_refresh.setOnClickListener {
            refreshSignInFlow()
        }
    }

    override fun showFirstBotMessage() {
        handleNewMessageLogic("Hello, what would you like to do?")
        addSpaceText()
        loadSignInOptions()
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

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(R.anim.slide_in, R.anim.slide_out)
            .replace(optionsPopupContainer.id, fragment)
            .addToBackStack(fragment.javaClass.name)
            .commit()
    }

    private fun loadLoadingFragment() {
        loadAnimatedLoadingFragment(loadingAnimatedFragment)
    }

    private fun loadAnimatedLoadingFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(R.anim.slide_in, R.anim.slide_out)
            .replace(root_container.id, fragment)
            .addToBackStack(fragment.javaClass.name)
            .commit()
    }

    fun setScenario(curScenario: Int) {
        scenario = curScenario
    }

    fun getScenario(): Int {
        return scenario ?: -1
    }

    private fun replaceBotIsTyping(msg: String) {
        val textView = messagesInnerLayout.getViewById(getMessageTextBubbleId()) as TextView
        textView.text = msg
    }

    private fun getMessageTextBubbleId(): Int {
        return 10 * msgCount
    }

    private fun showBotIsTypingView() {
        val setStr = "bot is typing"
        handleNewMessageLogic(setStr)
        val botIsTypingId = getMessageTextBubbleId()
        val botIsTypingTextView: TextView = findViewById<TextView>(botIsTypingId)
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

    fun runMessageFlow(msg: String, shouldCheckSchoolName: Boolean? = false) {
        handleNewMessageLogic(msg)
        if (shouldCheckSchoolName == true) {
            checkIfSchoolOnRecordAndContinueMessaging()
        } else {
            getBotResponse()
        }
    }

    fun runMessageFlowSignUp(msg: String) {
        handleNewMessageLogic(msg)
        setScenario(SIGN_UP_INDIVIDUAL)
        setUserAndBotResponseArrays()
        //getBotResponseForSignUp()
        getBotResponse()
    }

    fun removeOptionsMenu() {
        supportFragmentManager.popBackStack()
    }

    fun setUserAndBotResponseArrays() {
        botPosition = 0
        userPosition = 0
        botMessagesArray.clear()
        userMessagesFragmentArray.clear()
        when (scenario) {
            SIGN_IN -> {
                botMessagesArray = signInbotMessages
                userMessagesFragmentArray = signInUserMessagesFragments
            }
            SIGN_UP_INDIVIDUAL -> {
                botMessagesArray = signUpIndividualBotMessages
                userMessagesFragmentArray = signUpIndividualUserMessagesFragments
            }
            SIGN_UP_STUDENT -> {
                botMessagesArray = signUpThroughMySchoolBotMessages
                userMessagesFragmentArray = signUpThroughMySchoolUserMessages
            }
        }
    }

    private fun getBotResponseForSignUp() {
        removeOptionsMenu()
        setTimerTask("showBotIsTyping", 700, {
            addSpaceText()
            showBotIsTypingView()
        })

        setTimerTask("getCurrentOptionsMenu", 2000, {
            replaceBotIsTyping("How would you like to sign up?")
            loadSignUpOptionsFragment()
        })
    }

    private fun loadSignUpOptionsFragment() {
        loadOptionsFragment(signUpOptionsFragment)
    }

    fun getBotResponse() {
        removeOptionsMenu()
        setTimerTask("showBotIsTyping", 700, {
            addSpaceText()
            showBotIsTypingView()
        })

        setTimerTask("getCurrentOptionsMenu", 2000, {
            replaceBotIsTyping(botMessagesArray[botPosition++])
            getCurrentOptionsMenu()
        })
    }

    private fun loadRetrievingOptionsFragment() {
        loadFragment(retrievingOptionsFragment)
    }

    private fun removeRetrievingOptionsFragment() {
        supportFragmentManager.popBackStack()
        retrievingOptionsFragment = RetrievingOptionsFragment.newInstance("Retrieving Options")
    }

    fun loadOptionsFragment(fragment: Fragment) {
        loadRetrievingOptionsFragment()
        setTimerTask("loadOptionsFragment", 2000, {
            removeRetrievingOptionsFragment()
            loadFragment(fragment)
        })
    }

    private fun getCurrentOptionsMenu() {
        if (userPosition > userMessagesFragmentArray.size) return
        else if (userPosition == userMessagesFragmentArray.size) {
            if (credentialsAreValid()) signInExistingUser()
            else {
                loadSignInErrorFragment("Username and password cannot be empty")
            }
        } else {
            when (userMessagesFragmentArray[userPosition]) {
                FRAGMENT_ENTER_USERNAME -> {
                    loadUsernameFragment()
                }
                FRAGMENT_ENTER_PASSWORD -> {
                    loadPasswordFragment()
                }
                FRAGMENT_ENTER_EMAIL -> {
                    loadEmailFragment()
                }
                FRAGMENT_CHOOSE_PASSWORD -> {
                    loadChoosePasswordFragment()
                }
                FRAGMENT_REENTER_PASSWORD -> {
                    loadReenterPasswordFragment()
                }
                FRAGMENT_ENTER_PIN -> {
                    if (username != null && password != null) {
                        sendEmail(username as String)
                        loadEnterPinFragment()
                    }
                }
                FRAGMENT_ENTER_YOUR_SCHOOL_NAME -> {
                    loadEnterSchoolNameFragment()
                }
            }
            userPosition++
        }
    }

    private fun createRandomPin(): String {
        var result = ""
        for (i in 1..4) {
            result = result.plus((Math.random() * 10).toInt().toString())
        }
        return result
    }

    fun sendEmail(email: String) {
        appExecutors.diskIO().execute {
            val props = System.getProperties()
            props.put("mail.smtp.host", "smtp.gmail.com")
            props.put("mail.smtp.socketFactory.port", "465")
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory")
            props.put("mail.smtp.auth", "true")
            props.put("mail.smtp.port", "465")

            val session = Session.getInstance(props,
                object : javax.mail.Authenticator() {
                    //Authenticating the password
                    override fun getPasswordAuthentication(): PasswordAuthentication {
                        return PasswordAuthentication("ijoy4136@gmail.com", "ch#Ristos33")
                    }
                })

            try {
                //Creating MimeMessage object
                val mm = MimeMessage(session)
                //Setting sender address
                mm.setFrom(InternetAddress("ijoy4136@gmail.com"))
                //Adding receiver
                mm.addRecipient(
                    Message.RecipientType.TO,
                    InternetAddress(email)
                )
                //Adding subject
                mm.subject = "Your Chatter Pin"
                //Adding message
                pin = createRandomPin()
                mm.setText("Your pin is ".plus(pin))

                //Sending email
                Transport.send(mm)

                appExecutors.mainThread().execute {
                    //Something that should be executed on main thread.
                }

            } catch (e: MessagingException) {
                e.printStackTrace()
            }
        }
    }

    private fun credentialsAreValid(): Boolean {
        return (username != null && password != null)
    }

    private fun navigateToLanguageSelectionActivity(userId: String) {
        setTimerTask("signUp", 2000, {
            removeRetrievingOptionsFragment()
            toggleRestartFlag(false)
            val intent = Intent(this@SignInActivity, LanguageSelectionActivity::class.java)
            startActivity(intent)
        })
    }

    private fun checkIfSchoolOnRecordAndContinueMessaging() {
        retrievingOptionsFragment = RetrievingOptionsFragment.newInstance("Verifying School")
        loadRetrievingOptionsFragment()
        val schoolRef = databaseReference.child("Schools").child(schoolName as String)
        val schoolListener = baseValueEventListener { dataSnapshot ->
            setTimerTask("schoolTimer", 2000, {
                runSchoolNameCheckLogic(dataSnapshot)
            })
        }
        schoolRef.addListenerForSingleValueEvent(schoolListener)
    }

    private fun runSchoolNameCheckLogic(dataSnapshot: DataSnapshot) {
        if (dataSnapshot.hasChild("schoolName")) {
            removeRetrievingOptionsFragment()
            Toast.makeText(this, "Verified!", Toast.LENGTH_SHORT).show()
            getBotResponse()
        } else {
            removeRetrievingOptionsFragment()
            loadSignInErrorFragment("We don't have your school on record")
        }
    }

    fun removeWhitespaceCharactersFromUsernameAndPassword() {
        username?.trim()
        password?.trim()
        reenterPassword?.trim()
    }

    fun signUpNewUser() {
        retrievingOptionsFragment = RetrievingOptionsFragment.newInstance("Signing Up")
        loadRetrievingOptionsFragment()
        removeWhitespaceCharactersFromUsernameAndPassword()
        auth.createUserWithEmailAndPassword(username as String, password as String)
            .addOnSuccessListener {
                createUserDatabaseEntry(
                    auth.uid as String,
                    username as String,
                    password as String,
                    0,
                    null,
                    BEAR_PROFILE_PATH
                )
            }.addOnFailureListener {
                removeRetrievingOptionsFragment()
                loadSignInErrorFragment(it.localizedMessage)
            }
    }

    private fun createUserDatabaseEntry(
        userId: String,
        email: String,
        password: String,
        points: Int,
        nativeLanguage: String?,
        profileImage: String
    ) {
        databaseReference.child(USERS.plus(userId))
            .setValue(
                User(
                    email,
                    password,
                    points,
                    nativeLanguage,
                    profileImage
                )
            )
            .addOnSuccessListener {
                navigateToLanguageSelectionActivity(userId)
            }.addOnFailureListener {
                Toast.makeText(this, it.localizedMessage, Toast.LENGTH_LONG).show()
            }
    }

    private fun signInExistingUser() {
        //retrievingOptionsFragment = RetrievingOptionsFragment.newInstance("Logging In")
        //loadRetrievingOptionsFragment()
        setTimerTask("signingIn", 1000, {
            loadAnimatedLoadingFragment(loadingAnimatedFragment)
            setTimerTask("signingInInnerTask", 1000, {
                auth.signInWithEmailAndPassword(username as String, password as String)
                    .addOnSuccessListener {
                        removeLoadingAnimatedFragment()
                        signIn(it)
                    }.addOnFailureListener {
                        removeLoadingAnimatedFragment()
                        loadSignInErrorFragment(it.localizedMessage)
                    }
            })
        })
    }

    fun loginAutomatically() {
        loadLoadingFragment()
        setTimerTask("signingYouInAgain", 2000, {
            removeLoadingAnimatedFragment()
            val intent = Intent(this, HomeNavigationActivity::class.java)
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        })
    }

    private fun signIn(authResult: AuthResult) {
        authResult.user?.uid?.let {
            val uid = it
            val langListener = baseValueEventListener { dataSnapshot ->
                val langName = dataSnapshot.child("languageName").value
                val flagString = dataSnapshot.child("flagImg").value
                var intent: Intent? = null
                if (langName != null && flagString != null) {
                    //Toast.makeText(this, "success", Toast.LENGTH_SHORT).show()
                    intent = Intent(this@SignInActivity, HomeNavigationActivity::class.java)
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                } else {
                    //Toast.makeText(this, "failure", Toast.LENGTH_SHORT).show()
                    intent =
                        Intent(this@SignInActivity, LanguageSelectionActivity::class.java)
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }
                startActivity(intent)
            }
            databaseReference.child("Users/${uid}/nativeLanguage")
                .addListenerForSingleValueEvent(langListener)
        }
    }

    fun removeLoadingAnimatedFragment() {
        supportFragmentManager.popBackStack()
        loadingAnimatedFragment = LoadingAnimatedFragment.newInstance("Loading ...")
    }

    fun signInAsGuest() {
        //retrievingOptionsFragment = RetrievingOptionsFragment.newInstance("Guest Mode")
        //loadRetrievingOptionsFragment()
        //loadingAnimatedFragment = LoadingAnimatedFragment.newInstance("Loading Guest Mode ...")
        //loadAnimatedLoadingFragment(loadingAnimatedFragment)
        if (auth.currentUser != null) {
            auth.signOut()
        }
        setTimerTask("signInAsGuest", 0, {
            //removeLoadingAnimatedFragment()
            toggleRestartFlag(false)
            var intent: Intent? = null
            if (preferences.getCurrentTargetLanguage()
                    .isNotEmpty() && preferences.getCurrentTargetLanguageFlag().isNotEmpty()
            ) {
                //intent = Intent(this@SignInActivity, HomeNavigationActivity::class.java)
                //intent = Intent(this@SignInActivity, BotStoryActivity::class.java)
                //intent.putExtra("botStoryTitle",preferences.getCurrentBotStory())
                intent = Intent(this@SignInActivity, HomeNavActivityLatest::class.java)
            } else {
                intent = Intent(this@SignInActivity, LanguageSelectionActivity::class.java)
            }
            //intent.putExtra("GUEST_MODE", true)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        })
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

    private fun loadEmailFragment() {
        loadOptionsFragment(emailFragment)
    }

    private fun loadChoosePasswordFragment() {
        loadOptionsFragment(choosePasswordFragment)
    }

    private fun loadReenterPasswordFragment() {
        loadOptionsFragment(reenterPasswordFragment)
    }

    private fun loadEnterPinFragment() {
        loadOptionsFragment(enterPinFragment)
    }

    private fun loadEnterSchoolNameFragment() {
        loadOptionsFragment(enterSchoolNameFragment)
    }

    private fun loadSignInErrorFragment(errorMessage: String) {
        signInErrorFragment = SignInErrorFragment.newInstance(errorMessage)
        supportFragmentManager
            .beginTransaction()
            .replace(optionsPopupContainer.id, signInErrorFragment)
            .addToBackStack(signInErrorFragment.javaClass.name)
            .commit()
    }

    fun setUpMessageTextView(msg: String) {
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
            elevation = 5f
            setTypeface(typeface)
            setPadding(MESSAGE_PADDING)
            setId(newMsgId)

            text = msg
            textSize = TEXT_SIZE_MESSAGE
            if (newSide == "right") {
                isFocusableInTouchMode = true
                requestFocus()
            }
        }
    }

    fun setupProfileImgView() {
        profileImgView = GifImageView(this)
        profileImgView?.apply {
            id = getIdProfileImageView()
            setImageResource(R.drawable.robot_walking_pin)
            /*setImageDrawable(
                ContextCompat.getDrawable(
                    context,
                    R.drawable.business_profile
                )
            )*/
        }
    }

    fun getIdProfileImageView(): Int {
        return 10 * msgCount + 1
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

    fun addConstraintsForMessageTextView() {
        messageTextView?.apply {
            constraintSet.constrainHeight(id, ViewGroup.LayoutParams.WRAP_CONTENT)
            constraintSet.constrainWidth(id, MESSAGE_BUBBLE_WIDTH)
        }
        addViewToLayout(messageTextView as TextView)
    }

    fun addConstraintToTranslationImageView() {
        translationImgView?.apply {
            constraintSet.constrainHeight(id, PROFILE_IMAGE_SIZE)
            constraintSet.constrainWidth(id, PROFILE_IMAGE_SIZE)
            addViewToLayout(this)
        }
    }

    private fun addViewToLayout(view: View) {
        messagesInnerLayout.addView(view)
        view.setAlpha(0f)
        view.animate().alpha(1f).setDuration(500)
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

    private fun setConstraintsToLayout() {
        constraintSet.applyTo(messagesInnerLayout)
    }

    private fun addSpaceText() {
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
        val typeface: Typeface? =
            ResourcesCompat.getFont(this.applicationContext, R.font.avenir_next)
        messageTextView?.apply {
            if (newSide == "left") {
                setBackgroundColor(Color.parseColor("#ffffff"))
                setTextColor(Color.parseColor("#ffffff"))
            }
            setPadding(MESSAGE_PADDING)
            setId(spaceId)
            text = LOTS_OF_TEXT
            setTypeface(typeface)
            textSize = TEXT_SIZE_MESSAGE
            isFocusableInTouchMode = true
            requestFocus()
        }
    }

    private fun addGeneralConstraintsForSpaceView() {
        val textView = messageTextView as TextView
        if (msgCount == 1) {
            constraintSet.connect(
                textView.id,
                ConstraintSet.TOP,
                prevMsgId,
                ConstraintSet.BOTTOM,
                FIRST_VERTICAL_SPACING
            )
        } else if (newSide == "left") {
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

    fun refreshSignInFlow() {
        finish()
        overridePendingTransition(0, 0);
        startActivity(intent)
        overridePendingTransition(0, 0);
    }

    companion object {
        const val FRAGMENT_ENTER_USERNAME = 10
        const val FRAGMENT_ENTER_PASSWORD = 20
        const val FRAGMENT_ENTER_EMAIL = 30
        const val FRAGMENT_CHOOSE_PASSWORD = 40
        const val FRAGMENT_REENTER_PASSWORD = 50
        const val FRAGMENT_ENTER_PIN = 60
        const val FRAGMENT_ENTER_YOUR_SCHOOL_NAME = 70
        const val BEAR_PROFILE_PATH =
            "https://firebasestorage.googleapis.com/v0/b/chatter-f7ae2.appspot.com/o/botImages%2Fbusiness_profile.png?alt=media&token=04b44fa6-56ec-4614-b6fb-8a99c84fa96a"

        const val LOTS_OF_TEXT = "helloddsklkdkdkdkdkkdkdkdkkdkdkkddsfsdfdsfdsfdsfdsfdsfd" +
                "dfsdfdsfdsfdsfdsfdsfdsdsfdsfds" +
                "dsfdsfdsfdshelloddsklkdkdkdkdkkdkdkdkkdkdkkddsfsdfdsfdsfdsfdsfdsfd"
    }
}
