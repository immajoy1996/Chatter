package com.example.chatter.adapters

import android.content.Context
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
import com.example.chatter.extra.NOTIFICATION_TYPE
import com.example.chatter.ui.activity.DashboardActivity
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.bot_layout.view.*
import kotlinx.android.synthetic.main.fragment_story_board_one.*
import kotlinx.android.synthetic.main.user_profile_item_card.view.*
import kotlinx.android.synthetic.main.user_profile_item_card.view.card_subtitle
import kotlinx.android.synthetic.main.user_profile_item_card.view.card_title
import kotlinx.android.synthetic.main.user_profile_item_half_card.view.*

class StudyModeAdapter(
    val context: Context,
    var imageList: ArrayList<Int>,
    var cardTitles: ArrayList<String>,
    var cardSubtitles: ArrayList<String>

) :
    RecyclerView.Adapter<StudyModeAdapter.StudyModeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudyModeViewHolder {
        return StudyModeViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.user_profile_item_half_card,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: StudyModeViewHolder, position: Int) {
        val imagePath = imageList[position]
        val subtitle = cardSubtitles[position]
        val title = cardTitles[position]
        holder.bind(imagePath, title, subtitle)
    }

    override fun getItemCount(): Int = imageList.size

    inner class StudyModeViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(
            imagePath: Int,
            title: String,
            subTitle: String
        ) {
            itemView.card_image.setImageResource(imagePath)
            itemView.card_title.text = title
            itemView.card_subtitle.text = subTitle
            //itemView.inner_layout.startBounceAnimation()
        }

        private fun View.startBounceAnimation() {
            this.startAnimation(
                AnimationUtils.loadAnimation(context, R.anim.bounce)
            )
        }
    }
}