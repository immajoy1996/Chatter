package com.example.chatter

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_chatter.*
import kotlinx.android.synthetic.main.bottom_nav_bar.*
import kotlinx.android.synthetic.main.fragment_story_board_one.*


class StoryBoardOneFragment : Fragment() {

    var botTitle: String? = null
    private lateinit var database: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_story_board_one, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        database = FirebaseDatabase.getInstance().reference
        setUpNavButtons()
        fetchStoryBoardMessages()
    }

    private fun fetchStoryBoardMessages() {
        val storyReference1 = database.child(BOT_CATALOG.plus(botTitle).plus("/storyboardText1"))
        val storyReference2 = database.child(BOT_CATALOG.plus(botTitle).plus("/storyboardText2"))

        val storyboardListener1 = createStoryboardListener { dataSnapshot ->
            val storyText1 = dataSnapshot.value.toString()
            setUpStoryText1(storyText1)
        }
        val storyboardListener2 = createStoryboardListener { dataSnapshot ->
            val storyText2 = dataSnapshot.value.toString()
            setUpStoryText2(storyText2)
        }
        storyReference1.addValueEventListener(storyboardListener1)
        storyReference2.addValueEventListener(storyboardListener2)
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

    private fun setUpStoryText1(text: String) {
        story_message1.text = text
    }

    private fun setUpStoryText2(text: String) {
        story_message2.text = text
    }

    private fun setUpNavButtons() {
        button_back.setOnClickListener {
            fragmentManager?.popBackStack()
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
