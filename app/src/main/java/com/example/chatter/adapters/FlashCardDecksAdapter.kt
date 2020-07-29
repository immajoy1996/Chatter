package com.example.chatter.adapters

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chatter.R
import com.example.chatter.extra.MyBounceInterpolator
import com.example.chatter.ui.activity.DashboardActivity
import kotlinx.android.synthetic.main.bot_layout.view.*
import kotlinx.android.synthetic.main.flashcard_decks_layout.view.*
import kotlinx.android.synthetic.main.top_bar.view.*

class FlashCardDecksAdapter(
    val context: Context,
    var botTitles: ArrayList<String>,
    var botImages: ArrayList<String>,
    var botDescriptions: ArrayList<String>
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
        val image = botImages[position]
        val desc = botDescriptions[position]
        holder.bind(image, title, desc)
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

        fun bind(imagePath: String, title: String, desc: String) {
            /*val aniShakeForever = AnimationUtils.loadAnimation(
                context,
                R.anim.shake_forever
            )
            val aniWobbleForever = AnimationUtils.loadAnimation(
                context,
                R.anim.wobble_forever
            )
            var animPicked = aniShakeForever
            if (Math.random() > .5) {
                animPicked = aniWobbleForever
            }
            botImage?.startAnimation(animPicked)*/
            if (title.isNotEmpty()) {
                botImage?.let {
                    Glide.with(context)
                        .load(imagePath)
                        .into(it)
                }
                botTitle?.text = title
                botDesc?.text = desc
            }
            onDeckSelected()
        }

        fun onDeckSelected() {
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
    }
}