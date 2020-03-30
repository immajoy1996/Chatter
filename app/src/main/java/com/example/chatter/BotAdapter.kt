package com.example.chatter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.firebase.ui.storage.images.FirebaseImageLoader
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.bot_layout.view.*


class BotAdapter(
    val context: Context,
    var imageList: ArrayList<String>,
    var botTitles: ArrayList<String>
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
        holder.bind(imagePath, title)
    }

    override fun getItemCount(): Int = botTitles.size

    inner class BotViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private var botImage: ImageView? = null
        private var botTitle: TextView? = null

        init {
            botImage = view.image
            botTitle = view.title
        }

        fun bind(imagePath: String, title: String) {
            botImage?.let {
                Glide.with(context)
                    .load(imagePath)
                    .into(it)
            }
            botTitle?.text = title
            setOnClickListener()
        }

        fun setOnClickListener() {
            itemView.setOnClickListener { (context as? DashboardActivity)?.onBotItemClicked(botTitle?.text.toString()) }
        }

    }

    companion object {
    }
}