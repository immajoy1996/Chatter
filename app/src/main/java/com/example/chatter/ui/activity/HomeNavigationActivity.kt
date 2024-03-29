package com.example.chatter.ui.activity

import ProgressBarAnimation
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.UserHandle
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.chatter.R
import com.example.chatter.data.NativeLanguage
import com.example.chatter.extra.MyBounceInterpolator
import com.example.chatter.ui.fragment.EasterEggFragment
import com.example.chatter.ui.fragment.FlashCardCategoriesFragment
import com.example.chatter.ui.fragment.NavigationDrawerFragment
import com.example.chatter.ui.fragment.QuizDescriptionFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_dashboard.*
import kotlinx.android.synthetic.main.activity_home_navigation.*
import kotlinx.android.synthetic.main.home_navigation_toolbar.*

class HomeNavigationActivity : BaseActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private var jokesFragment = QuizDescriptionFragment.newInstance(false)
    private var langHasChanged = false
    private var targetLanguage = ""
    private var leveledUpFragment = EasterEggFragment.newInstance("You've leveled up!")
    private var noInternetFragment = EasterEggFragment.newInstance("Something went wrong. Please check your connection")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_navigation)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
        setUpTopBar()
        setUpButtons()
    }

    fun removePopup(){
        supportFragmentManager.popBackStack()
    }

    override fun onResume() {
        super.onResume()
        if(!canConnectToInternet(this)){
            loadFragment(noInternetFragment)
        }
        if (auth.currentUser != null) {
            //User signed in
            auth.currentUser?.let {
                val uid = it.uid
                val homeNavListener = baseValueEventListener { dataSnapshot ->
                    val level = dataSnapshot.child("level").value
                    val userHandle = dataSnapshot.child("email").value
                    val points = dataSnapshot.child("points").value
                    val profileImg = dataSnapshot.child("profileImage").value
                    val flagImg = dataSnapshot.child("nativeLanguage").child("flagImg").value

                    level?.let {
                        setUpLevel(it.toString())
                    }
                    userHandle?.let {
                        setUpUserHandle(it.toString())
                    }
                    points?.let {
                        setUpPoints(it.toString())
                    }
                    profileImg?.let {
                        setUpProfileImage(it.toString())
                    }
                    flagImg?.let {
                        setUpFlagImage(it.toString())
                    }
                }
                database.child("Users/${uid}").addListenerForSingleValueEvent(homeNavListener)
            }
        } else {
            if (preferences.getCurrentTargetLanguageFlag().isNotEmpty()) {
                setUpFlagImage(preferences.getCurrentTargetLanguageFlag())
            }
            if (preferences.getProfileImage().isNotEmpty()) {
                setUpProfileImage(preferences.getProfileImage())
            }
            preferences.getCurrentScore()?.let {
                setUpPoints(it.toString())
                setUpPointsProgressBar(it)
            }
        }
        if (targetLanguage != preferences.getCurrentTargetLanguage()) {
            newLanguageChosen()
            targetLanguage = preferences.getCurrentTargetLanguage()
        }
    }

    private fun newLanguageChosen() {
        langHasChanged = true
    }

    private fun resetLangChangedFlag() {
        langHasChanged = false
    }

    private fun showProgressAnimation(from: Int, to: Int, progressBar: ProgressBar) {
        val anim = ProgressBarAnimation(progressBar, from.toFloat(), to.toFloat())
        anim.duration = 1000
        progressBar.startAnimation(anim)
    }

    private fun setUpPointsProgressBar(totalPoints: Int) {
        val totalPointsForThisLevel = preferences.getTotalPointsForMyLevel(totalPoints)
        if (totalPoints >= totalPointsForThisLevel) {
            loadFragment(leveledUpFragment)
        }
        val from = home_activity_progress_bar.progress
        val to = preferences.getLevelCompletionPercentage(totalPoints)
        showProgressAnimation(from, to, home_activity_progress_bar)
        home_activity_level.text = preferences.getMyCurrentLevel(totalPoints)

    }

    fun updateTotalScore(pointsAdded: Long) {
        var oldScore: Int? = null
        var latestScore: Int? = null
        if (auth.currentUser != null) {
            auth.currentUser?.uid?.let {
                val userUid = it
                val pathRef = database.child(ChatterActivity.USERS).child(userUid).child("points")
                val pointsListener = baseValueEventListener { dataSnapshot ->
                    val currentScore = dataSnapshot.value as Long
                    oldScore = currentScore.toInt()
                    val newScore = currentScore + pointsAdded
                    latestScore = newScore.toInt()
                    database.child(ChatterActivity.USERS).child(userUid).child("points")
                        .setValue(newScore)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Points added", Toast.LENGTH_SHORT).show()
                        }
                }
                pathRef.addListenerForSingleValueEvent(pointsListener)
            }
        } else {
            //Guest mode
            val currentScore = preferences.getCurrentScore()
            oldScore = currentScore
            val newScore = currentScore + pointsAdded.toInt()
            latestScore = newScore
            preferences.storeCurrentScore(newScore)
            Toast.makeText(this, "Points added", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setUpLevel(level: String) {
        home_activity_level.text = level
    }

    private fun setUpUserHandle(userHandle: String) {
        top_bar_title_desc.text = userHandle.substringBefore("@gmail.com")
    }

    private fun setUpPoints(points: String) {
        home_bar_coin_total.text = points
    }

    private fun setUpProfileImage(profileImage: String) {
        top_bar_bot_image?.let {
            Glide.with(this)
                .load(profileImage)
                .into(it)
        }
    }

    private fun setUpFlagImage(langImage: String) {
        home_bar_flag?.let {
            Glide.with(this)
                .load(langImage)
                .into(it)
        }
    }

    fun loadFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(R.anim.slide_in, R.anim.slide_out)
            .replace(home_root_layout.id, fragment)
            .addToBackStack(fragment.javaClass.name)
            .commit()
    }

    private fun setUpButtons() {
        nav_image1.setOnDebouncedClickListener {
            nav_image1.startBounceAnimation {
                launchDashboardActivityAsGuest()
            }
        }
        nav_image2.setOnDebouncedClickListener {
            nav_image2.startBounceAnimation {
                val intent = Intent(this, FlashCardActivity::class.java)
                intent.putExtra("langChanged", langHasChanged)
                intent.putExtra("userLevel", home_activity_level.text.toString())
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                resetLangChangedFlag()
            }
        }
        nav_image3.setOnDebouncedClickListener {
            nav_image3.startBounceAnimation {
                val intent = Intent(this, GamesActivity::class.java)
                intent.putExtra("userLevel", home_activity_level.text.toString())
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
            }
        }
        nav_image4.setOnDebouncedClickListener {
            nav_image4.startBounceAnimation {
                val intent = Intent(this, JokesActivity::class.java)
                intent.putExtra("userLevel", home_activity_level.text.toString())
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
            }
        }
        signout_bar.setOnDebouncedClickListener {
            finish()
            auth.signOut()
            val intent = Intent(this, SignInActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        }
    }

    private fun View.startBounceAnimation(doStuff: () -> Unit) {
        val bounceAni =
            AnimationUtils.loadAnimation(this@HomeNavigationActivity, R.anim.bounce)
        val interpolator = MyBounceInterpolator(0.2, 20.0)
        bounceAni.interpolator = interpolator
        this.startAnimation(bounceAni)
        bounceAni.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(arg0: Animation) {}
            override fun onAnimationRepeat(arg0: Animation) {}
            override fun onAnimationEnd(arg0: Animation) {
                doStuff()
            }
        })
    }

    private fun launchDashboardActivityAsGuest() {
        val intent = Intent(this, DashboardActivity::class.java)
        intent.putExtra("GUEST_MODE", true)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }

    override fun onBackPressed() {
        //disable back button
    }

    override fun setUpTopBar() {
        home_bar_flag.setOnDebouncedClickListener {
            val intent = Intent(this, LanguageSelectionActivity::class.java)
            intent.putExtra(
                "ChangingDefaultLanguage",
                DashboardActivity.CHANGING_DEFAULT_LANG
            )
            startActivity(intent)
        }
        top_bar_bot_image.setOnDebouncedClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }
    }
}