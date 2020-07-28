package com.example.chatter.ui.activity

import android.content.Intent
import android.content.res.Resources
import android.media.MediaPlayer
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.chatter.*
import com.example.chatter.adapters.BotAdapter
import com.example.chatter.adapters.BotGridItemDecoration
import com.example.chatter.extra.Preferences
import com.example.chatter.interfaces.BotClickInterface
import com.example.chatter.ui.fragment.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_dashboard.*
import kotlinx.android.synthetic.main.activity_quiz.*
import kotlinx.android.synthetic.main.activity_sign_in.*
import kotlinx.android.synthetic.main.top_bar.*

class DashboardActivity : BaseActivity(),
    BotClickInterface {
    private lateinit var database: DatabaseReference
    private var titleList = ArrayList<String>()
    private var imageList = ArrayList<String>()
    private var categoryList = ArrayList<String>()
    private var levelList = ArrayList<String>()
    private var isEnabledInGuestMode = ArrayList<Boolean>()
    private var totalEnabled = -1
    private var countEnabled = -1
    private var newBotsAcquiredMessageShown = false
    private var changedProfile: Boolean? = null

    private lateinit var auth: FirebaseAuth
    private var botItemSpacing: Int? = 20

    private var retrievingOptionsFragment =
        RetrievingOptionsFragment.newInstance("Loading Bots ...")

    private var targetLanguage = ""
    private var targetBotCategory = "All Bots"

    private var navigationDrawerFragment =
        NavigationDrawerFragment.newInstance(
            "",
            "All Bots"
        )

    private var newBotsAquiredFragment =
        EasterEggFragment.newInstance("You've acquired new chat bots!")
    private var loadingAnimatedFragment = LoadingAnimatedFragment.newInstance("Loading ...")
    private var mediaPlayer = MediaPlayer()
    private var enableMusic = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        val guestMode = intent.getBooleanExtra("GUEST_MODE", false)
        setIsGuestMode(guestMode)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
        setUpTopBar()
    }

    override fun setUpTopBar() {
        home.visibility = View.GONE
        back.visibility = View.VISIBLE
        top_bar_categories.visibility = View.VISIBLE
        back.setOnClickListener {
            finish()
        }
        top_bar_categories.setOnClickListener {
            loadCategoriesSelectionScreen()
        }
        home.setOnClickListener {
            loadNavigationDrawer()
        }
        top_bar_jokebook.visibility = View.GONE
        top_bar_quiz.visibility = View.GONE
        top_bar_title.visibility = View.VISIBLE
        top_bar_messaging_image_container.visibility = View.GONE
        home.setOnLongClickListener {
            if (top_bar_plus_button.visibility == View.VISIBLE) {
                top_bar_plus_button.visibility = View.GONE
            } else {
                top_bar_plus_button.visibility = View.VISIBLE
            }
            true
        }
        top_bar_plus_button.setOnClickListener {
            startActivity(Intent(this, CreateBotActivity::class.java))
        }
        top_bar_quiz.setOnClickListener {
            val intent = Intent(this, QuizActivity::class.java)
            intent.putStringArrayListExtra("botImages", imageList)
            startActivity(intent)
        }
        top_bar_title.text = "Bots"
        top_bar_mic.visibility = View.GONE

        //top_bar_music_dashboard.visibility = View.VISIBLE
        //top_bar_music_enabled.visibility = View.VISIBLE
        //top_bar_music_disabled.visibility = View.GONE
        /*top_bar_music_enabled.setOnClickListener {
            top_bar_music_enabled.visibility = View.GONE
            top_bar_music_disabled.visibility = View.VISIBLE
            enableMusic = false
            disableMusic()
        }*/
        top_bar_jokebook.setOnDebouncedClickListener {
            loadFragment(QuizDescriptionFragment.newInstance(false))
        }
        /*top_bar_music_disabled.setOnClickListener {
            top_bar_music_disabled.visibility = View.GONE
            top_bar_music_enabled.visibility = View.VISIBLE
            enableMusic = true
            playMedia(ConcentrationActivity.GAME_BACKGROUND_MUSIC)
        }*/
    }

    fun loadFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(dashboard_root_layout.id, fragment)
            .addToBackStack(fragment.javaClass.name)
            .commit()
    }

    private fun disableMusic() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
        }
        mediaPlayer.release()
        mediaPlayer = MediaPlayer()
    }


    private fun getBotItemSpacing(): Int {
        val displayMetrics = DisplayMetrics();
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val height = displayMetrics.heightPixels
        val width = displayMetrics.widthPixels
        val spacingInPx = (width - dpToPx(125 * 3)) / 4
        return pxToDp(spacingInPx)
    }

    fun dpToPx(dp: Int): Int {
        return (dp * Resources.getSystem().getDisplayMetrics().density).toInt()
    }

    fun pxToDp(px: Int): Int {
        return (px / Resources.getSystem().getDisplayMetrics().density).toInt()
    }

    private fun playMedia(audio: String) {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop()
            mediaPlayer.release()
        }
        mediaPlayer = MediaPlayer.create(this, R.raw.game_music)
        mediaPlayer.isLooping = true
        //mediaPlayer.start()
    }

    private fun showLoadingProgress() {
        supportFragmentManager
            .beginTransaction()
            .replace(loading_bar.id, retrievingOptionsFragment)
            .addToBackStack(retrievingOptionsFragment.javaClass.name)
            .commit()
    }

    private fun removeLoadingProgress() {
        supportFragmentManager.popBackStack()
    }

    private fun removeLoadingAnimatedFragment() {
        supportFragmentManager.popBackStack()
    }

    private fun loadAnimatedLoadingFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(dashboard_inner_container.id, fragment)
            .addToBackStack(fragment.javaClass.name)
            .commit()
    }

    private fun loadBots() {
        //showLoadingProgress()
        loadAnimatedLoadingFragment(loadingAnimatedFragment)
        setTimerTask("loadBots", 2000, {
            setUpBots()
        })
    }

    private fun setUpBotGridView() {
        resetArrays()
        dashboard_recycler.layoutManager = GridLayoutManager(
            this,
            NUM_COLUMNS
        )

        val botAdapter = BotAdapter(
            this,
            imageList,
            titleList,
            isEnabledInGuestMode
        )
        dashboard_recycler.adapter = botAdapter
        /*botItemSpacing?.let {
            dashboard_recycler.addItemDecoration(BotGridItemDecoration(0, 20))
        }*/
    }

    private fun loadNewBotsAquiredFragment() {
        playNotificationSound()
        supportFragmentManager
            .beginTransaction()
            .replace(dashboard_root_layout.id, newBotsAquiredFragment)
            .addToBackStack(newBotsAquiredFragment.javaClass.name)
            .commit()
    }

    fun removeNewBotsAquiredFragment() {
        supportFragmentManager.popBackStack()
    }

    private fun loadNavigationDrawer() {
        navigationDrawerFragment =
            NavigationDrawerFragment.newInstance(
                targetLanguage,
                targetBotCategory
            )
        supportFragmentManager
            .beginTransaction()
            .replace(dashboard_root_layout.id, navigationDrawerFragment)
            .addToBackStack(navigationDrawerFragment.javaClass.name)
            .commit()
    }

    fun loadLanguageSelectionScreen() {
        val intent = Intent(this, LanguageSelectionActivity::class.java)
        intent.putExtra(
            "ChangingDefaultLanguage",
            CHANGING_DEFAULT_LANG
        )
        startActivity(intent)
    }

    fun loadCategoriesSelectionScreen() {
        val intent = Intent(this, CategorySelectionActivity::class.java)
        startActivityForResult(
            intent,
            CATEGORY_REQUEST_CODE
        )
    }

    fun isGuestMode(): Boolean {
        return guestMode
    }

    private fun setIsGuestMode(flag: Boolean) {
        guestMode = flag
    }

    private fun resetArrays() {
        titleList.clear()
        imageList.clear()
        isEnabledInGuestMode.clear()
        levelList.clear()
        categoryList.clear()
    }

    private fun setUpBots() {
        resetArrays()
        database.child("BotCatalog").addChildEventListener(baseChildEventListener {
            val botImage = it.child("botImage").value.toString()
            val botTitle = it.child("botTitle").value.toString()
            val botCategory = it.child("category").value.toString()
            val isEnabledGuest = it.child("isEnabledInGuestMode").value as Boolean
            /*if (retrievingOptionsFragment.isVisible) {
                removeLoadingProgress()
            }*/
            if (loadingAnimatedFragment.isVisible) {
                removeLoadingAnimatedFragment()
            }
            if (auth.currentUser != null) {
                val pointsNeeded = it.child("pointsNeeded").value as Long?
                auth.currentUser?.uid?.let {
                    val pointsRef = database.child(USERS.plus(it)).child("points")
                    val pointsListener = baseValueEventListener { dataSnapshot ->
                        val userPoints = dataSnapshot.value as Long
                        var botEnabled = true
                        pointsNeeded?.let {
                            if (it > userPoints) {
                                botEnabled = false
                            }
                        }
                        handleNewBot(botImage, botTitle, botCategory, botEnabled)
                        setBotAdapter()
                        if (countEnabled > totalEnabled && totalEnabled != -1 && !newBotsAcquiredMessageShown) {
                            loadNewBotsAquiredFragment()
                            newBotsAcquiredMessageShown = true
                        }
                        preferences.storeCountEnabledBots(countEnabled)
                    }
                    pointsRef.addListenerForSingleValueEvent(pointsListener)
                }
            } else {
                val botEnabled = isEnabledGuest
                handleNewBot(botImage, botTitle, botCategory, botEnabled)
                setBotAdapter()
                removeLoadingAnimatedFragment()
            }
        })
    }

    private fun setBotAdapter() {
        val botAdapter = BotAdapter(
            this,
            imageList,
            titleList,
            isEnabledInGuestMode
        )
        dashboard_recycler.adapter = botAdapter
    }

    private fun handleNewBot(
        botImage: String,
        botTitle: String,
        botCategory: String,
        botEnabled: Boolean
    ) {
        if (botEnabled) {
            if (countEnabled == -1) countEnabled = 0
            countEnabled++
            imageList.add(0, botImage)
            titleList.add(0, botTitle)
            categoryList.add(0, botCategory)
            isEnabledInGuestMode.add(0, botEnabled)
        } else {
            imageList.add(botImage)
            titleList.add(botTitle)
            categoryList.add(botCategory)
            isEnabledInGuestMode.add(botEnabled)
        }
    }

    override fun onBotItemClicked(imagePath: String, botTitle: String) {
        val intent = Intent(this, ChatterActivity::class.java)
        intent.putExtra(TARGET_LANGUAGE, targetLanguage)
        intent.putExtra(BOT_TITLE, botTitle)
        intent.putExtra(IMAGE_PATH, imagePath)
        startActivity(intent)
    }

    private fun playNotificationSound() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop()
            mediaPlayer.release()
        }
        mediaPlayer = MediaPlayer.create(this, R.raw.notification)
        mediaPlayer.start()
    }

    override fun onResume() {
        super.onResume()
        if (enableMusic) {
            playMedia("dfd")
        }
        countEnabled = 0
        totalEnabled = preferences.getEnabledBotCount()
        newBotsAcquiredMessageShown = false
        if (!navigationDrawerFragment.isVisible && targetBotCategory == "All Bots") {
            setUpBotGridView()
            loadBots()
        }
        if (top_bar_plus_button.visibility == View.VISIBLE) {
            top_bar_plus_button.visibility = View.GONE
        }
        if (auth.currentUser != null) {
            auth.currentUser?.uid?.let {
                val uid = it
                val languagePathRef = database.child("Users/${uid}").child("nativeLanguage")
                val langListener = baseValueEventListener { dataSnapshot ->
                    targetLanguage = dataSnapshot.value.toString()
                    if (navigationDrawerFragment.isVisible) {
                        navigationDrawerFragment.setUpLanguageTextField(targetLanguage)
                    }
                }
                languagePathRef.addListenerForSingleValueEvent(langListener)

                val profilePathRef = database.child("Users/${uid}").child("profileImage")
                val profileListener = baseValueEventListener { dataSnapshot ->
                    val targetProfile = dataSnapshot.value.toString()
                    if (navigationDrawerFragment.isVisible) {
                        navigationDrawerFragment.setUpProfileImage(targetProfile)
                    }
                }
                profilePathRef.addListenerForSingleValueEvent(profileListener)
            }
        } else {
            targetLanguage = preferences.getCurrentTargetLanguage()
            val targetProfileImage = preferences.getProfileImage()
            if (navigationDrawerFragment.isVisible) {
                navigationDrawerFragment.setUpLanguageTextField(targetLanguage)
                navigationDrawerFragment.setUpProfileImage(targetProfileImage)
            }
        }
    }

    override fun onBackPressed() {
        //not implemented
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            CATEGORY_REQUEST_CODE -> {
                val selectedCategory = data?.getStringExtra("SelectedCategory")
                selectedCategory?.let {
                    showBotsBasedOnCategory(it)
                    targetBotCategory = it
                    /*if (navigationDrawerFragment.isVisible) {
                        navigationDrawerFragment.setUpBotCategoryTextField(it)
                    }*/
                }
            }
        }
    }

    private fun showBotsBasedOnCategory(category: String) {
        val newTitleList = arrayListOf<String>()
        val newImageList = arrayListOf<String>()
        val newIsEnabledGuestList = arrayListOf<Boolean>()
        for (index in 0 until categoryList.size) {
            if (categoryList[index] == category || category == "All Bots") {
                newTitleList.add(titleList[index])
                newImageList.add(imageList[index])
                newIsEnabledGuestList.add(isEnabledInGuestMode[index])
            }
        }
        val botAdapter =
            BotAdapter(
                this,
                newImageList,
                newTitleList,
                newIsEnabledGuestList
            )
        dashboard_recycler.adapter = botAdapter
    }

    override fun onPause() {
        super.onPause()
        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
        }
        mediaPlayer.release()
    }

    companion object {
        private const val USERS = "Users/"
        private const val NUM_COLUMNS = 2
        private const val TOTAL_BOTS = 4
        private const val BOT_ITEM_SPACING = 40
        private const val BOT_TITLE = "BOT_TITLE"
        private const val IMAGE_PATH = "IMAGE_PATH"
        const val TARGET_LANGUAGE = "Target_Language"
        const val CHANGING_DEFAULT_LANG = -1
        const val CATEGORY_REQUEST_CODE = 10
        const val PROFILE_REQUEST_CODE = 25
        private const val GAME_BACKGROUND_MUSIC =
            "https://firebasestorage.googleapis.com/v0/b/chatter-f7ae2.appspot.com/o/vocabAudio%2FCollege%20Dropout.mp3?alt=media&token=17d3eaf3-7665-479c-89d9-ebd00c3d9929"
    }
}
