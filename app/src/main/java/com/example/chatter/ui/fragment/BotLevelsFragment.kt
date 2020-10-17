package com.example.chatter.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatter.R
import com.example.chatter.adapters.BotGridItemDecoration
import com.example.chatter.adapters.UserLevelsAdapter
import kotlinx.android.synthetic.main.fragment_bot_levels.*
import kotlinx.android.synthetic.main.top_bar.*

class BotLevelsFragment : BaseFragment() {
    private var levelNames =
        arrayListOf<String>(
            "Homeless",
            "Citizen"
        )
    private var levelImages = arrayListOf<Int>(
        R.drawable.homeless,
        R.drawable.citizen
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_bot_levels, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpTopBar()
        setUpRecycler()
    }

    private fun setUpTopBar() {
        back.visibility = View.VISIBLE
        home.visibility = View.GONE
        top_bar_title.text = "Language Level"
        user_level_close_button.setOnClickListener {
            fragmentManager?.popBackStack()
        }
    }

    private fun setUpRecycler() {
        val itemSpacing = 20
        if (levelImages.size == levelNames.size) {
            context?.let {
                user_levels_recycler.apply {
                    layoutManager = LinearLayoutManager(it, LinearLayoutManager.HORIZONTAL, false)
                    adapter = UserLevelsAdapter(it, levelNames, levelImages)
                    addItemDecoration(BotGridItemDecoration(itemSpacing, 0))
                }
            }
        } else {
            Toast.makeText(context, "mismatching names and image array", Toast.LENGTH_SHORT).show()
        }

    }
}