package com.example.chatter.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.chatter.R
import kotlinx.android.synthetic.main.fragment_favorite_deck_picker.*


class FavoriteDeckPickerFragment : BaseFragment() {
    private var decks = arrayOf<String>("My Curse Word", "My Favorite Foods")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_favorite_deck_picker, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpDeckPicker(decks)
    }

    private fun setUpDeckPicker(
        decks: Array<String>
    ) {
        deck_picker.maxValue = decks.size - 1
        deck_picker.minValue = 0
        deck_picker.wrapSelectorWheel = true
        deck_picker.displayedValues = decks
    }

    companion object {
        fun newInstance(): FavoriteDeckPickerFragment {
            return FavoriteDeckPickerFragment()
        }
    }
}