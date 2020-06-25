package com.example.chatter

import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_dashboard.*
import kotlinx.android.synthetic.main.top_bar.*

class DashboardActivity : BaseActivity(), BotClickInterface {
    private lateinit var database: DatabaseReference
    private var titleList = ArrayList<String>()
    private var imageList = ArrayList<String>()
    private var levelList = ArrayList<String>()

    private var botPawnTitles = ArrayList<String>()
    private var botPawnImages = ArrayList<String>()
    private var botPawnGuestModeEnabled = ArrayList<Boolean>()
    private var botPawnLevelList = ArrayList<String>()

    private var botKnightTitles = ArrayList<String>()
    private var botKnightImages = ArrayList<String>()
    private var botKnightGuestModeEnabled = ArrayList<Boolean>()
    private var botKnightLevelList = ArrayList<String>()

    private var botBishopTitles = ArrayList<String>()
    private var botBishopImages = ArrayList<String>()
    private var botBishopGuestModeEnabled = ArrayList<Boolean>()
    private var botBishopLevelList = ArrayList<String>()

    private var botRookTitles = ArrayList<String>()
    private var botRookImages = ArrayList<String>()
    private var botRookGuestModeEnabled = ArrayList<Boolean>()
    private var botRookLevelList = ArrayList<String>()

    private var isEnabledInGuestMode = ArrayList<Boolean>()
    private var navigationDrawerFragment = NavigationDrawerFragment()

    private lateinit var auth: FirebaseAuth
    private lateinit var preferences: Preferences
    private var botItemSpacing: Int? = 20

    private var retrievingOptionsFragment =
        RetrievingOptionsFragment.newInstance("Loading Bots ...")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        var guestMode = intent.getBooleanExtra("GUEST_MODE", false)
        setIsGuestMode(guestMode)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
        preferences = Preferences(this)
        //botItemSpacing = getBotItemSpacing()
        setUpTopBar()
        setUpBotGridView()
        loadBots()
    }

    override fun setUpTopBar() {
        home.setOnClickListener {
            loadNavigationDrawer()
        }
        top_bar_quiz.setOnClickListener {
            startActivity(Intent(this, QuizActivity::class.java))
        }
        top_bar_title.text = "Dashboard"
        top_bar_mic.visibility = View.GONE
        top_bar_quiz.visibility = View.VISIBLE
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

    fun showLoadingProgress() {
        supportFragmentManager
            .beginTransaction()
            .replace(loading_bar.id, retrievingOptionsFragment)
            .addToBackStack(retrievingOptionsFragment.javaClass.name)
            .commit()
    }

    fun removeLoadingProgress() {
        supportFragmentManager?.popBackStack()
    }

    private fun loadBots() {
        showLoadingProgress()
        setTimerTask("loadBots", 2000, {
            retrieveUserLevelAndSetUpBots()
        })
    }

    private fun retrieveUserLevelAndSetUpBots() {
        if (!isGuestMode()) {
            val curUid = auth.currentUser?.uid
            curUid?.let {
                val levelReference =
                    database.child(NavigationDrawerFragment.USERS.plus(it)).child("level")
                var levelListener = baseValueEventListener { dataSnapshot ->
                    val curUserLevel = dataSnapshot.value.toString()
                    preferences.storeUserLevel(curUserLevel)
                    setUpBots(curUserLevel)
                }
                levelReference.addListenerForSingleValueEvent(levelListener)
            }
        } else {
            setUpBots("nothing")
        }
    }

    private fun setUpCategories() {
        //val items = arrayListOf<String>("funny", "beginner")
        //category_spinner.adapter = ArrayAdapter<String>(this, R.layout.category_item_layout, items)
    }

    private fun setUpBotGridView() {
        dashboard_recycler.layoutManager = GridLayoutManager(this, NUM_COLUMNS)

        val botAdapter = BotAdapter(this, imageList, titleList, levelList, isEnabledInGuestMode)
        dashboard_recycler.adapter = botAdapter
        botItemSpacing?.let {
            //dashboard_recycler.addItemDecoration(BotGridItemDecoration(0, 20))
        }
    }

    private fun loadNavigationDrawer() {
        supportFragmentManager
            .beginTransaction()
            .replace(dashboard_root_layout.id, navigationDrawerFragment)
            .addToBackStack(navigationDrawerFragment.javaClass.name)
            .commit()
    }

    fun loadLanguageSelectionScreen() {
        val intent = Intent(this, LanguageSelectionActivity::class.java)
        intent.putExtra("ChangingDefaultLanguage", CHANGING_DEFAULT_LANG)
        startActivity(intent)
    }

    fun loadCategoriesSelectionScreen() {
        val intent = Intent(this, CategorySelectionActivity::class.java)
        startActivity(intent)
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
    }

    private fun setUpBots(curUserLevel: String) {
        resetArrays()
        database.child("BotCatalog").addChildEventListener(baseChildEventListener {
            val botImage = it.child("botImage").value.toString()
            val botTitle = it.child("botTitle").value.toString()
            val levelEnabled = it.child("levelEnabled").value.toString()
            val isEnabledGuest = it.child("isEnabledInGuestMode").value as Boolean
            var botEnabled = true
            if (isGuestMode()) {
                botEnabled = isEnabledGuest
            } else {
                var levelHashMap = preferences.getLevelHashMap()
                var point1: Int? = levelHashMap[curUserLevel]
                var point2: Int? = levelHashMap[levelEnabled]
                if (point1 != null && point2 != null && point1 < point2) {
                    botEnabled = false
                }
            }
            handleNewBot(levelEnabled, botImage, botTitle, botEnabled)
            setBotAdapter()
            removeLoadingProgress()
        })
    }

    private fun setBotAdapter() {
        resetArrays()
        fillUpBotAdapter(botPawnImages, botPawnTitles, botPawnGuestModeEnabled, botPawnLevelList)
        fillUpBotAdapter(
            botKnightImages,
            botKnightTitles,
            botKnightGuestModeEnabled,
            botKnightLevelList
        )
        fillUpBotAdapter(
            botBishopImages,
            botBishopTitles,
            botBishopGuestModeEnabled,
            botBishopLevelList
        )
        fillUpBotAdapter(botRookImages, botRookTitles, botRookGuestModeEnabled, botRookLevelList)
        val botAdapter = BotAdapter(this, imageList, titleList, levelList, isEnabledInGuestMode)
        dashboard_recycler.adapter = botAdapter
    }

    private fun fillUpBotAdapter(
        tempImageList: ArrayList<String>,
        tempTitleList: ArrayList<String>,
        tempIsEnabledGuestList: ArrayList<Boolean>,
        tempLevelList: ArrayList<String>
    ) {
        imageList.addAll(tempImageList)
        titleList.addAll(tempTitleList)
        isEnabledInGuestMode.addAll(tempIsEnabledGuestList)
        levelList.addAll(tempLevelList)
    }

    private fun handleNewBot(
        levelEnabled: String,
        botImage: String,
        botTitle: String,
        isEnabledInGuestMode: Boolean
    ) {
        when (levelEnabled) {
            "Pawn" -> {
                botPawnImages.add(botImage)
                botPawnTitles.add(botTitle)
                botPawnGuestModeEnabled.add(isEnabledInGuestMode)
                botPawnLevelList.add(levelEnabled)
            }
            "Knight" -> {
                botKnightImages.add(botImage)
                botKnightTitles.add(botTitle)
                botKnightGuestModeEnabled.add(isEnabledInGuestMode)
                botKnightLevelList.add(levelEnabled)
            }
            "Bishop" -> {
                botBishopImages.add(botImage)
                botBishopTitles.add(botTitle)
                botBishopGuestModeEnabled.add(isEnabledInGuestMode)
                botBishopLevelList.add(levelEnabled)
            }
            "Rook" -> {
                botRookImages.add(botImage)
                botRookTitles.add(botTitle)
                botRookGuestModeEnabled.add(isEnabledInGuestMode)
                botRookLevelList.add(levelEnabled)
            }
        }
    }

    override fun onBotItemClicked(botTitle: String) {
        val intent = Intent(this, ChatterActivity::class.java)
        intent.putExtra(BOT_TITLE, botTitle)
        startActivity(intent)
    }

    companion object {
        private const val NUM_COLUMNS = 3
        private const val TOTAL_BOTS = 4
        private const val BOT_ITEM_SPACING = 40
        private const val BOT_TITLE = "BOT_TITLE"
        const val CHANGING_DEFAULT_LANG = -1
    }
}
