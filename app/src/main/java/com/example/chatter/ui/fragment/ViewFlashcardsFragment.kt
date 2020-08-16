package com.example.chatter.ui.fragment

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.*
import com.bumptech.glide.Glide
import com.example.chatter.R
import com.example.chatter.data.Vocab
import com.example.chatter.ui.activity.FlashCardActivity
import kotlinx.android.synthetic.main.fragment_view_flashcards.*
import java.util.*
import kotlin.collections.ArrayList

class ViewFlashcardsFragment : BaseFragment() {

    private var flashCardArray = arrayListOf<Vocab>()
    private var originalFlashCardArray = arrayListOf<Vocab>()
    private var cardIndex = 0
    var studyMode = "Learn"
    var isFavoriteFragment = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_view_flashcards, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        shuffleDeck()
        setUpOriginalFlashcardArray()
        setUpButtons()
        setUpNextFlashcard()
        setUpFlashCardClick()
        setUpFlashcardArrowClick()
    }

    private fun setUpOriginalFlashcardArray() {
        originalFlashCardArray.clear()
        for (card in flashCardArray) {
            originalFlashCardArray.add(card)
        }
    }

    private fun shuffleDeck() {
        Collections.shuffle(flashCardArray)
    }

    private fun setUpFlashcardArrowClick() {
        val size = flashCardArray.size
        view_flashcards_right_arrow.setOnClickListener {
            var index = (cardIndex + 1) % size
            while (flashCardArray[index].expression == flashCardArray[cardIndex].expression) {
                index = (index + 1) % size
            }
            cardIndex = index
            setUpNextFlashcard()
        }
        view_flashcards_left_arrow.setOnClickListener {
            var index = (cardIndex - 1 + size) % size
            while (flashCardArray[index].expression == flashCardArray[cardIndex].expression) {
                index = (index - 1 + size) % size
            }
            cardIndex = index
            setUpNextFlashcard()
        }
    }

    private fun setUpNextFlashcard() {
        val card = flashCardArray[cardIndex]
        if (card.flashcardType == "text") {
            flashcard_text.text = card.expression
            flashcard_image.visibility = View.GONE
            flashcard_text.visibility = View.VISIBLE
            showFlashcardButtons()
        } else {
            flashcard_text.text = card.expression
            flashcard_image.visibility = View.GONE
            flashcard_text.visibility = View.VISIBLE
            showFlashcardButtons()
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
            if (isFavoriteFragment) {
                (activity as? FlashCardActivity)?.loadMyFavoritesFragment()
            } else {
                (activity as? FlashCardActivity)?.loadDecksFragment()
            }
        }
        flashcard_learn_mode.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP)
        flashcard_favorite.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP)
        flashcard_audio.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP)
        flashcard_learn_mode.setOnClickListener {
            context?.let {
                showStudyModeDialog(it)
            }
        }
        flashcard_audio.setOnClickListener {
            if (flashcard_text.visibility == View.VISIBLE) {
                (activity as? FlashCardActivity)?.letBearSpeak(flashcard_text.text.toString())
            }
        }
        view_flashcards_correct_button.setOnClickListener {
            updateFlashcardArray(true)
            setUpNextFlashcard()
            showFlashcardArrowsLayout()
            hideCorrectWrongButtons()
        }
        view_flashcards_wrong_button.setOnClickListener {
            updateFlashcardArray(false)
            setUpNextFlashcard()
            showFlashcardArrowsLayout()
            hideCorrectWrongButtons()
        }
    }

    private fun updateFlashcardArray(gotItRight: Boolean) {
        var total = WRONG_NUM_COPIES
        if (gotItRight) {
            total = CORRECT_NUM_COPIES
        }
        val curCard = flashCardArray[cardIndex]
        for (i in 0 until total) {
            flashCardArray.add(curCard)
        }
        val size = flashCardArray.size
        var index = (Math.random() * size).toInt()
        var newCard = flashCardArray[index]
        while (newCard.expression == curCard.expression) {
            index = (Math.random() * size).toInt()
            newCard = flashCardArray[index]
        }
        cardIndex = index
    }


    private fun showStudyModeDialog(context: Context) {
        val dialogBuilder = AlertDialog.Builder(context)
        var msg = "Switch to Study Mode?"
        if (studyMode == "Study") {
            msg = "Switch to Learn Mode?"
        }
        dialogBuilder.setMessage(msg)
            .setCancelable(false)
            .setPositiveButton("OK", { dialog, id ->
                if (studyMode == "Study") {
                    studyMode = "Learn"
                } else {
                    studyMode = "Study"
                }
                //preferences.storeStudyMode(studyMode)
                setUpNewStudyMode()
                setUpNextFlashcard()
                dialog.dismiss()
            })
            .setNegativeButton("Cancel", { dialog, id ->
                dialog.dismiss()
            })

        val alert = dialogBuilder.create()
        var title = "Learn"
        if (studyMode == "Learn") {
            title = "Study"
        }
        alert.setTitle("$title Mode")
        alert.show()
    }

    private fun hideFlashcardButtons() {
        flashcard_audio.visibility = View.GONE
        flashcard_learn_mode.visibility = View.GONE
        flashcard_favorite.visibility = View.GONE
    }

    private fun showFlashcardButtons() {
        flashcard_audio.visibility = View.VISIBLE
        flashcard_learn_mode.visibility = View.VISIBLE
        flashcard_favorite.visibility = View.VISIBLE
    }

    private fun resetDeck() {
        flashCardArray.clear()
        for (card in originalFlashCardArray) {
            flashCardArray.add(card)
        }
        cardIndex = 0
        shuffleDeck()
    }

    private fun setUpNewStudyMode() {
        if (studyMode == "Study") {
            resetDeck()
            view_flashcards_arrow_layout.visibility = View.VISIBLE
            view_flashcards_correct_wrong_layout.visibility = View.GONE
            flashcards_progress_bar.visibility = View.GONE
        } else {
            resetDeck()
            view_flashcards_arrow_layout.visibility = View.VISIBLE
            view_flashcards_correct_wrong_layout.visibility = View.GONE
            flashcards_progress_bar.visibility = View.VISIBLE
        }
    }

    private fun showCorrectWrongButtons() {
        view_flashcards_correct_wrong_layout.visibility = View.VISIBLE
    }

    private fun hideCorrectWrongButtons() {
        view_flashcards_correct_wrong_layout.visibility = View.GONE
    }

    private fun showFlashcardArrowsLayout() {
        view_flashcards_arrow_layout.visibility = View.VISIBLE
    }

    private fun hideFlashcardArrowsLayout() {
        view_flashcards_arrow_layout.visibility = View.GONE
    }

    private fun setUpFlashCardClick() {
        view_flashcards_container.setOnClickListener {
            val card = flashCardArray[cardIndex]
            if (card.flashcardType == "text") {
                if (flashcard_text.text == card.expression) {
                    hideFlashcardButtons()
                    flashcard_audio.visibility = View.VISIBLE
                    flashcard_text.text = card.definition
                    if (studyMode == "Study") {
                        showCorrectWrongButtons()
                        hideFlashcardArrowsLayout()
                    }
                } else {
                    showFlashcardButtons()
                    flashcard_text.text = card.expression
                    if (studyMode == "Study") {
                        hideCorrectWrongButtons()
                        showFlashcardArrowsLayout()
                    }
                }
            } else {
                if (flashcard_image.visibility == View.VISIBLE) {
                    showFlashcardButtons()
                    flashcard_image.visibility = View.GONE
                    flashcard_text.visibility = View.VISIBLE
                    if (studyMode == "Study") {
                        hideCorrectWrongButtons()
                        showFlashcardArrowsLayout()
                    }
                } else {
                    hideFlashcardButtons()
                    flashcard_image.visibility = View.VISIBLE
                    flashcard_text.visibility = View.GONE
                    if (studyMode == "Study") {
                        showCorrectWrongButtons()
                        hideFlashcardArrowsLayout()
                    }
                }
            }
        }
    }

    companion object {
        fun newInstance(
            flashCardArray: ArrayList<Vocab>,
            isFavoriteFragment: Boolean
        ): ViewFlashcardsFragment {
            val fragment = ViewFlashcardsFragment()
            fragment.flashCardArray = flashCardArray
            fragment.isFavoriteFragment = isFavoriteFragment
            return fragment
        }

        private const val WRONG_NUM_COPIES = 5
        private const val CORRECT_NUM_COPIES = 3
    }
}