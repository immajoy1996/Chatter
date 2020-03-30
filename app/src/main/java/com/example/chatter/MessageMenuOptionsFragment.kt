package com.example.chatter

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintSet
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_chatter.*
import kotlinx.android.synthetic.main.fragment_message_options.*
import kotlinx.android.synthetic.main.fragment_message_options.view.*
import java.util.*
import kotlin.concurrent.schedule
import kotlin.concurrent.timer


class MessageMenuOptionsFragment : Fragment() {

    private val chatterActivity by lazy {
        activity as ChatterActivity
    }

    private lateinit var database: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_message_options, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        database = FirebaseDatabase.getInstance().reference
        initializeScrollContainerAndOptionButtons()
        checkForEasterEggs()
    }

    private fun initializeScrollContainerAndOptionButtons() {
        chatterActivity.let {
            it.initializeMessagesContainer()
            setUpOptionsMenu(it.currentPath)
            setUpAllOptionClickListeners(it.currentPath)
        }
    }

    fun setUpOptionsMenu(currentPath: String) {
        val pathA = currentPath + "optionA"
        val pathB = currentPath + "optionB"
        val pathC = currentPath + "optionC"
        setUpOptionMenuText(pathA, optionA)
        setUpOptionMenuText(pathB, optionB)
        setUpOptionMenuText(pathC, optionC)
    }

    fun setUpMenuOptionClickListener(currentPath: String, option: TextView, optionType: String) {
        val path = currentPath + optionType + "/"
        option.setOnClickListener {
            chatterActivity.let {
                it.currentPath = path
                it.handleNewMessageLogic(option.text.toString())
                getBotResponse(it.currentPath)
            }
        }
    }

    fun setUpAllOptionClickListeners(currentPath: String) {
        setUpMenuOptionClickListener(currentPath, optionA, "optionA")
        setUpMenuOptionClickListener(currentPath, optionB, "optionB")
        setUpMenuOptionClickListener(currentPath, optionC, "optionC")
    }

    fun getBotResponse(path: String) {
        chatterActivity.removeOptionsMenu()

        val pathRef = database.child(path + "/botMessage")
        val messageListener = createMessageListener { botMessage ->

            Timer("showOptionsTimer", false).schedule(700) {
                chatterActivity.runOnUiThread {
                    chatterActivity.addSpaceText()
                    chatterActivity.showBotIsTypingView()
                }
            }

            Timer("showOptionsTimer", false).schedule(2000) {
                chatterActivity.runOnUiThread {
                    chatterActivity.replaceBotIsTyping(botMessage)
                    chatterActivity.loadOptionsMenu()
                }
            }
        }
        pathRef.addValueEventListener(messageListener)
    }

    fun checkForEasterEggs() {
        chatterActivity.currentPath.let {
            val pathRef = database.child(it)
            val easterEggListener = createEasterEggListener { dataSnapshot ->
                if (dataSnapshot.hasChild("title") && dataSnapshot.hasChild("points") && dataSnapshot.hasChild("image")) {
                    val title = dataSnapshot.child("title").value.toString()
                    val points = dataSnapshot.child("points").value as Long
                    val image=dataSnapshot.child("image").value.toString()
                    chatterActivity.loadEasterEggFragment(title, points,image)
                }
            }
            pathRef.addChildEventListener(easterEggListener)
        }
    }

    val createEasterEggListener: ((DataSnapshot) -> Unit) -> ChildEventListener = { doit ->
        val messageListener = object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, p1: String?) {
                doit(dataSnapshot)
            }

            override fun onChildRemoved(p0: DataSnapshot) {
                //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
                //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onCancelled(p0: DatabaseError) {
                //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        }
        messageListener
    }

    val runLogicAfterTimer: ((Unit) -> Unit) -> Unit = { doit ->
        Timer("showOptionsTimer", false).schedule(1000) {
            doit
        }
    }

    fun setUpOptionMenuText(path: String, option: TextView) {
        val pathRef = database.child(path + "/text")
        val messageListener = createMessageListener { optionText ->
            option.text = optionText
        }
        pathRef.addValueEventListener(messageListener)
    }

    val createMessageListener: ((String) -> Unit) -> ValueEventListener = { doit ->
        val messageListener = object : ValueEventListener {
            override fun onDataChange(data: DataSnapshot) {
                doit(data.value.toString())
            }

            override fun onCancelled(data: DatabaseError) {
                //TODO
            }

        }
        messageListener
    }

    companion object {

        fun newInstance(): MessageMenuOptionsFragment {
            return MessageMenuOptionsFragment()
        }
    }
}
