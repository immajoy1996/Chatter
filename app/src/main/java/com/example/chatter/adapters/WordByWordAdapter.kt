package com.example.chatter.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chatter.R
import kotlinx.android.synthetic.main.word_by_word_item_view.view.*

class WordByWordAdapter(
    val context: Context,
    var words: ArrayList<String>,
    var translations: ArrayList<String>
) :
    RecyclerView.Adapter<WordByWordAdapter.WordByWordViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordByWordViewHolder {
        return WordByWordViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.word_by_word_item_view,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: WordByWordViewHolder, position: Int) {
        val word = words[position]
        val translation = translations[position]
        holder.bind(word, translation)
    }

    override fun getItemCount(): Int = words.size

    inner class WordByWordViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(word: String, translation: String) {
            itemView.word_by_word_english_word.text = word
            itemView.word_by_word_translation.text = translation
        }
    }
}