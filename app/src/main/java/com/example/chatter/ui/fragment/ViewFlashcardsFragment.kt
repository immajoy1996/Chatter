package com.example.chatter.ui.fragment

import ProgressBarAnimation
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.PorterDuff
import android.media.AudioManager
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import com.bumptech.glide.Glide
import com.example.chatter.R
import com.example.chatter.data.Vocab
import com.example.chatter.ui.activity.FlashCardActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.fragment_view_flashcards.*
import java.util.*
import kotlin.collections.ArrayList

class ViewFlashcardsFragment : BaseFragment() {

    private var flashCardArray = arrayListOf<Vocab>()
    private var originalFlashCardArray = arrayListOf<Vocab>()
    private var cardIndex = 0
    var studyMode = "Learn"
    var isFavoriteFragment = false
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var audioManager: AudioManager
    private var totalSeen: Int = 0
    private var totalFlashcards = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_view_flashcards, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
        audioManager = context?.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        setUpBackButton()
        if (flashCardArray.size > 0) {
            showFlashcardsLayout()
            shuffleDeck()
            setUpOriginalFlashcardArray()
            setUpButtons()
            setUpNextFlashcard()
            setUpFlashCardClick()
            setUpFlashcardArrowClick()
            initProgressBar()
            if (flashCardArray.size == 1) {
                hideFlashcardArrowsLayout()
            } else {
                showFlashcardArrowsLayout()
            }
        } else {
            showNoFlashcardsMessage()
        }
    }

    private fun showFlashcardsLayout() {
        view_flashcards_root_container.visibility = View.VISIBLE
        no_favorites_textview.visibility = View.GONE
    }

    private fun showNoFlashcardsMessage() {
        view_flashcards_root_container.visibility = View.GONE
        no_favorites_textview.visibility = View.VISIBLE
    }

    private fun setUpBackButton() {
        view_flashcards_back_button.setOnClickListener {
            fragmentManager?.popBackStack()
            if (isFavoriteFragment) {
                (activity as? FlashCardActivity)?.loadFlashCardsCategoriesFragment()
            } else {
                (activity as? FlashCardActivity)?.loadDecksFragment()
            }
        }
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
            while (index != cardIndex && flashCardArray[index].expression == flashCardArray[cardIndex].expression) {
                index = (index + 1) % size
            }
            cardIndex = index
            setUpNextFlashcard()
            updateProgressBar()
        }
        view_flashcards_left_arrow.setOnClickListener {
            var index = (cardIndex - 1 + size) % size
            while (index != cardIndex && flashCardArray[index].expression == flashCardArray[cardIndex].expression) {
                index = (index - 1 + size) % size
            }
            cardIndex = index
            setUpNextFlashcard()
            updateProgressBar()
        }
    }

    private fun initProgressBar() {
        for (cardItem in flashCardArray) {
            totalFlashcards++
            preferences.addNewFlashcardToList(cardItem)
            if (preferences.flashcardAlreadySeen(cardItem)) {
                totalSeen++
            }
        }
        Log.d("SeenFlashcards", totalSeen.toString().plus(" ").plus(totalFlashcards.toString()))
        view_flashcards_progress_bar.setProgress((100.0 * totalSeen / totalFlashcards).toInt())
    }

    private fun updateProgressBar() {
        val card = flashCardArray[cardIndex]
        val oldProgress = (100.0 * totalSeen / totalFlashcards).toFloat()
        card.whichBot?.let {
            Log.d(
                "SeenFlashcard",
                (card.whichBot ?: "null").plus(preferences.flashcardAlreadySeen(card).toString())
            )
            if (!preferences.flashcardAlreadySeen(card)) {
                preferences.storeNewFlashcard(card)
                totalSeen++
            }
        }
        val newProgress = (100.0 * totalSeen / totalFlashcards).toFloat()
        Log.d("Seen Flashcard count", totalSeen.toString())
        if (oldProgress != newProgress) {
            showProgressAnimation(oldProgress, newProgress, view_flashcards_progress_bar)
        }
    }

    private fun showProgressAnimation(from: Float, to: Float, progressBar: ProgressBar) {
        val anim = ProgressBarAnimation(progressBar, from, to)
        anim.duration = 1000
        progressBar.startAnimation(anim)
    }

    private fun setUpFavoriteStar() {
        if (flashCardArray[cardIndex].isFavorite) {
            toggleFavoriteStartToYellow()
        } else {
            toggleFavoriteStartToBlack()
        }
    }

    private fun setUpNextFlashcard() {
        val card = flashCardArray[cardIndex]
        if (card.flashcardType == "text") {
            flashcard_text.text = card.expression
            flashcard_image.visibility = View.GONE
            flashcard_text.visibility = View.VISIBLE
            showFlashcardButtons()
            setUpFavoriteStar()
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
            setUpFavoriteStar()
        }
    }

    private fun selectRandomNumber(max: Int): Int {
        return (Math.random() * max).toInt()
    }

    private fun toggleFavoriteStartToYellow() {
        flashcard_favorite.setColorFilter(Color.parseColor("#FFDF00"), PorterDuff.Mode.SRC_ATOP)
    }

    private fun toggleFavoriteStartToBlack() {
        flashcard_favorite.setColorFilter(Color.parseColor("#000000"), PorterDuff.Mode.SRC_ATOP)
    }

    private fun addCardToFavorites(card: Vocab) {
        val suffix = selectRandomNumber(1000000).toString()
        if (auth.currentUser != null) {
            auth.currentUser?.let {
                val uid = it.uid
                database.child("Users").child(uid).child("My Favorites").child("Word $suffix")
                    .setValue(card)
                    .addOnSuccessListener {
                        flashCardArray[cardIndex].isFavorite = true
                        Toast.makeText(context, "Added to favorites", Toast.LENGTH_SHORT).show()
                        toggleFavoriteStartToYellow()
                    }.addOnFailureListener {
                        flashCardArray[cardIndex].isFavorite = false
                        Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show()
                        toggleFavoriteStartToBlack()
                    }
            }
        } else {
            flashCardArray[cardIndex].isFavorite = true
            val favsArray = preferences.getMyFavoritesArray()
            if (favsArray?.contains(flashCardArray[cardIndex]) == false) {
                favsArray?.add(flashCardArray[cardIndex])
            }
            favsArray?.let {
                preferences.storeMyFavoritesJsonString(it)
            }
            toggleFavoriteStartToYellow()
            Toast.makeText(context, "Added to favorites", Toast.LENGTH_SHORT).show()
        }
    }

    private fun removeCardFromFavorites(card: Vocab) {
        if (auth.currentUser != null) {
            auth.currentUser?.let {
                val uid = it.uid
                database.child("Users").child(uid).child("My Favorites")
                    .addChildEventListener(baseChildEventListener {
                        val expression = it.child("expression").value.toString()
                        if (expression == card.expression) {
                            it.key?.let {
                                database.child("Vocab/My Favorites").child(it).removeValue()
                                    .addOnSuccessListener {
                                        flashCardArray[cardIndex].isFavorite = false
                                        toggleFavoriteStartToBlack()
                                        Toast.makeText(
                                            context,
                                            "Removed from favorites",
                                            Toast.LENGTH_SHORT
                                        )
                                            .show()
                                    }.addOnFailureListener {
                                        flashCardArray[cardIndex].isFavorite = true
                                        toggleFavoriteStartToYellow()
                                        Toast.makeText(
                                            context,
                                            "Something went wrong",
                                            Toast.LENGTH_SHORT
                                        )
                                            .show()
                                    }
                            }
                        }
                    })
            }
        } else {
            val favsArray = preferences.getMyFavoritesArray()
            while (favsArray?.contains(flashCardArray[cardIndex]) == true) {
                favsArray?.remove(flashCardArray[cardIndex])
            }
            flashCardArray[cardIndex].isFavorite = false
            favsArray?.let {
                preferences.storeMyFavoritesJsonString(it)
            }
            toggleFavoriteStartToBlack()
            Toast.makeText(
                context,
                "Removed from favorites",
                Toast.LENGTH_SHORT
            )
                .show()
        }
    }

    private fun setUpButtons() {
        flashcard_learn_mode.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP)
        flashcard_favorite.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP)
        flashcard_audio.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP)
        flashcard_learn_mode.setOnClickListener {
            context?.let {
                showStudyModeDialog(it)
            }
        }
        flashcard_favorite.setOnClickListener {
            val card = flashCardArray[cardIndex]
            if (card.isFavorite) {
                toggleFavoriteStartToBlack()
                removeCardFromFavorites(card)
            } else {
                toggleFavoriteStartToYellow()
                addCardToFavorites(card)
            }
        }
        flashcard_audio.setOnClickListener {
            if (flashcard_text.visibility == View.VISIBLE) {
                if (audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) == 0) {
                    Toast.makeText(context, "Turn up your volume", Toast.LENGTH_LONG).show()
                } else {
                    (activity as? FlashCardActivity)?.letBearSpeak(flashcard_text.text.toString())
                }
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

    private fun hideFavoritesButton() {
        flashcard_favorite.visibility = View.GONE
    }

    private fun showFavoritesButton() {
        flashcard_favorite.visibility = View.VISIBLE
    }

    private fun setUpNewStudyMode() {
        if (studyMode == "Study") {
            resetDeck()
            //hideFavoritesButton()
            view_flashcards_arrow_layout.visibility = View.VISIBLE
            view_flashcards_correct_wrong_layout.visibility = View.GONE
            view_flashcards_progress_bar.visibility = View.GONE
        } else {
            resetDeck()
            //showFavoritesButton()
            view_flashcards_arrow_layout.visibility = View.VISIBLE
            view_flashcards_correct_wrong_layout.visibility = View.GONE
            view_flashcards_progress_bar.visibility = View.VISIBLE
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