package com.example.chatter.ui.fragment

import android.Manifest
import android.content.Context.AUDIO_SERVICE
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.FragmentManager
import com.example.chatter.R
import com.example.chatter.extra.MyBounceInterpolator
import com.example.chatter.ui.activity.*
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_chatter.*
import kotlinx.android.synthetic.main.bottom_nav_bar.*
import kotlinx.android.synthetic.main.fragment_story_board_one.*
import kotlinx.android.synthetic.main.top_bar.*


class StoryBoardOneFragment : BaseFragment() {

    var botTitle: String? = null
    private lateinit var database: DatabaseReference
    private var topMessage1 =
        "Jokes and puns are an important part of grasping any language."
    private var topMessage2 = " Jokes and puns are an important part of grasping any language."
    private var continueMessage = "Click Next to start"

    private var bearPersonality = "Mean"
    private var quotesArray = arrayListOf<String>()
    private var quoteIndex = 0

    private var clicked = false
    private val handler = Handler()
    private var characterAdder: Runnable? = null

    private var mediaPlayer = MediaPlayer()
    private lateinit var audioManager: AudioManager

    private var sadToastShow = false
    private var happyToastShown = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_story_board_one, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        database = FirebaseDatabase.getInstance().reference
        audioManager = context?.getSystemService(AUDIO_SERVICE) as AudioManager
        setUpTopBar()
        setUpNavButtons()
        //setUpPersonalityButtons()
        //setUpBearClick()
        //setUpAnswerButton()
        showNextJoke()
        setUpJokeButtons()
    }

    private fun showTypingAnimation(textView: TextView, delay: Long, message: String) {
        val handler = Handler()
        var pos = 0
        val characterAdder: Runnable = object : Runnable {
            override fun run() {
                val setStr = message.subSequence(0, pos + 1)
                textView.setText(setStr)
                pos++
                if (pos == message.length) {
                    handler.removeCallbacksAndMessages(null)
                } else {
                    handler.postDelayed(this, delay)
                }
            }
        }

        handler.removeCallbacks(characterAdder);
        handler.postDelayed(characterAdder, delay)
    }

    override fun onResume() {
        super.onResume()
        //initializeNextButton()
    }

    private fun initializeNextButton() {
        if ((activity as? ChatterActivity)?.textToSpeechInitialized() == true) {
            enableNextButton()
        } else {
            disableNextButton()
        }
    }

    private fun hideBearChatBubble() {
        bear_chat_bubble.visibility = View.GONE
        tap_me_textview.visibility = View.GONE
    }

    private fun showInitAudioAnimation(delay: Long) {
        hideBearChatBubble()
        initializing_audio_textview.visibility = View.VISIBLE
        initializing_audio_textview.text = "Waking up bear bot"
        val typingMessage = "Waking up bear bot ".plus("...")
        val start = "Waking up bear bot ".length
        var pos = 0
        characterAdder = object : Runnable {
            override fun run() {
                if (initializing_audio_textview.text.contains("Waking up bear bot")) {
                    var setStr = typingMessage.subSequence(0, start + pos + 1)
                    val spannableString = SpannableString(setStr)
                    spannableString.setSpan(
                        ForegroundColorSpan(Color.BLACK),
                        setStr.length - 1,
                        setStr.length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    initializing_audio_textview.setText(spannableString)
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

    override fun onDestroy() {
        super.onDestroy()
        characterAdder?.let {
            handler.removeCallbacks(it)
        }
    }

    private fun showInitializeAudioMessage() {
        showInitAudioAnimation(300)
        (activity as? ChatterActivity)?.let {
            it.setTimerTask("showInitAudioAnimation", 3000, {
                hideInitAudioMessageAndWakeButton()
                bearBotFadeIn()
                initializeNextButton()
            })
        }
    }

    private fun bearBotFadeIn() {
        if (this.isVisible) {
            bear_profile.visibility = View.VISIBLE
            bear_chat_bubble.visibility = View.VISIBLE
            tap_me_textview.visibility = View.VISIBLE
            val aniScaleIn = AnimationUtils.loadAnimation(
                context,
                R.anim.scale_in
            )
            bear_profile.startAnimation(aniScaleIn)
            bear_chat_bubble.startAnimation(aniScaleIn)
            tap_me_textview.startAnimation(aniScaleIn)
            aniScaleIn.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(arg0: Animation) {}
                override fun onAnimationRepeat(arg0: Animation) {}
                override fun onAnimationEnd(arg0: Animation) {
                    //bear_answer_button.visibility = View.VISIBLE
                }
            })
        }
    }

    private fun hideInitAudioMessageAndWakeButton() {
        if (this.isVisible) {
            wake_up_bear_button.visibility = View.GONE
            initializing_audio_textview.text = "Complete"
            initializing_audio_textview.visibility = View.GONE
        }
    }

    private fun animateBearShake() {
        val aniShake = AnimationUtils.loadAnimation(
            context,
            R.anim.shake
        )
        bear_profile.startAnimation(aniShake)
    }

    fun showNextJoke() {
        val joke =
            (activity as? JokesActivity)?.getNextJoke() ?: ""
        showTypingAnimation(joke_textview, 50, joke)
    }

    private fun setUpBearClick() {
        bear_profile.setOnDebouncedClickListener {

            animateBearShake()
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop()
                mediaPlayer.release()
                mediaPlayer = MediaPlayer()
            }
            if (audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) == 0) {
                Toast.makeText(context, "Turn up your volume", Toast.LENGTH_LONG).show()
            }
            if (activity is ChatterActivity) {
                (activity as? ChatterActivity)?.sayBearsNextQuote()
                val joke =
                    (activity as? ChatterActivity)?.getNextJoke() ?: ""
                //showTypingAnimation(tap_me_textview, 300, joke)
            } else {
                (activity as? CreateChatActivity)?.sayBearsNextQuote()
            }
            answerButtonFadeIn()
        }
    }

    private fun answerButtonFadeIn() {
        bear_answer_button.visibility = View.INVISIBLE
        bear_answer_button.isClickable = true
        joke_answer_textview.visibility = View.INVISIBLE
        val aniFadeIn = AnimationUtils.loadAnimation(
            context,
            R.anim.fade_in
        )
        bear_answer_button.startAnimation(aniFadeIn)
        aniFadeIn.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(arg0: Animation) {}
            override fun onAnimationRepeat(arg0: Animation) {}
            override fun onAnimationEnd(arg0: Animation) {
                bear_answer_button.visibility = View.VISIBLE
            }
        })
    }

    private fun setUpAudio() {
        (activity as? ChatterActivity)?.letBearSpeak("")
    }

    private fun setUpPersonalityButtons() {
        wake_up_bear_button.setOnClickListener {
            //top_message.text = topMessage2
            if (audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) == 0) {
                Toast.makeText(context, "Turn up your volume", Toast.LENGTH_LONG).show()
            }
            playBearYawn()
            wake_up_bear_button.isClickable = false
            showInitializeAudioMessage()
            setUpAudio()
        }
    }

    private fun playBearYawn() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop()
            mediaPlayer.release()
        }
        mediaPlayer = MediaPlayer.create(context, R.raw.bear_yawn)
        mediaPlayer.start()
    }

    private fun setUpTopBar() {
        top_bar_title.setText("Jokes!")
        back.visibility = View.VISIBLE
        back.setOnClickListener {
            activity?.finish()
            //fragmentManager?.popBackStack()
            (activity as? DashboardActivity)?.loadFragment(QuizDescriptionFragment.newInstance(false))
        }
        home.visibility = View.GONE
        top_bar_mic.visibility = View.INVISIBLE
    }

    val createStoryboardListener: ((DataSnapshot) -> Unit) -> ValueEventListener = { doit ->
        val storyboardListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                doit(dataSnapshot)
            }

            override fun onCancelled(p0: DatabaseError) {
                //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        }
        storyboardListener
    }

    private fun setUpStoryText(text: String) {
        //story_message1.visibility = View.VISIBLE
        //story_message1.text = text
    }

    private fun setUpStoryText2(text: String) {
        story_message2.text = text
    }

    private fun setUpNavButtons() {
        button_back.setOnClickListener {
            fragmentManager?.popBackStack()
            (activity as? ChatterActivity)?.toggleRestartFlag(false)
            (activity as? ChatterActivity)?.finish()
        }
        enableNextButton()
    }

    private fun enableNextButton() {
        //button_next.isClickable = true
        //button_next.setBackgroundResource(R.drawable.message_bubble)
        button_next.setOnClickListener {
            fragmentManager?.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            (activity as? ChatterActivity)?.onStoriesFinished()
        }
    }

    private fun setUpJokeButtons() {
        show_answer_button.setOnDebouncedClickListener {
            show_answer_button.visibility = View.GONE
            var jokeAnswer: String? = null
            jokeAnswer = (activity as? JokesActivity)?.getCurrentJokeAnswer()
            answer_textview.text = jokeAnswer ?: "Oops, something went wrong"
            val fadeInAni =
                AnimationUtils.loadAnimation(context, R.anim.fade_in)
            answer_textview.visibility = View.INVISIBLE
            answer_textview.startAnimation(fadeInAni)
            fadeInAni.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(arg0: Animation) {}
                override fun onAnimationRepeat(arg0: Animation) {}
                override fun onAnimationEnd(arg0: Animation) {
                    answer_textview.visibility = View.VISIBLE
                }
            })
        }
        happy_button.setOnDebouncedClickListener {
            if (!happyToastShown) {
                /*Toast.makeText(
                    context,
                    "Thanks! Your feedback lets us choose jokes at your level",
                    Toast.LENGTH_LONG
                ).show()*/
                happyToastShown = true
            }
            val shakeAni =
                AnimationUtils.loadAnimation(context, R.anim.shake)
            happy_button.startAnimation(shakeAni)
            show_answer_button.visibility = View.VISIBLE
            show_answer_button.text = "Show Answer"
            answer_textview.visibility = View.GONE
            showNextJoke()
        }
        sad_button.setOnDebouncedClickListener {
            if (!sadToastShow) {
                /*Toast.makeText(
                    context,
                    "Thanks! Your feedback lets us choose jokes at your level",
                    Toast.LENGTH_LONG
                ).show()*/
                sadToastShow = true
            }
            val shakeAni =
                AnimationUtils.loadAnimation(context, R.anim.shake)
            sad_button.startAnimation(shakeAni)
            (activity as? JokesActivity)?.loadJokesExplanationFragment()
            show_answer_button.visibility = View.VISIBLE
            show_answer_button.text = "Show Answer"
            answer_textview.visibility = View.GONE
            hideSmilesButtonsLayout()
        }
    }

    private fun hideSmilesButtonsLayout() {
        happy_layout.visibility = View.GONE
        sad_layout.visibility = View.GONE
        did_you_get_it.visibility = View.GONE
    }

    private fun showSmilesButtonsLayout() {
        happy_layout.visibility = View.VISIBLE
        sad_layout.visibility = View.VISIBLE
        did_you_get_it.visibility = View.VISIBLE
    }

    private fun disableNextButton() {
        button_next.setBackgroundResource(R.drawable.nav_button_disabled)
        button_next.isClickable = false
    }

    companion object {
        const val BOT_CATALOG = "BotCatalog/"
        const val PERMISSION_REQUEST_CODE = 100
        const val BEAR_YAWN_PATH =
            "https://firebasestorage.googleapis.com/v0/b/chatter-f7ae2.appspot.com/o/vocabAudio%2Fbear_yawn.mp3?alt=media&token=d531b301-d03c-4181-bdb0-61079b25145d"

        fun newInstance(botTitle: String): StoryBoardOneFragment {
            val fragment =
                StoryBoardOneFragment()
            fragment.botTitle = botTitle
            return fragment
        }
    }
}
