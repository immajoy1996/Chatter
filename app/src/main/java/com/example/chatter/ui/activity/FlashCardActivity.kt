package com.example.chatter.ui.activity

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.chatter.R
import com.example.chatter.ui.fragment.FlashCardCategoriesFragment
import com.example.chatter.ui.fragment.FlashCardDecksFragment
import kotlinx.android.synthetic.main.activity_flash_card.*

class FlashCardActivity : BaseActivity() {

    private var flashCardCategoriesFragment = FlashCardCategoriesFragment()
    private var decksFragment = FlashCardDecksFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flash_card)
        setUpTopBar()
        loadFlashCardsCategoriesFragment()
    }

    fun loadFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(flashcard_root_container.id, fragment)
            .addToBackStack(fragment.javaClass.name)
            .commit()
    }

    fun loadFlashCardsCategoriesFragment() {
        loadFragment(flashCardCategoriesFragment)
    }

    fun loadDecksFragment() {
        loadFragment(decksFragment)
    }

    override fun setUpTopBar() {
        //not implemented
    }
}