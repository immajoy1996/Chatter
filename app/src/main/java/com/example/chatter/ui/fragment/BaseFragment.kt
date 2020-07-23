package com.example.chatter.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.chatter.R
import com.example.chatter.extra.Preferences
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import java.util.*

abstract class BaseFragment : Fragment() {
    var timerTaskArray = arrayListOf<TimerTask>()
    lateinit var preferences: Preferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context?.let {
            preferences = Preferences(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_base, container, false)
    }

    override fun onPause() {
        super.onPause()
        for (timerTask in timerTaskArray) {
            timerTask.cancel()
        }
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
