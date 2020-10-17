package com.example.chatter.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chatter.R
import kotlinx.android.synthetic.main.user_level_item.view.*

class UserLevelsAdapter(
    val context: Context,
    var levelNames: ArrayList<String>,
    var levelImages: ArrayList<Int>
) :
    RecyclerView.Adapter<UserLevelsAdapter.UserLevelsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserLevelsViewHolder {
        return UserLevelsViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.user_level_item,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: UserLevelsViewHolder, position: Int) {
        val name = levelNames[position]
        val image = levelImages[position]
        holder.bind(name, image, position)
    }

    override fun getItemCount(): Int = levelNames.size

    inner class UserLevelsViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(name: String, image: Int, position: Int) {
            itemView.user_level_desc.text = name
            itemView.user_level_image.setImageResource(image)
            if (name.toLowerCase().contains("locked")) {
                itemView.user_level_image_layout.visibility = View.GONE
                itemView.lock_image.visibility = View.VISIBLE
                itemView.user_level_desc.visibility = View.GONE
            } else if (position == 0) {
                itemView.user_level_image_layout.visibility = View.VISIBLE
                itemView.lock_image.visibility = View.GONE
                itemView.user_level_desc.visibility = View.VISIBLE
            } else if (position > 0) {
                itemView.user_level_image_layout.visibility = View.VISIBLE
                itemView.lock_image.visibility = View.GONE
                itemView.user_level_desc.visibility = View.VISIBLE
                itemView.user_level_desc.setTextColor(Color.parseColor("#dcdcdc"))

            }
        }
    }
}