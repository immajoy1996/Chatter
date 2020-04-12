package com.example.chatter

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError

abstract class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base)
    }

    val createBotListener: ((DataSnapshot) -> Unit) -> ChildEventListener = { doit ->
        val messageListener = object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, p1: String?) {
                doit(dataSnapshot)
            }

            override fun onCancelled(p0: DatabaseError) {
                //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
                //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onChildRemoved(p0: DataSnapshot) {
                //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        }
        messageListener
    }

    abstract fun setUpTopBar()
}
