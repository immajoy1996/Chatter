package com.example.chatter.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.chatter.R
import kotlinx.android.synthetic.main.fragment_loading_animated.*

class LoadingAnimatedFragment : BaseFragment() {
    private var message: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_loading_animated, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpLoadingMessage()
    }

    private fun setUpLoadingMessage() {
        message?.let {
            loading_typewriter_message.text = it
        }
    }

    companion object {
        fun newInstance(msg: String): LoadingAnimatedFragment {
            val fragment = LoadingAnimatedFragment()
            fragment.message = msg
            return fragment
        }
    }
}