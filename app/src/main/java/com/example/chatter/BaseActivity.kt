package com.example.chatter

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.tts.TextToSpeech
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import java.util.*
import kotlin.concurrent.schedule

abstract class BaseActivity : AppCompatActivity() {
    var guestMode: Boolean = false
    private lateinit var timerTask: TimerTask
    private var timerTaskArray = arrayListOf<TimerTask>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base)
    }

    val baseChildEventListener: ((DataSnapshot) -> Unit) -> ChildEventListener = { doit ->
        val childEventListener = object : ChildEventListener {
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
        childEventListener
    }

    val baseValueEventListener: ((DataSnapshot) -> Unit) -> ValueEventListener = { doit ->
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                doit(dataSnapshot)
            }

            override fun onCancelled(data: DatabaseError) {
                //TODO
            }

        }
        valueEventListener
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

    override fun onPause() {
        super.onPause()
        for (timerTask in timerTaskArray) {
            timerTask.cancel()
        }
    }

    abstract fun setUpTopBar()
}
