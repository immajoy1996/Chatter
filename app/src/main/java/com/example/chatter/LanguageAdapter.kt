package com.example.chatter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.nations_item_view.view.*

class LanguageAdapter(
    val context: Context,
    var nations: ArrayList<String>,
    var flags: ArrayList<Int>
) :
    RecyclerView.Adapter<LanguageAdapter.LanguageViewHolder>() {

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
        holder.itemView.setOnClickListener {
            showFullLanguageList()
        }
        holder.bind(nation, flagImg)
    }

    private fun showFullLanguageList() {
        nations = arrayListOf("Spanish", "French", "German")
        flags = arrayListOf(R.drawable.spanishflag, R.drawable.frenchflag, R.drawable.germanflag)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = nations.size

    inner class LanguageViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(nation: String, flagResId: Int) {
            itemView.country.text = nation
            itemView.flagImg.setImageResource(flagResId)
        }
    }
}