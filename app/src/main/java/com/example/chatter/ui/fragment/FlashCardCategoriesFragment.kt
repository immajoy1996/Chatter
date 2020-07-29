package com.example.chatter.ui.fragment

import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.chatter.R
import com.example.chatter.ui.activity.FlashCardActivity
import kotlinx.android.synthetic.main.fragment_flash_card_categories.*
import kotlinx.android.synthetic.main.top_bar.*

class FlashCardCategoriesFragment : BaseFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_flash_card_categories, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpTopBar()
        setUpButtons()
    }

    private fun setUpTopBar() {
        home.visibility = View.GONE
        back.visibility = View.VISIBLE
        back.setOnClickListener {
            activity?.finish()
        }
        top_bar_title.visibility = View.VISIBLE
        top_bar_title.text = "Flashcards"
    }

    private fun setUpButtons() {
        flashcard_button1_arrow.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
        flashcard_button2_arrow.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
        flashcard_button1_container.setOnClickListener {
            fragmentManager?.popBackStack()
            (activity as? FlashCardActivity)?.loadDecksFragment()
        }
    }
}