package com.example.chatter

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.bottom_nav_bar.*
import kotlinx.android.synthetic.main.fragment_story_board_one.*
import kotlinx.android.synthetic.main.top_bar.*


class StoryBoardOneFragment : Fragment() {

    var botTitle: String? = null
    private lateinit var database: DatabaseReference
    private var topMessage1 = "Tap Bear to see what he has to say"
    private var topMessage2 = "Tap Bear one more time"
    private var continueMessage = "Click Next to start"

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
        setUpBearClick()
    }

    private fun setUpBearClick() {
        bear_profile.setOnClickListener {
            if (top_message.text == topMessage1) {
                fetchStoryBoardMessageOne()
            } else {
                fetchStoryBoardMessageTwo()
            }
        }
    }

    private fun setUpTopBar() {
        top_bar_title.setText("Storyboard")
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
        story_message1.visibility = View.VISIBLE
        story_message1.text = text
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
