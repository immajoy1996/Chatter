package com.example.chatter.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.example.chatter.R
import com.example.chatter.ui.activity.ChatterActivity
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.bottom_nav_bar.button_back
import kotlinx.android.synthetic.main.fragment_story_board_two.*
import kotlinx.android.synthetic.main.fragment_story_board_two.story_message


class StoryBoardTwoFragment : Fragment() {
    var botTitle: String? = null
    private lateinit var database: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_story_board_two, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        database = FirebaseDatabase.getInstance().reference
        setUpNavButtons()
        fetchStoryBoardMessages()
    }

    private fun fetchStoryBoardMessages() {
        val storyReference = database.child(StoryBoardOneFragment.BOT_CATALOG.plus(botTitle).plus("/storyboardText2"))

        val storyboardListener = createStoryboardListener { dataSnapshot ->
            val storyText2 = dataSnapshot.value.toString()
            setUpStoryText(storyText2)
        }
        storyReference.addValueEventListener(storyboardListener)
    }

    val createStoryboardListener: ((DataSnapshot) -> Unit) -> ValueEventListener = { doit ->
        val storyboardListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                doit(dataSnapshot)
            }

            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        }
        storyboardListener
    }

    private fun setUpStoryText(text: String) {
        story_message.text = text
    }

    private fun setUpNavButtons() {
        button_back.setOnClickListener {
            fragmentManager?.popBackStack()
            (activity as? ChatterActivity)?.loadFirstStoryBoardFragment()
        }
        button_start.setOnClickListener {
            fragmentManager?.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            (activity as? ChatterActivity)?.onStoriesFinished()
        }
    }

    companion object {
        const val BOT_CATALOG = "BotCatalog/"
        fun newInstance(botTitle: String): StoryBoardTwoFragment {
            val fragment =
                StoryBoardTwoFragment()
            fragment.botTitle = botTitle
            return fragment
        }
    }
}
