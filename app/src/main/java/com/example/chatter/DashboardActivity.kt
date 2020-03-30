package com.example.chatter

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_dashboard.*

class DashboardActivity : AppCompatActivity(), BotClickInterface {
    private lateinit var database: DatabaseReference
    private var titleList = ArrayList<String>()
    private var imageList = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        database = FirebaseDatabase.getInstance().reference

        setUpBotGridView()
        setUpBots()
    }

    private fun setUpBotGridView() {
        dashboard_recycler.layoutManager = GridLayoutManager(this, NUM_COLUMNS)
        val botAdapter = BotAdapter(this, imageList, titleList)
        dashboard_recycler.adapter = botAdapter
        dashboard_recycler.addItemDecoration(BotGridItemDecoration(BOT_ITEM_SPACING))
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

    val createBotListener: ((DataSnapshot) -> Unit) -> ChildEventListener = { doit ->
        val messageListener = object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, p1: String?) {
                doit(dataSnapshot)
            }

            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onChildRemoved(p0: DataSnapshot) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        }
        messageListener
    }

    companion object {
        private const val NUM_COLUMNS = 3
        private const val TOTAL_BOTS = 4
        private const val BOT_ITEM_SPACING = 40
        private const val BOT_TITLE = "BOT_TITLE"
    }
}
