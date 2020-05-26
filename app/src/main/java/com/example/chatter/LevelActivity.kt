package com.example.chatter

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_level.*
import kotlinx.android.synthetic.main.levels_activity_two.*
import kotlinx.android.synthetic.main.top_bar.*


class LevelActivity : BaseActivity() {
    private var gemNames = arrayListOf<String>(
        "Amethyst",
        "Ruby",
        "Emerald",
        "Sapphire",
        "Orange Julius",
        "Diamond",
        "Yellow Fever",
        "Breast Cancer",
        "Emerald"
    )
    private var gemPrices = arrayListOf<Int>(1000, 2000, 3000, 4000, 5000, 6000, 7000, 8000, 9000)
    private var gemImages = arrayListOf<Int>(
        R.drawable.gem,
        R.drawable.ruby,
        R.drawable.emerald,
        R.drawable.sapphire,
        R.drawable.orange_gem,
        R.drawable.diamond,
        R.drawable.yellow_gem,
        R.drawable.pink_gem,
        R.drawable.emerald
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_level)
        setUpTopBar()
        setUpLevelsRecycler()
        //setUpGemsGridView()
    }

    override fun setUpTopBar() {
        top_bar_title.text = "Level"
        top_bar_mic.visibility = View.GONE
        back.visibility = View.VISIBLE
        home.visibility = View.GONE
    }

    private fun setUpLevelsRecycler() {
        levels_recycler.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = LevelsAdapter(context, gemNames, gemImages, gemPrices)
        }
    }
}
