package com.example.chatter.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.TranslateAnimation
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.chatter.R
import com.example.chatter.data.NotificationItem
import com.example.chatter.extra.MyBounceInterpolator
import com.example.chatter.extra.NOTIFICATION_TYPE
import com.example.chatter.ui.activity.BotStoryActivityLatest
import com.example.chatter.ui.activity.GamesActivity
import com.example.chatter.ui.activity.SpeechGameActivity
import kotlinx.android.synthetic.main.quiz_notification_card_layout.view.*
import kotlinx.android.synthetic.main.user_profile_item_card.view.*
import kotlinx.android.synthetic.main.user_profile_item_card.view.card_subtitle
import kotlinx.android.synthetic.main.user_profile_item_card.view.card_title
import kotlinx.android.synthetic.main.user_profile_item_card.view.location_name


class UserProfileDashboardAdapter(
    val context: Context,
    var notificationList: ArrayList<NotificationItem>
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
            NOTIFICATION_TYPE.PRACTICE_GAMES.type -> {
                return CardViewHolder(
                    LayoutInflater.from(context).inflate(
                        R.layout.practice_games_notification_card_layout,
                        parent,
                        false
                    )
                )
            }
            NOTIFICATION_TYPE.PRACTICE_FLASHCARDS.type -> {
                return CardViewHolder(
                    LayoutInflater.from(context).inflate(
                        R.layout.practice_flashcards_notification_card_layout,
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
        return notificationList[position].notificationType.type
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val imagePath = notificationList[position].image
        val subtitle = notificationList[position].subtitle
        val title = notificationList[position].title
        val locationName = notificationList[position].locationName
        when (getItemViewType(position)) {
            NOTIFICATION_TYPE.STUDYMODE.type -> {
                if (imagePath != null && title != null && subtitle != null) {
                    holder.bindStoryModeCard(imagePath, title, subtitle, locationName)
                }
            }
            NOTIFICATION_TYPE.LEVEL_REACHED.type -> {
                if (imagePath != null && subtitle != null) {
                    holder.bindLevelNotification(imagePath, subtitle)
                }
            }
            NOTIFICATION_TYPE.QUIZ.type -> {
                if (imagePath != null && title != null && subtitle != null) {
                    holder.bindQuizNotification(imagePath, title, subtitle)
                }
            }
            NOTIFICATION_TYPE.PRACTICE_GAMES.type -> {
                holder.bindGamesNotification()
            }
            NOTIFICATION_TYPE.PRACTICE_FLASHCARDS.type -> {
                holder.bindFlashcardsNotification()
            }
        }
    }

    override fun getItemCount(): Int = notificationList.size

    inner class CardViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bindStoryModeCard(
            imagePath: Int,
            title: String,
            subTitle: String,
            locationName: String?
        ) {
            itemView.story_card_image.setImageResource(imagePath)
            if (locationName != null) {
                itemView.location_name.text = locationName
            }
            itemView.card_subtitle.text = subTitle
            itemView.card_title.text = title
            itemView.user_profile_item_card_layout.startBounceAnimation()
            val botTitle = subTitle
            setItemClickListener(botTitle)
        }

        fun bindQuizNotification(
            imagePath: Int,
            subTitle: String,
            locationName: String
        ) {
            //itemView.card_image.setImageResource(imagePath)
            itemView.location_name.text = locationName
            itemView.card_subtitle.text = subTitle
            itemView.quiz_inner_container.startBounceAnimation()
            setQuizItemClickNotification()
        }

        fun bindLevelNotification(
            imagePath: Int,
            subTitle: String
        ) {
            //itemView.card_image.setImageResource(imagePath)
            itemView.card_subtitle.text = subTitle
            //itemView.showBounceAnimation()
        }

        fun bindFlashcardsNotification() {

        }

        fun bindGamesNotification() {

        }

        private fun setQuizItemClickNotification() {
            itemView.quiz_inner_container.setOnClickListener {
                val intent = Intent(context, GamesActivity::class.java)
                intent.putExtra("gameType", "SpeechGame")
                intent.putExtra("shouldShowGameOptions", false)
                intent.putExtra("botTitle", "Doctor Susan")
                context.startActivity(intent)
            }
        }

        private fun setItemClickListener(botTitle: String) {
            itemView.full_card_launch_button.setOnClickListener {
                val intent = Intent(context, BotStoryActivityLatest::class.java)
                var botTitleNew = botTitle
                if (botTitleNew == "Doctor Hum-Vee") {
                    botTitleNew = "Doctor Susan"
                }
                intent.putExtra("botStoryTitle", botTitleNew)
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                context.startActivity(intent)
            }
        }

        private fun View.startBounceAnimation() {
            this.startAnimation(
                AnimationUtils.loadAnimation(context, R.anim.bounce)
            )
        }
    }
}