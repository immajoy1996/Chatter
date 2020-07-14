package com.example.chatter.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chatter.interfaces.ProfileClickInterface
import com.example.chatter.R
import kotlinx.android.synthetic.main.profile_item_layout.view.*

class ProfileAdapter(
    val context: Context,
    var imageList: ArrayList<String>,
    var profileClickInterface: ProfileClickInterface
) :
    RecyclerView.Adapter<ProfileAdapter.ProfileViewHolder>() {
    var selectedPos = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileViewHolder {
        return ProfileViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.profile_item_layout,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ProfileViewHolder, position: Int) {
        val image = imageList[position]
        holder.bind(image, position)
        holder.itemView.setOnClickListener {
            if (holder.itemView.green_check.visibility == View.VISIBLE) {
                selectedPos = -1
                notifyDataSetChanged()
            } else {
                selectedPos = position
                profileClickInterface.onProfileClicked(imageList[selectedPos])
                notifyDataSetChanged()
            }
        }
    }

    override fun getItemCount(): Int = imageList.size

    inner class ProfileViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private var botImage: ImageView? = null

        init {
            botImage = view.profile_image
        }

        fun bind(image: String, position: Int) {
            botImage?.let {
                Glide.with(context)
                    .load(image)
                    .into(it)
            }
            if (selectedPos == position) {
                itemView.profile_item_inner_layout.setBackgroundResource(R.drawable.profile_item_enabled)
                itemView.green_check.visibility = View.VISIBLE
            } else {
                itemView.profile_item_inner_layout.setBackgroundResource(R.drawable.profile_item_disabled)
                itemView.green_check.visibility = View.GONE
            }
        }
    }
}