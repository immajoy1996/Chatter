package com.example.chatter.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.chatter.R
import com.example.chatter.extra.Preferences
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
    lateinit var preferences: Preferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base)
        preferences = Preferences(this)
    }

    fun getNextJoke(): String {
        return preferences.getNextJoke()
    }

    fun getCurrentJokeAnswer(): String {
        return preferences.getCurrentJokeAnswer()
    }

    fun String.compareLevelTo(otherLevel: String): Int {
        if (this == otherLevel) return 0
        when (this) {
            "Easy" -> {
                return -1
            }
            "Medium" -> {
                if (otherLevel == "Easy") return 1
                else return -1
            }
            "Hard" -> {
                return 1
            }
        }
        return 0
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

    fun View.setOnDebouncedClickListener(doStuff: () -> (Unit)) {
        this.setOnClickListener {
            val lastClickTime: Long = preferences.getLastClickTime(it.id) ?: -1L
            val currentTime = System.currentTimeMillis()
            if (lastClickTime == -1L || currentTime - lastClickTime > MIN_TIME_BETWEEN_CLICKS) {
                preferences.storeLastClickTime(it.id, System.currentTimeMillis())
                doStuff()
            }
        }
    }

    companion object {
        const val MIN_TIME_BETWEEN_CLICKS = 700L
    }
}
