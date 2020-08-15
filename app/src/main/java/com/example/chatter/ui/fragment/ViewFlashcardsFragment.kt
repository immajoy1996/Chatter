package com.example.chatter.ui.fragment

import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.*
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.View.OnTouchListener
import com.bumptech.glide.Glide
import com.example.chatter.R
import com.example.chatter.data.Vocab
import com.example.chatter.extra.OnSwipeTouchListener
import com.example.chatter.ui.activity.FlashCardActivity
import kotlinx.android.synthetic.main.fragment_view_flashcards.*
import java.util.*
import kotlin.collections.ArrayList


class ViewFlashcardsFragment : BaseFragment() {
    private var flashCardArray = arrayListOf<Vocab>()
    private var cardIndex = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_view_flashcards, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpButtons()
        shuffleDeck()
        setUpNextFlashcard()
        setUpFlashCardClick()
        setUpFlashcardArrowClick()
    }

    private fun shuffleDeck() {
        Collections.shuffle(flashCardArray)
    }

    private fun setUpFlashcardArrowClick() {
        view_flashcards_right_arrow.setOnClickListener {
            cardIndex = (cardIndex + 1) % flashCardArray.size
            setUpNextFlashcard()
        }
        view_flashcards_left_arrow.setOnClickListener {
            cardIndex = (cardIndex - 1 + flashCardArray.size) % flashCardArray.size
            setUpNextFlashcard()
        }
    }

    private fun setUpNextFlashcard() {
        val card = flashCardArray[cardIndex]
        if (card.flashcardType == "text") {
            flashcard_text.text = card.expression
            flashcard_image.visibility = View.GONE
            flashcard_text.visibility = View.VISIBLE
            flashcard_audio.visibility = View.VISIBLE
        } else {
            flashcard_text.text = card.expression
            flashcard_image.visibility = View.GONE
            flashcard_text.visibility = View.VISIBLE
            flashcard_audio.visibility = View.VISIBLE
            context?.let {
                Glide.with(it)
                    .load(card.image)
                    .into(flashcard_image)
            }
        }
    }

    private fun setUpButtons() {
        view_flashcards_back_button.setOnClickListener {
            fragmentManager?.popBackStack()
            (activity as? FlashCardActivity)?.loadDecksFragment()
        }
        flashcard_learn_mode.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP)
        flashcard_favorite.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP)
        flashcard_audio.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP)
        flashcard_audio.setOnClickListener {
            if (flashcard_text.visibility == View.VISIBLE) {
                (activity as? FlashCardActivity)?.letBearSpeak(flashcard_text.text.toString())
            }
        }
    }

    private fun setUpFlashCardClick() {
        view_flashcards_container.setOnClickListener {
            val card = flashCardArray[cardIndex]
            if (card.flashcardType == "text") {
                flashcard_audio.visibility = View.VISIBLE
                if (flashcard_text.text == card.expression) {
                    flashcard_text.text = card.definition
                } else {
                    flashcard_text.text = card.expression
                }
            } else {
                if (flashcard_image.visibility == View.VISIBLE) {
                    flashcard_audio.visibility = View.VISIBLE
                    flashcard_image.visibility = View.GONE
                    flashcard_text.visibility = View.VISIBLE
                } else {
                    flashcard_audio.visibility = View.GONE
                    flashcard_image.visibility = View.VISIBLE
                    flashcard_text.visibility = View.GONE
                }
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

    companion object {
        fun newInstance(flashCardArray: ArrayList<Vocab>): ViewFlashcardsFragment {
            val fragment = ViewFlashcardsFragment()
            fragment.flashCardArray = flashCardArray
            return fragment
        }
    }


}