package com.example.chatter

import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.fragment_navigation_drawer.*

class NavigationDrawerFragment : BaseFragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var preferences: Preferences
    private var isGuestMode = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_navigation_drawer, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isGuestMode = (activity as? DashboardActivity)?.isGuestMode() ?: false
        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference
        context?.let {
            preferences = Preferences(it)
        }
        setUpUserInfo()
        setUpNavigationOptionButtons()
        setUpButtons()
    }

    private fun setUpUserInfo() {
        if (!isGuestMode) {
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
                emailRef.addListenerForSingleValueEvent(emailListener)
                pointsRemainingRef.addListenerForSingleValueEvent(pointsRemainingListener)
                user_level.setText(preferences.getUserLevel())
                setUpUserLevelImage()
            }
        } else {
            setUserBadge(R.drawable.pawn)
            navigation_drawer_username.text = "Guest"
            navigation_drawer_user_score.text = "1000"
            user_level.setText("Pawn")
        }
    }

    private fun setUpUserLevelImage() {
        var curUserLevel = preferences.getUserLevel()
        when (curUserLevel) {
            "Pawn" -> {
                setUserBadge(R.drawable.pawn)
            }
            "Knight" -> {
                setUserBadge(R.drawable.knight)
            }
        }
    }

    private fun setUserBadge(drawableResource: Int) {
        context?.let {
            user_badge.setImageDrawable(ContextCompat.getDrawable(it, drawableResource))
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
