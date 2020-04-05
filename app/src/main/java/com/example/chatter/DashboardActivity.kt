package com.example.chatter

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_dashboard.*
import kotlinx.android.synthetic.main.top_bar.*

class DashboardActivity : BaseActivity(), BotClickInterface {
    private lateinit var database: DatabaseReference
    private var titleList = ArrayList<String>()
    private var imageList = ArrayList<String>()
    private var navigationDrawerFragment = NavigationDrawerFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        database = FirebaseDatabase.getInstance().reference
        setUpTopBar()
        setUpBotGridView()
        setUpBots()
    }

    override fun setUpTopBar() {
        home.setOnClickListener {
            loadNavigationDrawer()
        }
        egg_image.visibility = View.INVISIBLE
        easter_egg_count.visibility = View.INVISIBLE
    }

    private fun setUpBotGridView() {
        dashboard_recycler.layoutManager = GridLayoutManager(this, NUM_COLUMNS)
        val botAdapter = BotAdapter(this, imageList, titleList)
        dashboard_recycler.adapter = botAdapter
        dashboard_recycler.addItemDecoration(BotGridItemDecoration(BOT_ITEM_SPACING))
    }

    private fun loadNavigationDrawer() {
        supportFragmentManager
            .beginTransaction()
            .replace(dashboard_root_layout.id, navigationDrawerFragment)
            .addToBackStack(navigationDrawerFragment.javaClass.name)
            .commit()
    }

    private fun setUpBots() {
        database.child("BotCatalog").addChildEventListener(createBotListener {
            val botImage = it.child("botImage").value.toString()
            val botTitle = it.child("botTitle").value.toString()
            imageList.add(botImage)
            titleList.add(botTitle)

            val botAdapter = BotAdapter(this, imageList, titleList)
            dashboard_recycler.adapter = botAdapter
            dashboard_recycler?.adapter?.notifyDataSetChanged()
        })
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
    }
}
