package com.example.chatter.ui.activity

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.example.chatter.R
import com.example.chatter.data.BotStoryModel
import com.example.chatter.ui.BotStoryFragmentUsed
import com.example.chatter.ui.fragment.BaseFragment
import com.example.chatter.ui.fragment.BotStoryIndividualFragment
import com.example.chatter.ui.fragment.LoadingAnimatedFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_bot_story_latest.*
import kotlinx.android.synthetic.main.three_dots_layout.*

class BotStoryActivityLatest : BaseActivity(
) {
    private var TAG = BotStoryActivityLatest::class.java.simpleName
    private var gameType = ""
    private var botTitle = ""
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private var fragmentArray = arrayListOf<BotStoryModel>()
    private var currentStoryCount = 0

    private var loadingFragment = LoadingAnimatedFragment()
    private var imagePath = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bot_story_latest)
        database = FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()
        initializeViews()
        //getBotTitle()
    }

    fun initializeViews() {
        hideStartChattingButton()
        //disableStartChattingButton()
        showLoadingAndFetchStories()
    }

    fun setUpImagePath(image: String) {
        imagePath = image
    }

    fun disableStartChattingButton() {
        start_chatting_button.setBackgroundResource(R.drawable.disabled_start_chatting_button)
        start_chatting_button.setTextColor(Color.parseColor("#a9a9a9"))
        start_chatting_button.isClickable = false
    }

    fun enableStartChattingButton() {
        start_chatting_button.setBackgroundResource(R.drawable.option_bubble)
        start_chatting_button.setTextColor(Color.parseColor("#ffffff"))
        start_chatting_button.isClickable = true
        start_chatting_button.setOnClickListener {
            launchChatterActivity()
        }
    }

    private fun launchChatterActivity() {
        val intent = Intent(this, ChatterActivity::class.java)
        intent.putExtra(ChatterActivity.SHOULD_SHOW_STORY, false)
        intent.putExtra(ChatterActivity.BOT_TITLE, botTitle)
        if (imagePath.isNotEmpty()) {
            intent.putExtra(ChatterActivity.IMAGE_PATH, imagePath)
        }
        if (gameType.isNotEmpty()) {
            intent.putExtra(BotStoryActivity.GAME_TYPE, gameType)
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }

    private fun loadLoadingFragment() {
        loadFragment(loadingFragment)
    }

    private fun removeLoadingFragment() {
        supportFragmentManager.popBackStack()
    }

    private fun resetStoryCount() {
        currentStoryCount = 0
        fragmentArray.clear()
    }

    private fun getBotTitle() {
        //botTitle = intent.getStringExtra("botStoryTitle") ?: ""
        Log.d(TAG, botTitle.toString())
    }

    private fun setGameType(type: String) {
        gameType = type
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            //.setCustomAnimations(R.anim.slide_in, R.anim.slide_out)
            .replace(bot_story_latest_root_layout.id, fragment)
            .addToBackStack(fragment.javaClass.name)
            .commit()
    }

    private fun hideStartChattingButton() {
        start_chatting_button.visibility = View.GONE
    }

    private fun showStartChattingButton() {
        start_chatting_button.visibility = View.VISIBLE
    }

    private fun showLoadingAndFetchStories() {
        //loadLoadingFragment()
        hideStartChattingButton()
        //setTimerTask("fetchStories", 2000, {
        fetchStories()
        //})
    }

    private fun fetchStories() {
        resetStoryCount()
        val storyPath = preferences.getCurrentStoryPath()
        Log.d("My Path", storyPath)
        val storyCountListener = baseValueEventListener { dataSnapshot ->
            val storyCount = dataSnapshot.value as Long?
            storyCount?.let {
                val botStoryPathRef = database.child(storyPath)
                botStoryPathRef.addChildEventListener(getStoryEventListener(it.toInt()))
            }
        }
        val storyCountRef = database.child(storyPath).child("contextCount")
        storyCountRef.addValueEventListener(storyCountListener)
    }

    private fun getStoryEventListener(storyCount: Int): ChildEventListener {
        val storyListener = baseChildEventListener { dataSnapshot ->
            val cardTitle = dataSnapshot.child("cardTitle").value
            val cardText = dataSnapshot.child("cardText").value
            val image = dataSnapshot.child("cardImage").value
            //val soundEffect = dataSnapshot.child("soundEffect").value
            val order = dataSnapshot.child("order").value
            //val gameType = dataSnapshot.child("gameType").value
            /*gameType?.let {
                setGameType(it.toString())
            }*/
            if(cardTitle!=null) {
                Log.d("My path", cardTitle.toString())
            }else{
                Log.d("My path", "sorry its null")
            }
            
            if (cardTitle != null && cardText != null /*&& image != null && soundEffect != null */ && order != null && currentStoryCount < storyCount - 1) {
                addToFragmentArray(
                    cardTitle.toString(),
                    cardText.toString(),
                    image.toString(),
                    order as Long,
                    storyCount
                )
                currentStoryCount++
            } else if (currentStoryCount == storyCount - 1) {
                currentStoryCount++
                Log.d("Blah blah", "" + currentStoryCount + " " +  cardTitle+" "+order)
                addToFragmentArray(
                    cardTitle.toString(),
                    cardText.toString(),
                    image.toString(),
                    order as Long,
                    storyCount
                )
                sortFragmentArray()
                val viewPager = createViewPager()
                viewPager?.let {
                    showStartChattingButton()
                    setUpViewPager(it)
                }
                hideUnnecessaryPins(storyCount)
                setUpPageChangeListener()
                //removeLoadingFragment()
            } else {
                //Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show()
                //Log.d(TAG, "something went wrong")
            }
        }
        return storyListener
    }

    private fun sortFragmentArray() {
        fragmentArray.sortBy {
            it.order
        }
    }

    private fun setUpPageChangeListener() {
        bot_story_latest_view_pager.addOnPageChangeListener(object :
            ViewPager.OnPageChangeListener {
            override fun onPageSelected(position: Int) {
                if (position == fragmentArray.size - 1) {
                    enableStartChattingButton()
                } else {
                    disableStartChattingButton()
                }
                setUpPinColors(position + 1)
            }

            override fun onPageScrollStateChanged(state: Int) {
                //TODO("Not yet implemented")
            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                //TODO("Not yet implemented")
            }
        })
    }

    private fun addToFragmentArray(
        cardTitle: String,
        cardText: String,
        cardImage: String,
        order: Long,
        storyCount: Int
    ) {
        fragmentArray.add(
            BotStoryModel(
                order.toInt(),
                BotStoryIndividualFragment.newInstance(
                    cardTitle,
                    cardText,
                    cardImage,
                    order.toInt(),
                    (order.toInt() == storyCount)
                )
            )
        )
    }

    private fun hideUnnecessaryPins(storyCount: Int) {
        when (storyCount) {
            1 -> {
                second_dot.visibility = View.GONE
                third_dot.visibility = View.GONE
                fourth_dot.visibility = View.GONE
            }
            2 -> {
                third_dot.visibility = View.GONE
                fourth_dot.visibility = View.GONE
            }
            3 -> {
                fourth_dot.visibility = View.GONE
            }
        }
    }

    private fun getPinView(position: Int): TextView {
        return when (position) {
            1 -> {
                findViewById<TextView>(R.id.first_dot)
            }
            2 -> {
                findViewById<TextView>(R.id.second_dot)
            }
            3 -> {
                findViewById<TextView>(R.id.third_dot)
            }
            else -> {
                findViewById<TextView>(R.id.fourth_dot)
            }
        }
    }

    fun setUpPinColors(position: Int) {
        for (i in 1 until 5) {
            val pinView = getPinView(i)
            if (i == position) {
                pinView.setBackgroundResource(R.drawable.dots_background_filled)
            } else {
                pinView.setBackgroundResource(R.drawable.dots_background)
            }
        }
    }

    private fun setUpViewPager(pagerAdapter: FragmentStatePagerAdapter) {
        bot_story_latest_view_pager.adapter = pagerAdapter
    }

    private fun createViewPager(): FragmentStatePagerAdapter? {
        class PagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {

            override fun getCount(): Int {
                return fragmentArray.size
            }

            override fun getItem(position: Int): BaseFragment {
                return fragmentArray[position].fragment
            }

        }
        if (fragmentArray.isNotEmpty()) {
            return PagerAdapter(supportFragmentManager)
        } else {
            return null
        }
    }

    override fun setUpTopBar() {
        //TODO("Not yet implemented")
    }
}