package com.example.chatter.ui.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import com.example.chatter.R
import com.example.chatter.extra.MyBounceInterpolator
import com.example.chatter.ui.fragment.FlashCardCategoriesFragment
import com.example.chatter.ui.fragment.NavigationDrawerFragment
import com.example.chatter.ui.fragment.QuizDescriptionFragment
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_dashboard.*
import kotlinx.android.synthetic.main.activity_home_navigation.*
import kotlinx.android.synthetic.main.home_navigation_toolbar.*

class HomeNavigationActivity : BaseActivity() {
    private lateinit var auth: FirebaseAuth
    private var jokesFragment = QuizDescriptionFragment.newInstance(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_navigation)
        auth = FirebaseAuth.getInstance()
        setUpTopBar()
        setUpButtons()
    }

    fun loadFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
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
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
            }
        }
        nav_image3.setOnDebouncedClickListener {
            nav_image3.startBounceAnimation {
                val intent = Intent(this, GamesActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
            }
        }
        nav_image4.setOnDebouncedClickListener {
            nav_image4.startBounceAnimation {
                val intent = Intent(this, JokesActivity::class.java)
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

    fun View.startBounceAnimation(doStuff: () -> Unit) {
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