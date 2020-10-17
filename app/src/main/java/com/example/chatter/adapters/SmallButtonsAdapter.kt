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
import com.example.chatter.interfaces.SmallIconClickInterface
import com.example.chatter.ui.activity.DashboardActivity
import kotlinx.android.synthetic.main.bot_layout.view.*
import kotlinx.android.synthetic.main.fragment_story_board_one.*
import kotlinx.android.synthetic.main.small_item_view.view.*


class SmallButtonsAdapter(
    val context: Context,
    var imageList: ArrayList<Int>,
    var onSmallIconClickInterface: SmallIconClickInterface
) :
    RecyclerView.Adapter<SmallButtonsAdapter.SmallButtonsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SmallButtonsViewHolder {
        return SmallButtonsViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.small_item_view,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: SmallButtonsViewHolder, position: Int) {
        val resId = imageList[position]
        holder.bind(resId)
    }

    override fun getItemCount(): Int = imageList.size

    inner class SmallButtonsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private var icon: ImageView? = null

        init {
            icon = view.small_item_icon
        }

        fun bind(resId: Int) {
            icon?.setImageResource(resId)
            setOnClickListener(resId)
        }

        fun setOnClickListener(resId: Int) {
            icon?.setOnClickListener {
                onSmallIconClickInterface.onSmallIconClick(resId)
            }
        }
    }
}