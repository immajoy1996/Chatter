package com.example.chatter.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chatter.ui.activity.DashboardActivity
import com.example.chatter.R
import kotlinx.android.synthetic.main.bot_layout.view.*


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
            botImage?.let {
                Glide.with(context)
                    .load(imagePath)
                    .into(it)
            }
            botTitle?.text = title
            if (!isEnabled) {
                itemView.isClickable = false
                botLockedImage?.visibility = View.VISIBLE
                botLayout?.setBackgroundResource(R.drawable.bot_disabled_background)

            } else {
                itemView.setClickable(true)
                setOnClickListener(imagePath)
                botLockedImage?.visibility = View.GONE
                botLayout?.setBackgroundResource(R.drawable.gem_item_selector)
            }
        }

        fun setOnClickListener(imagePath: String) {
            itemView.setOnClickListener {
                (context as? DashboardActivity)?.onBotItemClicked(
                    imagePath,
                    botTitle?.text.toString()
                )
            }
        }

    }
}