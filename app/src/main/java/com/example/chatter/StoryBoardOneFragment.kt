package com.example.chatter

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.speech.tts.TextToSpeech
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_chatter.*
import kotlinx.android.synthetic.main.bottom_nav_bar.*
import kotlinx.android.synthetic.main.fragment_story_board_one.*
import kotlinx.android.synthetic.main.top_bar.*
import kotlinx.android.synthetic.main.vocab_item_view.*
import java.util.*
import kotlin.collections.ArrayList


class StoryBoardOneFragment : Fragment() {

    var botTitle: String? = null
    private lateinit var database: DatabaseReference
    private var topMessage1 =
        "Wake up Bear Bot to see what he has to say. He's probably not going to be too happy."
    private var topMessage2 = "Tap Bear Bot to hear his valuable insight"
    private var continueMessage = "Click Next to start"

    private var bearPersonality = "Mean"
    private var quotesArray = arrayListOf<String>()
    private var quoteIndex = 0

    private var clicked = false
    private val handler = Handler()
    private var characterAdder: Runnable? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_story_board_one, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        database = FirebaseDatabase.getInstance().reference
        top_message.text = topMessage1
        setUpTopBar()
        setUpNavButtons()
        setUpPersonalityButtons()
        setUpBearClick()
    }

    private fun showBearBotInfo() {
        //top_message.text = topMessage2
        bearBotFadeIn()
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

    fun showInitializeAudioMessage() {
        showInitAudioAnimation(300)
        (activity as? ChatterActivity)?.let {
            it.setTimerTask("showInitAudioAnimation", 3000, {
                hideInitAudioMessageAndWakeButton()
                bearBotFadeIn()
            })
        }
    }

    private fun bearBotFadeIn() {
        if(this.isVisible) {
            bear_profile.visibility = View.INVISIBLE
            val aniFade = AnimationUtils.loadAnimation(
                context,
                R.anim.fade_in
            )
            bear_profile.startAnimation(aniFade)
            bear_chat_bubble.startAnimation(aniFade)
            tap_me_textview.startAnimation(aniFade)
            aniFade.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(arg0: Animation) {}
                override fun onAnimationRepeat(arg0: Animation) {}
                override fun onAnimationEnd(arg0: Animation) {
                    bear_profile.visibility = View.VISIBLE
                    bear_chat_bubble.visibility = View.VISIBLE
                    tap_me_textview.visibility = View.VISIBLE
                }
            })
        }
    }

    private fun hideInitAudioMessageAndWakeButton() {
        if(this.isVisible) {
            wake_up_bear_button.visibility = View.GONE
            initializing_audio_textview.text = "Complete"
            initializing_audio_textview.visibility = View.GONE
        }
    }

    private fun sayInstruction() {
        (activity as? ChatterActivity)?.letBearSpeak(quotesArray[quoteIndex])
        quoteIndex = (quoteIndex + 1) % quotesArray.size
    }

    private fun setUpBearClick() {
        bear_profile.setOnClickListener {
            if (bearPersonality == "Nice") {
                botTitle?.let {
                    quotesArray = (activity as? ChatterActivity)?.getBotStories(it)
                        ?: arrayListOf<String>("I have no idea about ".plus(it))
                    sayInstruction()
                }
            } else {
                (activity as? ChatterActivity)?.sayBearsNextQuote()
            }
        }
    }

    private fun setUpAudio() {
        (activity as? ChatterActivity)?.letBearSpeak("")
    }

    private fun setUpPersonalityButtons() {
        wake_up_bear_button.setOnClickListener {
            wake_up_bear_button.isClickable = false
            showInitializeAudioMessage()
            setUpAudio()
        }
    }
    /*bad_bear_button.setOnClickListener {
        if (bearPersonality != "Mean") {
            bear_profile.setImageResource(R.drawable.mad_bear)
            good_bear_button.setBackgroundResource(R.drawable.bear_personality_background_disabled)
            bad_bear_button.setBackgroundResource(R.drawable.bear_personality_background_enabled)
            bearPersonality = "Mean"
        }
    }*/

    private fun setUpTopBar() {
        top_bar_title.setText("Bear Bot")
        top_bar_mic.visibility = View.INVISIBLE
    }

    private fun fetchStoryBoardMessageOne() {
        val storyReference1 = database.child(BOT_CATALOG.plus(botTitle).plus("/storyboardText1"))

        val storyboardListener1 = createStoryboardListener { dataSnapshot ->
            val storyText1 = dataSnapshot.value.toString()
            setUpStoryText(storyText1)
            top_message.text = topMessage2
        }
        storyReference1.addListenerForSingleValueEvent(storyboardListener1)
    }

    private fun fetchStoryBoardMessageTwo() {
        val storyReference2 = database.child(BOT_CATALOG.plus(botTitle).plus("/storyboardText2"))

        val storyboardListener2 = createStoryboardListener { dataSnapshot ->
            val storyText2 = dataSnapshot.value.toString()
            setUpStoryText(storyText2)
            top_message.text = continueMessage
        }
        storyReference2.addListenerForSingleValueEvent(storyboardListener2)
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

        button_next.setOnClickListener {
            fragmentManager?.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            (activity as? ChatterActivity)?.onStoriesFinished()
        }
    }

    companion object {
        const val BOT_CATALOG = "BotCatalog/"
        fun newInstance(botTitle: String): StoryBoardOneFragment {
            val fragment = StoryBoardOneFragment()
            fragment.botTitle = botTitle
            return fragment
        }
    }
}
