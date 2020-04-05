package com.example.chatter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_all_easter_eggs.*


class AllEasterEggsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_all_easter_eggs, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpRecycler()
        setUpExitButton()
    }

    private fun setUpExitButton() {
        all_easter_eggs_exit.setOnClickListener {
            fragmentManager?.popBackStack()
        }
    }

    private fun setUpRecycler() {
        easter_egg_recyler.layoutManager = LinearLayoutManager(context)
        val pointsArray = arrayListOf<Long>(400, 300, 1000, 100)
        val messageArray = arrayListOf(
            "Make Paul happy",
            "Make Paul mad",
            "Make Paul interested",
            "Make Paul excited"
        )
        context?.let {
            val eggAdapter = AllEasterEggAdapter(it, pointsArray, messageArray)
            easter_egg_recyler.adapter = eggAdapter
            easter_egg_recyler.addItemDecoration(
                DividerItemDecoration(
                    it,
                    DividerItemDecoration.HORIZONTAL
                )
            )
        }
    }
}
