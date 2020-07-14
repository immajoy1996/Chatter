package com.example.chatter.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chatter.R
import kotlinx.android.synthetic.main.easter_egg_item_view.view.*


class AllEasterEggAdapter(
    val context: Context,
    var points: ArrayList<Long>,
    var messages: ArrayList<String>
) :
    RecyclerView.Adapter<AllEasterEggAdapter.EasterEggViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EasterEggViewHolder {
        return EasterEggViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.easter_egg_item_view,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: EasterEggViewHolder, position: Int) {
        val totalPoints = points[position]
        val title = messages[position]
        holder.bind(totalPoints, title)
    }

    override fun getItemCount(): Int = points.size

    inner class EasterEggViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(points: Long, title: String) {
            itemView.egg_points.setText("+".plus(points))
            itemView.egg_message.setText(title)
        }

    }
}