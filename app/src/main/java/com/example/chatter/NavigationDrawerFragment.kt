package com.example.chatter

import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.fragment_navigation_drawer.*

class NavigationDrawerFragment : BaseFragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_navigation_drawer, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference
        setUpUserInfo()
        setUpNavigationOptionButtons()
        setUpButtons()
    }

    private fun setUpUserInfo() {
        val curUid = auth.currentUser?.uid
        curUid?.let {
            val emailRef = databaseReference.child(USERS.plus(it)).child("email")
            val pointsRemainingRef =
                databaseReference.child(USERS.plus(it)).child("pointsRemaining")
            var emailListener = baseValueEventListener { dataSnapshot ->
                val email = dataSnapshot.value.toString()
                navigation_drawer_username.text = email.removeSuffix("@gmail.com")
            }
            var pointsRemainingListener = baseValueEventListener { dataSnapshot ->
                val pointsRemaining = dataSnapshot.value as Long
                navigation_drawer_user_score.text = pointsRemaining.toString()
            }
            emailRef.addValueEventListener(emailListener)
            pointsRemainingRef.addValueEventListener(pointsRemainingListener)
        }
    }

    private fun setUpNavigationOptionButtons() {
        drawer_my_subscription_image.setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_ATOP)
        drawer_settings_image.setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_ATOP)
        drawer_logout_image.setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_ATOP)
    }

    private fun setUpButtons() {
        drawer_my_logout_layout.setOnClickListener {
            activity?.finish()
            auth.signOut()
            startActivity(Intent(context, SignInActivity::class.java))
        }
        drawer_close_layout.setOnClickListener {
            fragmentManager?.popBackStack()
        }
        drawer_close_image.setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_ATOP)
    }

    companion object {
        const val USERS = "Users/"
    }
}
