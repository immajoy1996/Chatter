package com.example.chatter

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import com.example.chatter.EnterUsernameFragment.Companion.CHOOSE_PASSWORD
import com.example.chatter.EnterUsernameFragment.Companion.ENTER_EMAIL
import com.example.chatter.EnterUsernameFragment.Companion.ENTER_PASSWORD
import com.example.chatter.EnterUsernameFragment.Companion.ENTER_USERNAME
import com.example.chatter.EnterUsernameFragment.Companion.REENTER_PASSWORD
import com.example.chatter.NavigationDrawerFragment.Companion.USERS
import com.example.chatter.SignInOptionsFragment.Companion.SIGN_IN
import com.example.chatter.SignInOptionsFragment.Companion.SIGN_UP
import com.example.chatter.SignInOptionsFragment.Companion.SIGN_UP_INDIVIDUAL
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_chatter.*
import kotlinx.android.synthetic.main.top_bar.*
import java.util.*
import kotlin.concurrent.schedule

class SignInActivity : BaseChatActivity() {
    private var scenario: Int? = null
    private var menuOptionsFragment = SignInOptionsFragment()
    private var signUpOptionsFragment = SignUpOptionsFragment()
    private var signInErrorFragment = SignInErrorFragment()
    private var usernameFragment = EnterUsernameFragment.newInstance(ENTER_USERNAME)
    private var passwordFragment = EnterUsernameFragment.newInstance(ENTER_PASSWORD)
    private var emailFragment = EnterUsernameFragment.newInstance(ENTER_EMAIL)
    private var choosePasswordFragment = EnterUsernameFragment.newInstance(CHOOSE_PASSWORD)
    private var reenterPasswordFragment = EnterUsernameFragment.newInstance(REENTER_PASSWORD)
    private var enterPinFragment = EnterPinFragment()
    private var retrievingOptionsFragment =
        RetrievingOptionsFragment.newInstance("Retrieving options")
    private lateinit var signInPresenter: SignInPresenter

    private lateinit var timerTask: TimerTask

    var username: String? = null
    var password: String? = null
    var reenterPassword: String? = null

    private var profileImgView: ImageView? = null
    private var translationImgView: CircleImageView? = null
    private var messageTextView: TextView? = null

    private var constraintSet = ConstraintSet()

    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference

    val signInUserMessagesFragments =
        arrayListOf<Int>(FRAGMENT_ENTER_USERNAME, FRAGMENT_ENTER_PASSWORD)
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
    val signUpIndividualBotMessages = arrayListOf<String>(
        "Ok. Enter your email.",
        "Ok. Choose your password?",
        "Ok, please reenter your password for confirmation.",
        "Ok, you're all set! We sent a 4 digit pin to your email. Please enter it below."
    )

    var userMessagesFragmentArray =
        arrayListOf<Int>()
    var botMessagesArray = arrayListOf<String>()

    var userPosition = 0
    var botPosition = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)
        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference
        setUpTopBar()
        initializeMessagesContainer()
        showFirstBotMessage()
    }

    override fun setUpTopBar() {
        top_bar_title.text = "Sign In"
        top_bar_mic.visibility = View.INVISIBLE
    }

    override fun showFirstBotMessage() {
        handleNewMessageLogic("Hello, what would you like to do?")
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

    fun runMessageFlowSignUp(msg: String) {
        handleNewMessageLogic(msg)
        getBotResponseForSignUp()
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
        }
    }

    fun getBotResponseForSignUp() {
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

    val setTimerTask: (name: String, delay: Long, () -> Unit) -> TimerTask = { name, delay, doit ->
        timerTask = Timer(name, false).schedule(delay) {
            runOnUiThread {
                doit()
            }
        }
        timerTaskArray.add(timerTask)
        timerTask
    }

    fun loadRetrievingOptionsFragment() {
        loadFragment(retrievingOptionsFragment)
    }

    fun removeRetrievingOptionsFragment() {
        supportFragmentManager.popBackStack()
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
                loadSignInErrorFragment()
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
                    loadEnterPinFragment()
                }
            }
            userPosition++
        }
    }

    private fun credentialsAreValid(): Boolean {
        return (username != null && password != null)
    }

    private fun navigateToUserDashboard() {
        retrievingOptionsFragment = RetrievingOptionsFragment.newInstance("Signing Up")
        loadRetrievingOptionsFragment()
        setTimerTask("signUp", 2000, {
            removeRetrievingOptionsFragment()
            toggleRestartFlag(false)
            startActivity(Intent(this@SignInActivity, DashboardActivity::class.java))
        })
    }

    fun signUpNewUser() {
        auth.createUserWithEmailAndPassword(username as String, password as String)
            .addOnSuccessListener {
                createUserDatabaseEntry(
                    auth.uid as String,
                    username as String,
                    password as String,
                    2000
                )
            }.addOnFailureListener {
                Toast.makeText(this, it.localizedMessage, Toast.LENGTH_LONG).show()
                loadSignInErrorFragment()
            }
    }

    private fun createUserDatabaseEntry(
        userId: String,
        email: String,
        password: String,
        pointsRemaining: Int
    ) {
        databaseReference.child(USERS.plus(userId)).setValue(User(email, password, pointsRemaining))
            .addOnSuccessListener {
                navigateToUserDashboard()
            }.addOnFailureListener {
                Toast.makeText(this, it.localizedMessage, Toast.LENGTH_LONG).show()
            }
    }

    fun signInExistingUser() {
        auth.signInWithEmailAndPassword(username as String, password as String)
            .addOnSuccessListener {
                signIn()
            }.addOnFailureListener {
                Toast.makeText(this, it.localizedMessage, Toast.LENGTH_LONG).show()
                loadSignInErrorFragment()
            }
    }

    fun signIn() {
        retrievingOptionsFragment = RetrievingOptionsFragment.newInstance("Logging In")
        loadRetrievingOptionsFragment()
        setTimerTask("signIn", 2000, {
            removeRetrievingOptionsFragment()
            toggleRestartFlag(false)
            startActivity(Intent(this@SignInActivity, DashboardActivity::class.java))
        })
    }

    fun signInAsGuest() {
        retrievingOptionsFragment = RetrievingOptionsFragment.newInstance("Guest Mode")
        loadRetrievingOptionsFragment()
        setTimerTask("signInAsGuest", 2000, {
            removeRetrievingOptionsFragment()
            toggleRestartFlag(false)
            startActivity(Intent(this@SignInActivity, DashboardActivity::class.java))
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

    private fun loadSignInErrorFragment() {
        supportFragmentManager
            .beginTransaction()
            .replace(optionsPopupContainer.id, signInErrorFragment)
            .addToBackStack(signInErrorFragment.javaClass.name)
            .commit()
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

    fun refreshSignInFlow() {
        finish()
        overridePendingTransition(0, 0);
        startActivity(intent)
        overridePendingTransition(0, 0);
    }

    companion object {
        const val SIGN_IN_SEQUENCE = 0
        const val FRAGMENT_ENTER_USERNAME = 10
        const val FRAGMENT_ENTER_PASSWORD = 20
        const val FRAGMENT_ENTER_EMAIL = 30
        const val FRAGMENT_CHOOSE_PASSWORD = 40
        const val FRAGMENT_REENTER_PASSWORD = 50
        const val FRAGMENT_ENTER_PIN = 60
    }
}
