package com.example.chatter.adapters

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chatter.R
import com.example.chatter.interfaces.ConcentrationGameClickedInterface
import com.example.chatter.interfaces.MultipleChoiceClickedInterface
import kotlinx.android.synthetic.main.flashcard_decks_layout.view.*

class FlashCardDecksAdapter(
    val context: Context,
    var botTitles: ArrayList<String>,
    var botImages: ArrayList<String>? = null,
    var gameImages: ArrayList<Int>? = null,
    var botDescriptions: ArrayList<String>,
    var isGameFragment: Boolean? = null,
    var concentrationGameClickedInterface: ConcentrationGameClickedInterface? = null,
    var multipleChoiceClickedInterface: MultipleChoiceClickedInterface? = null
) :
    RecyclerView.Adapter<FlashCardDecksAdapter.FlashCardLevelsViewHolder>() {

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
        if (isGameFragment == true) {
            gameImages?.let {
                holder.bind(it[position], title, desc)
            }
        } else {
            botImages?.let {
                holder.bind(it[position], title, desc)
            }
        }
    }

    override fun getItemCount(): Int = botTitles.size

    inner class FlashCardLevelsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private var botImage: ImageView? = null
        private var botTitle: TextView? = null
        private var botDesc: TextView? = null
        private var botLayout: ConstraintLayout? = null

        init {
            botImage = view.flashcard_bot_image
            botTitle = view.flashcard_deck_title
            //botLayout = view.bot_item_inner_layout
            botDesc = view.flashcard_deck_desc
        }

        fun bind(image: String, title: String, desc: String) {
            itemView.decks_progress_bar.visibility = View.VISIBLE
            itemView.flashcard_completion_rate.visibility = View.VISIBLE
            if (title.isNotEmpty()) {
                botImage?.let {
                    Glide.with(context)
                        .load(image)
                        .into(it)
                }
                botTitle?.text = title
                botDesc?.text = desc
            }
            onDeckSelected()
        }

        fun bind(imagePath: Int, title: String, desc: String) {
            if (title.isNotEmpty()) {
                botImage?.setImageResource(imagePath)
                botTitle?.text = title
                botDesc?.text = desc
            }
            itemView.decks_progress_bar.visibility = View.GONE
            itemView.flashcard_completion_rate.visibility = View.GONE
            onGameSelected(title)
        }

        private fun onGameSelected(title: String) {
            itemView.setOnClickListener {
                launchGames(title)
            }
        }

        private fun onDeckSelected() {
            itemView.setOnClickListener {
                if ((itemView.decks_item_layout.background as ColorDrawable).color == Color.parseColor(
                        "#ffffff"
                    )
                ) {
                    itemView.decks_item_layout.setBackgroundColor(Color.parseColor("#e4e5e9"))
                } else {
                    itemView.decks_item_layout.setBackgroundColor(Color.parseColor("#ffffff"))
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
            }
        }
    }
}