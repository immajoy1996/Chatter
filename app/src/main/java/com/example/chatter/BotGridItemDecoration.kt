package com.example.chatter

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class BotGridItemDecoration(horizontalSpace: Int, verticalSpace: Int) :
    RecyclerView.ItemDecoration() {
    private var horizontalSpacing: Int? = null
    private var verticalSpacing: Int? = null

    init {
        horizontalSpacing = horizontalSpace
        verticalSpacing = verticalSpace
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {

        outRect.setEmpty()
        horizontalSpacing?.let {
            outRect.left = it
        }
        verticalSpacing?.let {
            outRect.top = it
        }
    }
}