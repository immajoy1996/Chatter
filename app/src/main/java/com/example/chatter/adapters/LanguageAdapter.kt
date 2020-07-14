package com.example.chatter.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chatter.R
import com.example.chatter.interfaces.CategorySelectionInterface
import com.example.chatter.interfaces.LanguageSelectedInterface
import kotlinx.android.synthetic.main.nations_item_view.view.*

class LanguageAdapter(
    val context: Context,
    var nations: ArrayList<String>,
    var flags: ArrayList<Int>,
    var languageSelectedInterface: LanguageSelectedInterface? = null,
    var categorySelectionInterface: CategorySelectionInterface? = null
) :
    RecyclerView.Adapter<LanguageAdapter.LanguageViewHolder>() {
    private var selectedPos = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LanguageViewHolder {
        return LanguageViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.nations_item_view,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: LanguageViewHolder, position: Int) {
        val nation = nations[position]
        val flagImg = flags[position]
        holder.itemView.nations_item_layout.setOnClickListener {
            selectLanguageItem(position)
        }
        holder.bind(nation, flagImg, position)
    }

    private fun selectLanguageItem(position: Int) {
        selectedPos = position
        languageSelectedInterface?.onLanguageSelected(nations[selectedPos])
        categorySelectionInterface?.onCategorySelected(nations[selectedPos])
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = nations.size

    inner class LanguageViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(nation: String, flagResId: Int, position: Int) {
            itemView.country.text = nation
            itemView.flagImg.setImageResource(flagResId)
            if (selectedPos == position) {
                itemView.language_selector_green_check.visibility = View.VISIBLE
                itemView.nations_item_layout.setBackgroundResource(R.drawable.nation_item_view_background_enabled)
            } else {
                itemView.language_selector_green_check.visibility = View.GONE
                itemView.nations_item_layout.setBackgroundResource(R.drawable.nation_item_view_background)
            }
        }
    }
}