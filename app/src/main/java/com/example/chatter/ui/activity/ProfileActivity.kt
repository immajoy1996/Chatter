package com.example.chatter.ui.activity

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.example.chatter.extra.Preferences
import com.example.chatter.adapters.ProfileAdapter
import com.example.chatter.interfaces.ProfileClickInterface
import com.example.chatter.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.top_bar.*

class ProfileActivity : BaseActivity(),
    ProfileClickInterface {
    var imageList = arrayListOf<String>()
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private var selectedProfileImage: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        database = FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()
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
            profiles_recycler.adapter =
                ProfileAdapter(
                    this,
                    imageList,
                    this
                )
        }
        pathRef.addChildEventListener(imageListener)
    }

    private fun setUpProfileGridView() {
        profiles_recycler.layoutManager = GridLayoutManager(this, 5)

        val profileAdapter = ProfileAdapter(
            this,
            arrayListOf(),
            this
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
        top_bar_save_button.setOnClickListener {
            if (selectedProfileImage != null) {
                if (auth.currentUser != null) {
                    auth.currentUser?.uid?.let {
                        val uid = it
                        database.child("Users/${uid}").child("profileImage")
                            .setValue(selectedProfileImage as String).addOnSuccessListener {
                                Toast.makeText(this, "Selection saved", Toast.LENGTH_LONG).show()
                                finish()
                            }.addOnFailureListener {
                                Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG)
                                    .show()
                            }
                    }
                } else {
                    preferences.storeProfileImageSelection(selectedProfileImage as String)
                    Toast.makeText(this, "Selection saved", Toast.LENGTH_LONG).show()
                    finish()
                }
            } else {
                Toast.makeText(this, "Select an image", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onProfileClicked(image: String) {
        selectedProfileImage = image
    }
}
