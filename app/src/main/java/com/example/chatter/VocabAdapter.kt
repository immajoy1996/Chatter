package com.example.chatter

import android.content.Context
import android.media.MediaPlayer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.vocab_item_view.view.*

class VocabAdapter(
    val context: Context,
    var audioSrc: ArrayList<String>,
    var spanishWords: ArrayList<String>,
    var translations: ArrayList<String>,
    var transliterations: ArrayList<String>
) :
    RecyclerView.Adapter<VocabAdapter.VocabViewHolder>() {
    var mediaPlayer = MediaPlayer()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VocabViewHolder {
        return VocabViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.vocab_item_view,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: VocabViewHolder, position: Int) {
        val audio = audioSrc[position]
        val spanishWord = spanishWords[position]
        val translation = translations[position]
        val transliteration = transliterations[position]
        holder.bind(audio, spanishWord, translation, transliteration)
    }

    override fun getItemCount(): Int = spanishWords.size

    inner class VocabViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(audio: String, spanishWord: String, translation: String, transliteration: String) {
            itemView.spanish_word.text = spanishWord
            itemView.translation.text = translation
            itemView.transliteration.text = transliteration
            itemView.setOnClickListener {
                playMedia(audio)
            }
        }

        private fun playMedia(audio: String) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop()
                mediaPlayer.release()
            }
            mediaPlayer = MediaPlayer()
            mediaPlayer.setDataSource(audio)
            mediaPlayer.prepareAsync()
            mediaPlayer.setOnPreparedListener {
                mediaPlayer.start()
            }
        }
    }
}