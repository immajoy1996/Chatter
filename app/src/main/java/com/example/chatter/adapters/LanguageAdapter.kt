package com.example.chatter.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chatter.R
import com.example.chatter.interfaces.CategorySelectionInterface
import com.example.chatter.interfaces.LanguageSelectedInterface
import kotlinx.android.synthetic.main.bot_layout.view.*
import kotlinx.android.synthetic.main.nations_item_view.view.*

class LanguageAdapter(
    val context: Context,
    var nations: ArrayList<String>,
    var flags: ArrayList<String>,
    var languageSelectedInterface: LanguageSelectedInterface? = null,
    var categorySelectionInterface: CategorySelectionInterface? = null
) :
    RecyclerView.Adapter<LanguageAdapter.LanguageViewHolder>() {
    private var selectedPos = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LanguageViewHolder {
        return LanguageViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.bot_item_original_layout,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: LanguageViewHolder, position: Int) {
        val nation = nations[position]
        val flagImg = flags[position]
        holder.itemView.bot_item_layout.setOnClickListener {
            selectLanguageItem(position)
        }
        holder.bind(nation, flagImg, position)
    }

    private fun selectLanguageItem(position: Int) {
        selectedPos = position
        languageSelectedInterface?.onLanguageSelected(nations[selectedPos], flags[selectedPos])
        categorySelectionInterface?.onCategorySelected(nations[selectedPos])
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = nations.size

    inner class LanguageViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(nation: String, flagImg: String, position: Int) {
            itemView.title.text = nation
            //itemView.flagImg.setImageResource(flagResId)
            itemView.image?.let {
                Glide.with(context)
                    .load(flagImg)
                    .into(it)
            }
            if (selectedPos == position) {
                itemView.green_check.visibility = View.VISIBLE
                itemView.bot_item_inner_layout.setBackgroundResource(R.drawable.profile_item_enabled)
            } else {
                itemView.green_check.visibility = View.GONE
                itemView.bot_item_inner_layout.setBackgroundResource(R.drawable.profile_item_disabled)
            }
        }
    }
}