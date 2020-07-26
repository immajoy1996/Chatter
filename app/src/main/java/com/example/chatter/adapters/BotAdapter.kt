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
import com.example.chatter.ui.activity.DashboardActivity
import kotlinx.android.synthetic.main.bot_layout.view.*
import kotlinx.android.synthetic.main.fragment_story_board_one.*


class BotAdapter(
    val context: Context,
    var imageList: ArrayList<String>,
    var botTitles: ArrayList<String>,
    var isGuestModeEnabled: ArrayList<Boolean>
) :
    RecyclerView.Adapter<BotAdapter.BotViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BotViewHolder {
        return BotViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.bot_layout,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: BotViewHolder, position: Int) {
        val imagePath = imageList[position]
        val title = botTitles[position]
        var isAllowedInGuestMode = isGuestModeEnabled[position]
        holder.bind(imagePath, title, isAllowedInGuestMode)
    }

    override fun getItemCount(): Int = botTitles.size

    inner class BotViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private var botImage: ImageView? = null
        private var botLockedImage: ImageView? = null
        private var botTitle: TextView? = null
        private var botLevel: TextView? = null
        private var botLayout: ConstraintLayout? = null

        init {
            botImage = view.image
            botTitle = view.title
            botLevel = view.bot_level
            botLayout = view.bot_item_inner_layout
            botLockedImage = view.bot_locked_image
        }

        fun bind(imagePath: String, title: String, isEnabled: Boolean) {
            val aniShakeForever = AnimationUtils.loadAnimation(
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
            botImage?.startAnimation(animPicked)
            botImage?.let {
                Glide.with(context)
                    .load(imagePath)
                    .into(it)
            }
            botTitle?.text = title
            if (!isEnabled) {
                itemView.isClickable = false
                botLockedImage?.visibility = View.VISIBLE
                botLayout?.setBackgroundResource(R.drawable.profile_item_enabled)

            } else {
                itemView.setClickable(true)
                setOnClickListener(imagePath)
                botLockedImage?.visibility = View.GONE
                botLayout?.setBackgroundResource(R.drawable.profile_item_disabled)
            }
        }

        fun setOnClickListener(imagePath: String) {
            itemView.setOnClickListener {
                val bounceAni =
                    AnimationUtils.loadAnimation(context, R.anim.bounce)
                // Use bounce interpolator with amplitude 0.2 and frequency 20
                // Use bounce interpolator with amplitude 0.2 and frequency 20
                val interpolator = MyBounceInterpolator(0.2, 20.0)
                bounceAni.interpolator = interpolator
                itemView.startAnimation(bounceAni)
                bounceAni.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(arg0: Animation) {}
                    override fun onAnimationRepeat(arg0: Animation) {}
                    override fun onAnimationEnd(arg0: Animation) {
                        (context as? DashboardActivity)?.onBotItemClicked(
                            imagePath,
                            botTitle?.text.toString()
                        )
                    }
                })
            }
        }

    }
}