package com.example.chatter

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_concentration.*
import kotlinx.android.synthetic.main.top_bar.*

class ConcentrationActivity : BaseActivity(), RevealItemInterface {
    var imageList = arrayListOf<String>()
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        database = FirebaseDatabase.getInstance().reference
        setContentView(R.layout.activity_concentration)
        setUpTopBar()
        setUpProfileGridView()
        fetchBotItemImages()
    }

    override fun revealItem() {
        setTimerTask("revealCardItem", 800) {
            concentration_recycler.adapter?.notifyDataSetChanged()
        }
    }

    private fun fetchBotItemImages() {
        while (imageList.size < 25) {
            if (Math.random() > .5) {
                imageList.add("https://firebasestorage.googleapis.com/v0/b/chatter-f7ae2.appspot.com/o/botImages%2Fduck.png?alt=media&token=224ae912-16cf-4cdd-bb53-7166d36e4588")
            } else {
                imageList.add("https://firebasestorage.googleapis.com/v0/b/chatter-f7ae2.appspot.com/o/botImages%2Fmountain.png?alt=media&token=93fc6cdc-e15d-4317-9dc2-fc1b5836b872")
            }
        }
        concentration_recycler.adapter = ConcentrationAdapter(this, imageList, this)
        /*val pathRef = database.child("BotCatalog")
        val imageListener = baseChildEventListener { dataSnapshot ->
            val imagePath = dataSnapshot.child("botImage").value.toString()
            imageList.add(imagePath)
            concentration_recycler.adapter = ProfileAdapter(this, imageList)
        }
        pathRef.addChildEventListener(imageListener)*/
    }

    private fun setUpProfileGridView() {
        concentration_recycler.layoutManager = GridLayoutManager(this, 5)
    }

    override fun setUpTopBar() {
        back.visibility = View.VISIBLE
        home.visibility = View.GONE
        top_bar_mic.visibility = View.GONE
        top_bar_title.text = "Concentration"
        concentration_game_timer.visibility = View.VISIBLE
        back.setOnClickListener {
            finish()
        }
    }
}