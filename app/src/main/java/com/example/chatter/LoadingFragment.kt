package com.example.chatter

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_loading.*

class LoadingFragment : Fragment() {
    private var loadingMessage: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_loading, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpLoadingMessage()
    }

    private fun setUpLoadingMessage() {
        loading_message.text = loadingMessage
    }

    companion object {
        fun newInstance(message: String): LoadingFragment {
            var fragment = LoadingFragment()
            fragment.loadingMessage = message
            return fragment
        }
    }
}
