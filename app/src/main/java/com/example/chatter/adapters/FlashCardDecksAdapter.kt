package com.example.chatter.adapters

import ProgressBarAnimation
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chatter.R
import com.example.chatter.interfaces.ConcentrationGameClickedInterface
import com.example.chatter.interfaces.DeckSelectedInterface
import com.example.chatter.interfaces.MultipleChoiceClickedInterface
import com.example.chatter.interfaces.SpeechGameClickedInterface
import kotlinx.android.synthetic.main.flashcard_decks_layout.view.*
import kotlinx.android.synthetic.main.fragment_bot_description.*
import kotlinx.android.synthetic.main.fragment_view_flashcards.view.*

class FlashCardDecksAdapter(
    val context: Context,
    var botTitles: ArrayList<String>,
    var botImages: ArrayList<String>? = null,
    var gameImages: ArrayList<Int>? = null,
    var botDescriptions: ArrayList<String>,
    var isGameFragment: Boolean? = null,
    var onDecksSelectedInterface: DeckSelectedInterface? = null,
    var concentrationGameClickedInterface: ConcentrationGameClickedInterface? = null,
    var multipleChoiceClickedInterface: MultipleChoiceClickedInterface? = null,
    var speechGameClickedInterface: SpeechGameClickedInterface? = null,
    var completionPercentage: ArrayList<Int>? = null,
    var isMultipleChoiceDecksFragment: Boolean? = null
) :
    RecyclerView.Adapter<FlashCardDecksAdapter.FlashCardLevelsViewHolder>() {
    private var selectedBotPos = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FlashCardLevelsViewHolder {
        return FlashCardLevelsViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.flashcard_decks_layout,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: FlashCardLevelsViewHolder, position: Int) {
        val title = botTitles[position]
        val desc = botDescriptions[position]
        var rate: Int? = null
        completionPercentage?.let {
            rate = it[position]
        }
        if (isGameFragment == true) {
            gameImages?.let {
                holder.bind(it[position], title, desc)
            }
        } else {
            botImages?.let {
                holder.bind(it[position], title, desc, rate, position)
            }
        }
    }

    override fun getItemCount(): Int = botTitles.size

    inner class FlashCardLevelsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private var botImage: ImageView? = null
        private var botTitle: TextView? = null
        private var botDesc: TextView? = null

        init {
            botImage = view.flashcard_bot_image
            botTitle = view.flashcard_deck_title
            botDesc = view.flashcard_deck_desc
        }

        private fun hideCompletionProgress() {
            itemView.flashcard_completion_rate.visibility = View.INVISIBLE
            itemView.decks_progress_bar.visibility = View.INVISIBLE
        }

        private fun showCompletionProgress() {
            itemView.flashcard_completion_rate.visibility = View.VISIBLE
            itemView.decks_progress_bar.visibility = View.VISIBLE
        }

        fun bind(image: String, title: String, desc: String, completionRate: Int?, position: Int) {
            if (position == selectedBotPos) {
                itemView.decks_item_layout.setBackgroundColor(Color.parseColor("#e4e5e9"))
            } else {
                itemView.decks_item_layout.setBackgroundColor(Color.parseColor("#ffffff"))
            }
            if (title.isNotEmpty()) {
                itemView.create_deck_layout.visibility = View.GONE
                itemView.normal_decks_layout.visibility = View.VISIBLE
                itemView.decks_progress_bar.visibility = View.VISIBLE
                itemView.flashcard_completion_rate.visibility = View.VISIBLE
                itemView.create_decks_divider.visibility = View.GONE
                itemView.normal_decks_divider.visibility = View.VISIBLE
                if (completionRate != null) {
                    itemView.flashcard_completion_rate.visibility = View.VISIBLE
                    itemView.decks_progress_bar.visibility = View.VISIBLE
                    itemView.flashcard_completion_rate.text = "${completionRate} %"
                    itemView.decks_progress_bar.setProgress(completionRate)
                    if (completionRate == 100) {
                        itemView.flashcard_complete_layout_container.visibility = View.VISIBLE
                        hideCompletionProgress()
                    } else if (completionRate == -1) {
                        itemView.flashcard_complete_layout_container.visibility = View.GONE
                        hideCompletionProgress()
                    } else {
                        itemView.flashcard_complete_layout_container.visibility = View.GONE
                        showCompletionProgress()
                    }
                } else {
                    itemView.flashcard_complete_layout_container.visibility = View.GONE
                    hideCompletionProgress()
                }
                botImage?.let {
                    Glide.with(context)
                        .load(image)
                        .into(it)
                }
                botTitle?.text = title
                botDesc?.text = desc
                toggleSelectedDeck(title, position)
            } else {
                itemView.decks_progress_bar.visibility = View.INVISIBLE
                itemView.create_deck_layout.visibility = View.VISIBLE
                itemView.normal_decks_layout.visibility = View.GONE
                itemView.create_decks_divider.visibility = View.VISIBLE
                itemView.normal_decks_divider.visibility = View.GONE
            }
        }

        fun bind(imagePath: Int, title: String, desc: String) {
            if (title.isNotEmpty()) {
                botImage?.setImageResource(imagePath)
                botTitle?.text = title
                botDesc?.text = desc
            }
            itemView.decks_progress_bar.visibility = View.INVISIBLE
            itemView.flashcard_completion_rate.visibility = View.GONE
            onGameSelected(title)
        }

        private fun onGameSelected(title: String) {
            itemView.setOnClickListener {
                launchGames(title)
            }
        }

        private fun refreshAdapter() {
            notifyDataSetChanged()
        }

        private fun toggleSelectedDeck(title: String, position: Int) {
            itemView.setOnClickListener {
                if ((itemView.decks_item_layout.background as ColorDrawable).color == Color.parseColor(
                        "#ffffff"
                    )
                ) {
                    itemView.decks_item_layout.setBackgroundColor(Color.parseColor("#e4e5e9"))
                    if (isMultipleChoiceDecksFragment == true) {
                        selectedBotPos = position
                        onDecksSelectedInterface?.onDeckSelected(title,true)
                        refreshAdapter()
                    }else{
                        onDecksSelectedInterface?.onDeckSelected(title,false)
                    }
                } else {
                    itemView.decks_item_layout.setBackgroundColor(Color.parseColor("#ffffff"))
                    onDecksSelectedInterface?.onDeckUnselected(title)
                    if (isMultipleChoiceDecksFragment == true) {
                        selectedBotPos = -1
                        refreshAdapter()
                    }
                }
            }
        }

        private fun launchGames(gameTitle: String) {
            when (gameTitle) {
                "Concentration" -> {
                    concentrationGameClickedInterface?.onConcentrationGameClicked()
                }
                "Multiple Choice" -> {
                    multipleChoiceClickedInterface?.onMultipleChoiceClicked()
                }
                "Listen to me!" -> {
                    speechGameClickedInterface?.onSpeechGameClicked()
                }
            }
        }
    }
}