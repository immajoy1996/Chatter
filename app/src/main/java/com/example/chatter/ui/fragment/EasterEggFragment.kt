package com.example.chatter.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.example.chatter.extra.Preferences
import com.example.chatter.R
import com.example.chatter.ui.activity.ChatterActivity
import com.example.chatter.ui.activity.DashboardActivity
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_easter_egg.*

class EasterEggFragment : Fragment() {
    var message: String? = null
    var points: Long? = null
    var imageSrc: String? = null
    var imageGem: Int? = null
    private lateinit var database: DatabaseReference
    private lateinit var preferences: Preferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_easter_egg, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        database = FirebaseDatabase.getInstance().reference
        context?.let {
            preferences = Preferences(it)
        }
    }

    override fun onResume() {
        super.onResume()
        setUpButtons()
        setUpViews()
    }

    private fun updateUserTotalScore() {
        points?.let {
            (activity as? ChatterActivity)?.updateTotalScore(it)
        }
    }

    private fun setUpButtons() {
        easter_egg_close_button.setOnClickListener {
            if (activity is DashboardActivity) {
                (activity as? DashboardActivity)?.removeNewBotsAquiredFragment()
            } else {
                (activity as? ChatterActivity)?.let {
                    it.closeEasterEggFragment()
                    updateUserTotalScore()
                }
            }
        }
    }

    private fun setUpViews() {
        if (activity is DashboardActivity) {
            easter_egg_message.text = message
            easter_egg_price_tag_layout.visibility = View.GONE
            new_gem_image.visibility = View.GONE
            new_bots_image.visibility = View.VISIBLE
        } else {
            easter_egg_message.text = message
            if (points != null) {
                easter_egg_price_tag_layout.visibility = View.VISIBLE
                new_gem_image.visibility = View.GONE
                new_bots_image.visibility = View.GONE
                easter_egg_price.text = points.toString()
            } else {
                easter_egg_price_tag_layout.visibility = View.GONE
                new_gem_image.visibility = View.VISIBLE
                new_bots_image.visibility = View.GONE
                imageGem?.let {
                    new_gem_image.setImageResource(it)
                }
            }
        }
    }

    private fun showEasterEggAnimation() {
        val aniFade = AnimationUtils.loadAnimation(
            context,
            R.anim.fade_out_long
        )
        easter_egg_layout.startAnimation(aniFade)
        aniFade.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(arg0: Animation) {}
            override fun onAnimationRepeat(arg0: Animation) {}
            override fun onAnimationEnd(arg0: Animation) {
                (activity as? ChatterActivity)?.let {
                    it.closeEasterEggFragment()
                }
            }
        })
    }

    companion object {
        fun newInstance(message: String, points: Long, imageSrc: String): EasterEggFragment {
            val fragment = EasterEggFragment()
            fragment.message = message
            fragment.points = points
            fragment.imageSrc = imageSrc
            return fragment
        }

        fun newInstance(message: String, imageGem: Int): EasterEggFragment {
            val fragment = EasterEggFragment()
            fragment.message = message
            fragment.imageGem = imageGem
            return fragment
        }

        fun newInstance(message: String): EasterEggFragment {
            val fragment = EasterEggFragment()
            fragment.message = message
            return fragment
        }
    }
}
