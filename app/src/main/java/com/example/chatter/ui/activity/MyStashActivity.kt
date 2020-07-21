package com.example.chatter.ui.activity

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.example.chatter.R
import com.example.chatter.adapters.GemsGridAdapter
import com.example.chatter.interfaces.GemClickInterface
import kotlinx.android.synthetic.main.levels_activity_two.*
import kotlinx.android.synthetic.main.top_bar.*

class MyStashActivity : BaseActivity(),
    GemClickInterface {

    private var gemNames = arrayListOf<String>(
        "Amethyst",
        "Ruby",
        "Emerald",
        "Sapphire",
        "Orange Julius",
        "Diamond",
        "Yellow Fever",
        "Tropic of Cancer",
        "Emerald"
    )

    private var gemPrices = arrayListOf<Int>(
        1000,
        2000,
        3000,
        4000,
        5000,
        6000,
        7000,
        8000,
        9000
    )

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
        false,
        false,
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
        setUpHaveGemsArray()
        setUpTopBar()
        setUpGemsGridView()
    }

    override fun setUpTopBar() {
        top_bar_title.text = "Achievements"
        top_bar_mic.visibility = View.GONE
        back.visibility = View.VISIBLE
        home.visibility = View.GONE
        back.setOnClickListener {
            finish()
        }
    }

    private fun setUpHaveGemsArray() {
        val targetId = intent.getIntExtra("gem_image", -100)
        val points = intent.getIntExtra("points", 0)
        total_coins.setText(points.toString())
        Toast.makeText(this, "" + targetId, Toast.LENGTH_LONG).show()
        var found = false
        var index = 0
        for (gemId in gemImages) {
            if (gemId == targetId) {
                found = true
            }
            haveGems[index++] = found
        }
    }

    private fun setUpGemsGridView() {
        gems_recycler.apply {
            layoutManager = GridLayoutManager(context, 3)
            val gemAdapter =
                GemsGridAdapter(
                    context,
                    gemImages,
                    haveGems,
                    gemNames,
                    gemPrices,
                    this@MyStashActivity
                )
            adapter = gemAdapter
        }
    }

    override fun onGemClicked(name: String, price: Int) {
        gem_name.text = name
        gem_price.text = "Needs ${price} coins"
    }

    override fun onBackPressed() {
    }
}
