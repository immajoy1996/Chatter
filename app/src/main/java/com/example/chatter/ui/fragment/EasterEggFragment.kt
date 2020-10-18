package com.example.chatter.ui.fragment

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.chatter.extra.Preferences
import com.example.chatter.R
import com.example.chatter.ui.activity.*
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
        updateUserTotalScore()
        setUpButtons()
        setUpViews()
    }

    private fun updateUserTotalScore() {
        points?.let {
            when (activity) {
                is ChatterActivity -> (activity as? ChatterActivity)?.updateTotalScore(it)
                is ConcentrationActivity -> (activity as? ConcentrationActivity)?.updateTotalScore(
                    it
                )
                is MultipleChoiceActivity -> (activity as? MultipleChoiceActivity)?.updateTotalScore(
                    it
                )
                is HomeNavigationActivity -> (activity as? HomeNavigationActivity)?.updateTotalScore(
                    it
                )
                is SpeechGameActivity -> (activity as? SpeechGameActivity)?.updateTotalScore(
                    it
                )
                else -> {
                    Toast.makeText(context, "No method defined to update score", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private fun setUpButtons() {
        easter_egg_close_button.setOnClickListener {
            when (activity) {
                is DashboardActivity -> {
                    (activity as? DashboardActivity)?.removeNewBotsAquiredFragment()
                }
                is ConcentrationActivity -> {
                    if (message?.contains("Resume") == true) {
                        (activity as? ConcentrationActivity)?.let {
                            it.removeStartGameFragment()
                            it.resumeGame()
                        }
                    } else {
                        (activity as? ConcentrationActivity)?.let {
                            it.removeStartGameFragment()
                            it.startGame()
                        }
                    }
                }
                is MultipleChoiceActivity -> {
                    (activity as? MultipleChoiceActivity)?.let {
                        it.removeStartGameFragment()
                        it.startGame()
                    }
                }
                is SpeechGameActivity -> {
                    preferences.incrementCurrentBotStoryIndex()
                    (activity as? SpeechGameActivity)?.returnToBotStoryActivity()

                }
                is HomeNavigationActivity -> {
                    (activity as? HomeNavigationActivity)?.removePopup()
                }
                is ChatterActivity -> {
                    (activity as? ChatterActivity)?.removePopup()
                }
                else -> {
                    Toast.makeText(context, "No method to handle this screen", Toast.LENGTH_SHORT)
                        .show()
                }
            }
            easter_egg_exit_button.setOnClickListener {//Play again button
                when (activity) {
                    is SpeechGameActivity -> {
                        (activity as? SpeechGameActivity)?.let {
                            it.removeStartGameFragment()
                            it.startGame()
                        }
                    }
                }
            }
        }
    }

    private fun setUpViews() {
        if (activity is DashboardActivity) {
            easter_egg_message.text = message
            easter_egg_price_tag_layout.visibility = View.GONE
            new_gem_image.visibility = View.GONE
            new_bots_image.visibility = View.GONE
            start_game_image.visibility = View.GONE
            jackpot_layout.visibility = View.VISIBLE
            if (message?.toLowerCase()?.contains("something went wrong") == true) {
                jackpot_image.setImageResource(R.drawable.red_exclamation)
            }
        } else if (activity is ConcentrationActivity) {
            if (points == null) {
                easter_egg_message.text = message
                easter_egg_price_tag_layout.visibility = View.GONE
                jackpot_layout.visibility = View.VISIBLE
                jackpot_image.setImageResource(R.drawable.concentration)
                new_gem_image.visibility = View.GONE
                easter_egg_close_button.text = "Play"
                easter_egg_exit_button.visibility = View.VISIBLE
            } else {
                easter_egg_message.text = message
                easter_egg_price_tag_layout.visibility = View.VISIBLE
                easter_egg_price.text = points.toString()
                new_gem_image.visibility = View.GONE
                easter_egg_close_button.text = "Play"
                easter_egg_exit_button.visibility = View.VISIBLE
            }
        } else if (activity is MultipleChoiceActivity) {
            if (points == null) {
                easter_egg_message.text = message
                easter_egg_price_tag_layout.visibility = View.GONE
                jackpot_layout.visibility = View.VISIBLE
                jackpot_image.setImageResource(R.drawable.quiz_icon)
                new_gem_image.visibility = View.GONE
                easter_egg_close_button.text = "Play"
                easter_egg_exit_button.visibility = View.VISIBLE
            } else {
                easter_egg_message.text = message
                easter_egg_price_tag_layout.visibility = View.VISIBLE
                easter_egg_price.text = points.toString()
                new_gem_image.visibility = View.GONE
                easter_egg_close_button.text = "Play"
                easter_egg_exit_button.visibility = View.VISIBLE
            }
        } else if (activity is HomeNavigationActivity) {
            if (points == null) {
                easter_egg_message.text = message
                easter_egg_price_tag_layout.visibility = View.GONE
                jackpot_layout.visibility = View.VISIBLE
                if (message?.toLowerCase()?.contains("something went wrong") == true) {
                    jackpot_image.setImageResource(R.drawable.red_exclamation)
                } else {
                    //change to leveling up image
                    jackpot_image.setImageResource(R.drawable.climbing)
                }
                new_gem_image.visibility = View.GONE
                easter_egg_close_button.text = "Close"
                easter_egg_exit_button.visibility = View.GONE
            } else {
                easter_egg_message.text = message
                easter_egg_price_tag_layout.visibility = View.VISIBLE
                easter_egg_price.text = points.toString()
                new_gem_image.visibility = View.GONE
                easter_egg_close_button.text = "Close"
                easter_egg_exit_button.visibility = View.GONE
            }
        } else if (activity is SpeechGameActivity) {
            if (points == null) {
                easter_egg_message.text = message
                easter_egg_price_tag_layout.visibility = View.GONE
                jackpot_layout.visibility = View.VISIBLE
                jackpot_image.setImageResource(R.drawable.trophy)
                new_gem_image.visibility = View.GONE
                easter_egg_close_button.text = "Continue Story"
                easter_egg_exit_button.visibility = View.VISIBLE
                easter_egg_exit_button.text = "Play Again"
            } else {
                easter_egg_message.text = message
                easter_egg_price_tag_layout.visibility = View.VISIBLE
                easter_egg_price.text = points.toString()
                new_gem_image.visibility = View.GONE
                easter_egg_close_button.text = "Play"
                easter_egg_exit_button.visibility = View.VISIBLE
            }
        } else if (activity is ChatterActivity) {
            easter_egg_message.text = message
            /*if (points != null) {
                easter_egg_price_tag_layout.visibility = View.VISIBLE
                new_gem_image.visibility = View.GONE
                new_bots_image.visibility = View.GONE
                start_game_image.visibility = View.GONE
                easter_egg_price.text = points.toString()
            } else */
            easter_egg_price_tag_layout.visibility = View.GONE
            new_gem_image.visibility = View.GONE
            new_bots_image.visibility = View.GONE
            start_game_image.visibility = View.GONE
            imageSrc?.let {
                if (context != null) {
                    Glide.with(context as Context)
                        .load(it)
                        .into(jackpot_image)
                }
            }
            jackpot_layout.visibility = View.VISIBLE
            if (message?.toLowerCase()?.contains("something went wrong") == true) {
                jackpot_image.setImageResource(R.drawable.red_exclamation)
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
        fun newInstance(message: String, points: Long?, imageSrc: String): EasterEggFragment {
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

        fun newInstance(message: String, points: Long): EasterEggFragment {
            val fragment = EasterEggFragment()
            fragment.message = message
            fragment.points = points
            return fragment
        }
    }
}
