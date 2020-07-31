package com.example.chatter.ui.fragment

import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.*
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.View.OnTouchListener
import com.example.chatter.R
import com.example.chatter.extra.OnSwipeTouchListener
import com.example.chatter.ui.activity.FlashCardActivity
import kotlinx.android.synthetic.main.fragment_view_flashcards.*


class ViewFlashcardsFragment : BaseFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_view_flashcards, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpButtons()
        setUpFlashCardClick()
    }

    private fun setUpButtons() {
        view_flashcards_back_button.setOnClickListener {
            fragmentManager?.popBackStack()
            (activity as? FlashCardActivity)?.loadDecksFragment()
        }
        flashcard_learn_mode.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP)
        flashcard_favorite.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP)
        flashcard_audio.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP)
        flashcard_favorite.setOnClickListener {
            //Todo
        }
        flashcards_continue_button.setOnClickListener {
            if (flashcard_text.text == "Grizzly bear") {
                onSwipeLeft()
            } else {
                onSwipeRight()
            }
        }
    }

    private fun setUpFlashCardClick() {
        view_flashcards_container.setOnClickListener {
            if (flashcard_image.visibility == View.VISIBLE) {
                flashcard_image.visibility = View.GONE
                flashcard_text.visibility = View.VISIBLE
            } else {
                flashcard_image.visibility = View.VISIBLE
                flashcard_text.visibility = View.GONE
            }
        }
    }

    fun onSwipeRight() {
        flashcard_text.text = "Grizzly bear"
        flashcard_image.setImageResource(R.drawable.mad_bear)
        flashcard_image.visibility = View.GONE
        flashcard_text.visibility = View.VISIBLE
    }

    fun onSwipeLeft() {
        flashcard_text.text = "Jewel"
        flashcard_image.setImageResource(R.drawable.gem)
        flashcard_image.visibility = View.GONE
        flashcard_text.visibility = View.VISIBLE
    }

    private fun setUpFlashCardSwipe() {
        view_flashcards_container.setOnTouchListener(object : OnSwipeTouchListener(context) {
            override fun onSwipeRight() {
                flashcard_text.text = "Grizzly bear"
                flashcard_image.setImageResource(R.drawable.mad_bear)
                flashcard_image.visibility = View.GONE
                flashcard_text.visibility = View.VISIBLE
            }

            override fun onSwipeLeft() {
                flashcard_text.text = "Jewel"
                flashcard_image.setImageResource(R.drawable.gem)
                flashcard_image.visibility = View.GONE
                flashcard_text.visibility = View.VISIBLE
            }

            override fun onClick() {
                if (flashcard_image.visibility == View.VISIBLE) {
                    flashcard_image.visibility = View.GONE
                    flashcard_text.visibility = View.VISIBLE
                } else {
                    flashcard_image.visibility = View.VISIBLE
                    flashcard_text.visibility = View.GONE
                }
            }
        })
    }


}