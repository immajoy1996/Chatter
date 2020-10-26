package com.example.chatter.ui.activity

import ProgressBarAnimation
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.chatter.R
import com.example.chatter.data.BotStoryModel
import com.example.chatter.ui.BotStoryFragmentUsed
import com.example.chatter.ui.activity.ChatterActivity.Companion.BOT_TITLE
import com.example.chatter.ui.activity.ChatterActivity.Companion.IMAGE_PATH
import com.example.chatter.ui.activity.ChatterActivity.Companion.SHOULD_SHOW_STORY
import com.example.chatter.ui.fragment.BaseFragment
import com.example.chatter.ui.fragment.BeginYourJourneyFragment
import com.example.chatter.ui.fragment.BotStoryOptionsFragment
import com.example.chatter.ui.fragment.LoadingAnimatedFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_bot_story.*
import kotlinx.android.synthetic.main.activity_chatter.*
import kotlinx.android.synthetic.main.activity_sign_in.*
import kotlinx.android.synthetic.main.bot_story_toolbar.*
import kotlinx.android.synthetic.main.bottom_nav_bar.*
import kotlinx.android.synthetic.main.bottom_nav_bar.button_back
import kotlinx.android.synthetic.main.fragment_bot_story_layout.*
import kotlinx.android.synthetic.main.fragment_story_board_two.*

class BotStoryActivity : BaseChatActivity() {
    private var fragmentArray = arrayListOf<BotStoryModel>()
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private var botTitle: String? = null
    private var currentIndex = 0
    private var imagePath = ""

    private var loadingAnimatedFragment = LoadingAnimatedFragment()
    private var optionsMenu = BotStoryOptionsFragment()
    private var gameType = ""
    private var beginYourJourneyFragment = BeginYourJourneyFragment()
    private var loadBeginYourJourney = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bot_story)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
        getBotTitle()
        hideToolbar()
        setUpTopBarButtons()
        setUpMenuOptionsDismiss()
        hideBottomBar()
        fetchAndShowStoryFragments()
    }

    fun setGameType(gameType: String) {
        this.gameType = gameType
    }

    fun setBotImagePath(path: String) {
        imagePath = path
    }

    private fun setUpTopBarButtons() {
        bot_story_menu.setOnDebouncedClickListener {
            val intent = Intent(this, HomeNavActivityUsed::class.java)
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    private fun setUpMenuOptionsDismiss() {
        bot_story_root_layout.setOnClickListener {
            if (optionsMenu.isVisible) {
                dismissMenuOptionsPopup()
            }
        }
    }

    fun dismissMenuOptionsPopup() {
        supportFragmentManager.popBackStack()
    }

    fun loadNextStoryFragment() {
        if (currentIndex + 1 < fragmentArray.size) {
            removeStoryFragment()
            loadFragmentMoveLeftOut(fragmentArray[currentIndex + 1].fragment)
            incrementCurrentIndex()
        } else {
            val intent = Intent(this, ChatterActivity::class.java)
            intent.putExtra(SHOULD_SHOW_STORY, false)
            botTitle?.let {
                intent.putExtra(BOT_TITLE, it)
            }
            if (imagePath.isNotEmpty()) {
                intent.putExtra(IMAGE_PATH, imagePath)
            }
            if (gameType.isNotEmpty()) {
                intent.putExtra(GAME_TYPE, gameType)
            }
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        }
    }

    fun showStoryProgressAnimation() {
        val from = bot_story_progress_bar.progress
        val to = getCurrentProgress()
        showProgressAnimation(from, to, bot_story_progress_bar)
    }

    private fun showProgressAnimation(from: Int, to: Int, progressBar: ProgressBar) {
        val anim = ProgressBarAnimation(progressBar, from.toFloat(), to.toFloat())
        anim.duration = 1000
        progressBar.startAnimation(anim)
    }

    fun loadPreviousFragment() {
        if (currentIndex - 1 >= 0) {
            removeStoryFragment()
            loadFragmentMoveRightOut(fragmentArray[currentIndex - 1].fragment)
            decrementCurrentIndex()
        } else {
            currentIndex = 0
            finish()
        }
    }

    private fun loadFirstStoryFragment() {
        if (fragmentArray.isNotEmpty()) {
            loadFragmentMoveLeftOut(fragmentArray.first().fragment)
        }
    }

    private fun incrementCurrentIndex() {
        currentIndex++
    }

    private fun decrementCurrentIndex() {
        currentIndex--
    }

    private fun loadAnimatedLoadingFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(R.anim.slide_in, R.anim.slide_out)
            .replace(bot_story_root_layout.id, fragment)
            .addToBackStack(fragment.javaClass.name)
            .commit()
    }

    fun weDontWantToSeeBeginYourJourneyScreenAgain() {
        preferences.storeCurrentBotStoryIndex(0)
    }

    fun resetBeginYourJourneyFlag() {
        loadBeginYourJourney = false
    }

    fun fetchAndShowStoryFragments() {
        if (currentIndex == 0 && preferences.getCurrentBotIndex() == -1) {
            loadFragment(beginYourJourneyFragment)
            loadBeginYourJourney = true
        } else {
            loadAnimatedLoadingFragment(loadingAnimatedFragment)
            loadBeginYourJourney = false
        }
        fetchStoryFragments()
        if (!loadBeginYourJourney) {
            setTimerTask("fetchStories", 3000) {
                setUpFirstStoryScreen()
            }
        }
    }

    fun setUpFirstStoryScreen() {
        removeLoadingAnimatedFragment()
        loadFirstStoryFragment()
        setUpBottomNavBar(0)
    }


    fun removeStoryFragment() {
        supportFragmentManager.popBackStack()
    }

    private fun removeLoadingAnimatedFragment() {
        supportFragmentManager.popBackStack()
    }

    private fun getBotTitle() {
        botTitle = intent?.getStringExtra("botStoryTitle")
        Log.d("BotStoryTitle", botTitle.toString())
    }

    fun hideBottomBar() {
        bottom_nav_bar.visibility = View.GONE
    }

    private fun fetchStoryFragments() {
        val storyListener = baseChildEventListener { dataSnapshot ->
            val cardTitle = dataSnapshot.child("cardTitle").value
            val cardText = dataSnapshot.child("cardText").value
            val image = dataSnapshot.child("cardImage").value
            val soundEffect = dataSnapshot.child("soundEffect").value
            val order = dataSnapshot.child("order").value
            val gameType = dataSnapshot.child("gameType").value
            gameType?.let {
                setGameType(it.toString())
            }
            Log.d("BotStories", "${cardTitle.toString()} ${order.toString()}")
            if (cardTitle != null && cardText != null /*&& image != null && soundEffect != null */ && order != null) {
                addToFragmentArray(
                    cardTitle.toString(),
                    cardText.toString(),
                    image.toString(),
                    soundEffect.toString(),
                    order as Long
                )
            } else {
                Log.d("Hello", "got in")
            }
        }
        botTitle?.let {
            val botStoryPathRef = database.child("BotStories/$botTitle")
            botStoryPathRef.addChildEventListener(storyListener)
        }
    }

    private fun addToFragmentArray(
        cardTitle: String,
        cardText: String,
        cardImage: String,
        soundEffect: String,
        order: Long
    ) {
        /*fragmentArray.add(
            BotStoryModel(
                order.toInt(),
                BotStoryFragmentUsed.newInstance(
                    cardTitle,
                    cardText,
                    cardImage,
                    soundEffect,
                    order.toInt()
                )
            )
        )*/
        fragmentArray.sortBy {
            it.order
        }
    }

    fun showToolbar() {
        bot_story_toolbar.visibility = View.VISIBLE
    }

    fun hideToolbar() {
        bot_story_toolbar.visibility = View.GONE
    }

    fun showBottomNavBar() {
        bottom_nav_bar.visibility = View.VISIBLE
    }

    fun hideBottomNavBar() {
        bottom_nav_bar.visibility = View.GONE
    }

    fun getCurrentProgress(): Int {
        return (100.0 * (currentIndex + 1) / fragmentArray.size).toInt()
    }

    fun setUpBottomNavButtons() {
        button_back.setOnDebouncedClickListener {
            loadPreviousFragment()
        }
        button_start.setOnDebouncedClickListener {
            //Toast.makeText(this,"next one",Toast.LENGTH_SHORT).show()
            loadNextStoryFragment()
        }
        button_next.setOnDebouncedClickListener {
            loadNextStoryFragment()
        }
    }

    fun setUpBottomNavBar(order: Int) {
        bottom_nav_bar.visibility = View.VISIBLE
        when (order) {
            1 -> {
                button_back.visibility = View.GONE
                button_next.visibility = View.GONE
                button_start.visibility = View.VISIBLE
            }
            2, 3 -> {
                button_back.visibility = View.VISIBLE
                button_next.visibility = View.VISIBLE
                button_start.visibility = View.GONE
            }
        }
    }

    private fun loadMenuOptionsFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(R.anim.slide_in, R.anim.slide_out)
            .replace(bot_story_options_root_layout.id, fragment)
            .addToBackStack(fragment.javaClass.name)
            .commit()
    }

    private fun loadFragmentMoveRightOut(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(R.anim.slide_left, R.anim.slide_right)
            .replace(bot_story_card_root_layout.id, fragment)
            .addToBackStack(fragment.javaClass.name)
            .commit()
    }

    private fun loadFragmentMoveLeftOut(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(R.anim.slide_right, R.anim.slide_left)
            .replace(bot_story_card_root_layout.id, fragment)
            .addToBackStack(fragment.javaClass.name)
            .commit()
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(R.anim.slide_in, R.anim.slide_out)
            .replace(bot_story_root_layout.id, fragment)
            .addToBackStack(fragment.javaClass.name)
            .commit()
    }

    override fun onBackPressed() {
        //not impl
    }

    override fun setUpTopBar() {
        //not impl
    }

    override fun addMessage(msg: String) {
        //TODO("Not yet implemented")
    }

    override fun initializeMessagesContainer() {
        //TODO("Not yet implemented")
    }

    override fun showFirstBotMessage() {
        //TODO("Not yet implemented")
    }

    companion object {
        const val GAME_TYPE = "game_type"
    }
}