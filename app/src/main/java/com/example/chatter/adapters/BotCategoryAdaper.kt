package com.example.chatter.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chatter.R
import kotlinx.android.synthetic.main.bot_category_item_layout.view.*

class BotCategoryAdapter(
    val context: Context,
    var categories: ArrayList<String>
) :
    RecyclerView.Adapter<BotCategoryAdapter.BotCategoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BotCategoryViewHolder {
        return BotCategoryViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.bot_category_item_layout,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: BotCategoryViewHolder, position: Int) {
        holder.bind(categories[position])
    }

    override fun getItemCount(): Int = categories.size

    inner class BotCategoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(categoryName: String) {
            itemView.category_name.text = categoryName
        }

        fun setOnClickListener() {

        }

    }
}