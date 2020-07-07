package com.example.chatter

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_create_easter_egg.*

class CreateEasterEggFragment : Fragment() {
    private val chatterActivity by lazy { activity as CreateChatActivity }
    var message: String? = null
    var points: Long? = null
    private lateinit var database: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_create_easter_egg, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        database = FirebaseDatabase.getInstance().reference
        //updateUserTotalScore()
        setUpButtons()
        //setUpViews()
    }

    override fun onResume() {
        super.onResume()
        create_easter_egg_price.setHint("Enter price")
        create_easter_egg_price.setText("")
        create_easter_egg_message.setHint("Enter message")
        create_easter_egg_message.setText("")
    }

    private fun setUpButtons() {
        create_easter_egg_close_button.setOnClickListener {
            (activity as? CreateChatActivity)?.let {
                it.closeEasterEggFragment()
            }
        }
        create_easter_egg_submit_button.setOnClickListener {
            val eggMessage = create_easter_egg_message.text.toString()
            val eggPointsString = create_easter_egg_price.text.toString()
            val id = (Math.random() * MAX_EASTER_EGG_ID).toInt()
            if (eggMessage.isNotEmpty() && eggPointsString.isNotEmpty()) {
                val eggPointsInt = eggPointsString.toInt()
                database.child(chatterActivity.currentPath).child("EasterEgg")
                    .setValue(EasterEgg(id, eggMessage, eggPointsInt)).addOnSuccessListener {
                        Toast.makeText(context, "Update successful", Toast.LENGTH_LONG)
                            .show()
                    }.addOnFailureListener {
                        Toast.makeText(context, it.localizedMessage, Toast.LENGTH_LONG)
                            .show()
                    }
            } else {
                //apply wobble animation to empty views
            }
        }
    }

    private fun setUpViews() {
        //create_easter_egg_message.text = message
        //create_easter_egg_price.text = points.toString()
    }

    companion object {
        private const val MAX_EASTER_EGG_ID = 1000000000
    }
}
