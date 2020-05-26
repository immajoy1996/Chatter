package com.example.chatter

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.android.synthetic.main.levels_activity_two.*
import kotlinx.android.synthetic.main.top_bar.*

class MyStashActivity : BaseActivity() {

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
    private var haveGems = arrayListOf<Boolean>(
        true,
        true,
        false,
        false,
        false,
        false,
        false,
        false,
        false
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.levels_activity_two)
        setUpTopBar()
        setUpGemsGridView()
    }

    override fun setUpTopBar() {
        top_bar_title.text = "Achievement"
        top_bar_mic.visibility = View.GONE
        back.visibility = View.VISIBLE
        home.visibility = View.GONE
    }

    private fun setUpGemsGridView() {
        gems_recycler.apply {
            layoutManager = GridLayoutManager(context, 3)
            val gemAdapter =
                GemsGridAdapter(context, gemImages, haveGems)
            adapter = gemAdapter
        }
    }
}
