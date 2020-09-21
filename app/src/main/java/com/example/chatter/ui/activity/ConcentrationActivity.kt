package com.example.chatter.ui.activity

import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.example.chatter.adapters.ConcentrationAdapter
import com.example.chatter.R
import com.example.chatter.data.ConcentrationTime
import com.example.chatter.interfaces.RevealItemInterface
import com.example.chatter.ui.fragment.EasterEggFragment
import com.google.api.client.util.DateTime
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_concentration.*
import kotlinx.android.synthetic.main.activity_dashboard.*
import kotlinx.android.synthetic.main.top_bar.*
import java.time.DateTimeException
import java.time.format.DateTimeFormatter
import java.util.*

class ConcentrationActivity : BaseActivity(),
    RevealItemInterface {
    var imageList = arrayListOf<String>()
    var concentrationGameImages = arrayListOf<String>()
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private var gameStartFragment = EasterEggFragment.newInstance("Timer starts when you click Play")
    private var timer = Timer()
    private var currentTime = ConcentrationTime(0, 0)
    private var mediaPlayer = MediaPlayer()
    private var gameStarted = false
    private var enableMusic = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        database = FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()
        setContentView(R.layout.activity_concentration)
        setUpTopBar()
        setUpProfileGridView()
        fetchBotItemImages()
        initializeCurrentTime()
        loadStartGameFragment()
    }

    override fun revealItem() {
        setTimerTask("revealCardItem", 250) {
            (concentration_recycler.adapter as? ConcentrationAdapter)?.resetSelectedPositions()
            concentration_recycler.adapter?.notifyDataSetChanged()
        }
    }

    private fun loadStartGameFragment() {
        supportFragmentManager
            .beginTransaction()
            .replace(concentration_root_layout.id, gameStartFragment)
            .addToBackStack(gameStartFragment.javaClass.name)
            .commit()
    }

    private fun loadFragment(easterEggFragment: EasterEggFragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(concentration_root_layout.id, easterEggFragment)
            .addToBackStack(easterEggFragment.javaClass.name)
            .commit()
    }

    fun removeStartGameFragment() {
        supportFragmentManager.popBackStack()
    }

    private fun playMedia(audio: String) {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop()
            mediaPlayer.release()
        }
        mediaPlayer = MediaPlayer.create(this, R.raw.game_music)
        mediaPlayer.isLooping = true
        mediaPlayer.start()
    }

    private fun playNotificationSound() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop()
            mediaPlayer.release()
        }
        mediaPlayer = MediaPlayer.create(this, R.raw.notification)
        mediaPlayer.start()
    }

    private fun initializeCurrentTime(){
        currentTime = ConcentrationTime(0, 0)
    }

    fun resumeGame(){
        gameStarted = true
        timer = Timer()
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                currentTime.addOneSecond()
                runOnUiThread {
                    concentration_game_timer.text = currentTime.getFormattedTime()
                }
            }
        }, 0, 1000)
    }

    fun startGame() {
        gameStarted = true
        timer = Timer()
        initializeCurrentTime()
        fetchBotItemImages()
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                currentTime.addOneSecond()
                runOnUiThread {
                    concentration_game_timer.text = currentTime.getFormattedTime()
                }
            }
        }, 0, 1000)
    }

    private fun fetchBotItemImages() {
        imageList = intent.getStringArrayListExtra("botImages") ?: arrayListOf()
        if (imageList.size < BOT_COUNT) {
            while (imageList.size < BOT_COUNT) {
                imageList.add(GIFT_IMAGE)
            }
        }
        Collections.shuffle(imageList)
        concentrationGameImages.clear()
        for (index in 0 until BOT_COUNT) {
            concentrationGameImages.add(imageList[index])
            concentrationGameImages.add(imageList[index])
        }
        concentrationGameImages.add(GIFT_IMAGE)
        Collections.shuffle(concentrationGameImages)
        concentration_recycler.adapter =
            ConcentrationAdapter(
                this,
                concentrationGameImages,
                this
            )
    }

    private fun setUpProfileGridView() {
        concentration_recycler.layoutManager = GridLayoutManager(this, 3)
    }

    override fun setUpTopBar() {
        back.visibility = View.VISIBLE
        home.visibility = View.GONE
        top_bar_mic.visibility = View.GONE
        top_bar_title.text = "Play!"
        top_bar_title.visibility = View.GONE
        concentration_game_timer.visibility = View.VISIBLE
        score_to_beat.visibility = View.VISIBLE
        //top_bar_music_concentration.visibility = View.VISIBLE
        top_bar_music_enabled_concentration.visibility = View.VISIBLE
        top_bar_music_disabled_concentration.visibility = View.GONE
        top_bar_music_enabled_concentration.setOnClickListener {
            top_bar_music_enabled_concentration.visibility = View.GONE
            top_bar_music_disabled_concentration.visibility = View.VISIBLE
            enableMusic = false
            disableMusic()
        }
        top_bar_music_disabled_concentration.setOnClickListener {
            top_bar_music_disabled_concentration.visibility = View.GONE
            top_bar_music_enabled_concentration.visibility = View.VISIBLE
            enableMusic = true
            playMedia(GAME_BACKGROUND_MUSIC)
        }
        back.setOnClickListener {
            finish()
        }
    }

    private fun disableMusic() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
        }
        mediaPlayer.release()
        mediaPlayer = MediaPlayer()
    }

    override fun onResume() {
        super.onResume()
        if (gameStarted) {
            val resumeGameFragment = EasterEggFragment.newInstance("Resume Game?")
            loadFragment(resumeGameFragment)
        }
        if (enableMusic) {
            //playMedia(GAME_BACKGROUND_MUSIC)
        }
    }

    override fun onPause() {
        super.onPause()
        timer.cancel()
        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        timer.cancel()
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
        }
        mediaPlayer.release()
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

    override fun endGame() {
        timer.cancel()
        val newScore = concentration_game_timer.text.toString()
        val newScoreFragment =
            EasterEggFragment.newInstance("You've set a new high score!", 25L)
        val thanksForPlayingFragment =
            EasterEggFragment.newInstance("Better luck next time!")
        val betterThanOneMinuteFragment =
            EasterEggFragment.newInstance("Congrats! You've won.", 20L)
        if (newScore.isBetterTimeThan(RESPECTABLE_TIME)) {
            playNotificationSound()
            loadFragment(betterThanOneMinuteFragment)
        } else {
            playNotificationSound()
            loadFragment(thanksForPlayingFragment)
        }
    }

    fun String.isBetterTimeThan(otherTime: String): Boolean {
        val h1 = this.split(":")[0].toInt()
        val m1 = this.split(":")[1].toInt()
        val h2 = otherTime.split(":")[0].toInt()
        val m2 = otherTime.split(":")[1].toInt()
        if (h1 == h2) {
            if (m1 == m2) {
                return false
            } else {
                return m1 < m2
            }
        } else {
            return h1 < h2
        }
    }

    companion object {
        const val GAME_BACKGROUND_MUSIC =
            "https://firebasestorage.googleapis.com/v0/b/chatter-f7ae2.appspot.com/o/vocabAudio%2FCollege%20Dropout.mp3?alt=media&token=17d3eaf3-7665-479c-89d9-ebd00c3d9929"
        const val GIFT_IMAGE =
            "https://firebasestorage.googleapis.com/v0/b/chatter-f7ae2.appspot.com/o/botImages%2Fgift.png?alt=media&token=42c1890c-db5e-45eb-a296-871e271d27cc"
        const val RESPECTABLE_TIME = "0:30"
        const val BOT_COUNT = 7
    }
}