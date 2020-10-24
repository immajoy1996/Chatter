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

class UserProfileDashboardAdapter(
    val context: Context,
    var notificationType: ArrayList<NOTIFICATION_TYPE>,
    var imageList: ArrayList<Int>,
    var cardSubtitles: ArrayList<String>,
    var locationNames: ArrayList<String>

) :
    RecyclerView.Adapter<UserProfileDashboardAdapter.CardViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        when (viewType) {
            NOTIFICATION_TYPE.STUDYMODE.type -> {
                return CardViewHolder(
                    LayoutInflater.from(context).inflate(
                        R.layout.user_profile_item_card,
                        parent,
                        false
                    )
                )
            }
            NOTIFICATION_TYPE.QUIZ.type -> {
                return CardViewHolder(
                    LayoutInflater.from(context).inflate(
                        R.layout.quiz_notification_card_layout,
                        parent,
                        false
                    )
                )
            }
            NOTIFICATION_TYPE.LEVEL_REACHED.type -> {
                return CardViewHolder(
                    LayoutInflater.from(context).inflate(
                        R.layout.level_notificaiton_card_layout,
                        parent,
                        false
                    )
                )
            }

            else -> {
                return CardViewHolder(
                    LayoutInflater.from(context).inflate(
                        R.layout.user_profile_item_card,
                        parent,
                        false
                    )
                )
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return notificationType[position].type
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val imagePath = imageList[position]
        val locationName = locationNames[position]
        val subtitle = cardSubtitles[position]
        when (getItemViewType(position)) {
            NOTIFICATION_TYPE.LEVEL_REACHED.type -> {
                holder.bindLevelNotification(imagePath, subtitle)
            }
            else -> {
                holder.bind(imagePath, subtitle, locationName)
            }
        }
    }

    override fun getItemCount(): Int = imageList.size

    inner class CardViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(
            imagePath: Int,
            subTitle: String,
            locationName: String
        ) {
            itemView.card_image.setImageResource(imagePath)
            itemView.location_name.text = locationName
            itemView.card_subtitle.text = subTitle
        }

        fun bindLevelNotification(
            imagePath: Int,
            subTitle: String
        ) {
            itemView.card_image.setImageResource(imagePath)
            itemView.card_subtitle.text = subTitle
        }


    }
}