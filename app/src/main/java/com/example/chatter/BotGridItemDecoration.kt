package com.example.chatter

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class BotGridItemDecoration(space: Int) : RecyclerView.ItemDecoration() {
    private var spacing: Int? = null

    init {
        spacing = space
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {

        spacing?.let {
            outRect.left = it
            outRect.top=it
        }
    }
}