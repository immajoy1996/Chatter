package com.example.chatter

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.top_bar.*

class ProfileActivity : BaseActivity() {
    var imageList = arrayListOf<String>()
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        database = FirebaseDatabase.getInstance().reference
        setContentView(R.layout.activity_profile)
        setUpProfileGridView()
        setUpTopBar()
        fetchProfileImages()
    }

    private fun fetchProfileImages() {
        val pathRef = database.child("ProfileImages")
        val imageListener = baseChildEventListener { dataSnapshot ->
            val imagePath = dataSnapshot.child("image").value.toString()
            imageList.add(imagePath)
            profiles_recycler.adapter = ProfileAdapter(this, imageList)
        }
        pathRef.addChildEventListener(imageListener)
    }

    private fun setUpProfileGridView() {
        profiles_recycler.layoutManager = GridLayoutManager(this, 5)

        val profileAdapter = ProfileAdapter(
            this,
            arrayListOf()
        )
        profiles_recycler.adapter = profileAdapter
    }

    override fun setUpTopBar() {
        back.visibility = View.VISIBLE
        home.visibility = View.GONE
        top_bar_mic.visibility = View.GONE
        top_bar_save_button.visibility = View.VISIBLE
        top_bar_title.text = "Profile"
        back.setOnClickListener {
            finish()
        }
    }
}
