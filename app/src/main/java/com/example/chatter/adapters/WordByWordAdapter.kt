package com.example.chatter.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.chatter.R
import com.example.chatter.interfaces.WordByWordInterface
import kotlinx.android.synthetic.main.word_by_word_item_view.view.*

class WordByWordAdapter(
    val context: Context,
    var words: ArrayList<String>,
    var translations: ArrayList<String>,
    var shouldShowTranslation: ArrayList<Boolean>,
    var wordByWordInterface: WordByWordInterface
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
        holder.bind(position)
    }

    override fun getItemCount(): Int = words.size

    fun changeTranslation(pos: Int, text: String) {
        translations.set(pos, text)
    }

    fun showTranslation(pos: Int) {
        shouldShowTranslation[pos] = true
    }

    fun refreshRecycler() {
        notifyDataSetChanged()
    }

    inner class WordByWordViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(position: Int) {
            itemView.word_by_word_english_word.text = words[position]
            itemView.word_by_word_translation.text = translations[position]
            if (shouldShowTranslation[position]) {
                itemView.word_by_word_translation.visibility = View.VISIBLE
                itemView.word_by_word_english_word.visibility = View.GONE
            } else {
                itemView.word_by_word_translation.visibility = View.GONE
                itemView.word_by_word_english_word.visibility = View.VISIBLE
            }
            setItemViewClick(position)
        }

        private fun setItemViewClick(pos: Int) {
            itemView.setOnClickListener {
                val englishWord = itemView.word_by_word_english_word.text.toString()
                val translation = itemView.word_by_word_translation.text.toString()
                if (itemView.word_by_word_english_word.visibility == View.VISIBLE) {
                    itemView.word_by_word_translation.visibility = View.VISIBLE
                    itemView.word_by_word_english_word.visibility = View.GONE
                    if (translation.isEmpty()) {
                        wordByWordInterface.onItemTapped(englishWord, pos)
                    }
                } else {
                    itemView.word_by_word_translation.visibility = View.GONE
                    itemView.word_by_word_english_word.visibility = View.VISIBLE
                }
            }
        }
    }
}