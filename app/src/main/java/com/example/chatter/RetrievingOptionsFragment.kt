package com.example.chatter

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_retrieving_options.*

class RetrievingOptionsFragment : Fragment() {

    private var loadingText: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_retrieving_options, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpLoadingText()
    }

    fun setUpLoadingText() {
        retrieving_options_text.text = loadingText
    }

    companion object {
        fun newInstance(text: String): RetrievingOptionsFragment {
            var fragment = RetrievingOptionsFragment()
            fragment.loadingText = text
            return fragment
        }
    }

}
