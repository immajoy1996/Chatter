package com.example.chatter.adapters

import android.graphics.Rect
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class BotGridItemDecoration(horizontalSpace: Int, verticalSpace: Int) :
    RecyclerView.ItemDecoration() {
    private var horizontalSpacing: Int? = null
    private var verticalSpacing: Int? = null
    private var pos: Int? = null

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
        pos = parent.getChildAdapterPosition(view)
        Log.d("spacing ", pos.toString())
        pos?.let {
            if(horizontalSpacing != null && verticalSpacing != null) {
                if ((it % 3) > 0) {
                    outRect.set(horizontalSpacing as Int, verticalSpacing as Int, 0, 0)
                } else {
                    outRect.set(0,verticalSpacing as Int,0,0)
                }
            }
        }
    }
}