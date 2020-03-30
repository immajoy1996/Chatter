package com.example.chatter

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.bot_layout.*
import kotlinx.android.synthetic.main.fragment_easter_egg.*

class EasterEggFragment : Fragment() {
    var message: String? = null
    var points: Long? = null
    var imageSrc: String? = null
    private lateinit var database: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_easter_egg, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        database = FirebaseDatabase.getInstance().reference
        setUpButtons()
        setUpViews()
    }

    private fun setUpButtons() {
        exit.setOnClickListener {
            (activity as? ChatterActivity)?.let {
                it.closeEasterEggFragment()
                //it.loadOptionsMenu()
            }
        }
    }

    private fun setUpViews() {
        easter_egg_message.text = message
        total_points.text = "+".plus(points.toString())
        context?.let {
            Glide.with(it)
                .load(imageSrc)
                .into(emotion)
        }
    }

    companion object {
        fun newInstance(message: String, points: Long, imageSrc: String): EasterEggFragment {
            val fragment = EasterEggFragment()
            fragment.message = message
            fragment.points = points
            fragment.imageSrc = imageSrc
            return fragment
        }
    }
}
