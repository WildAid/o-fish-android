package org.wildaid.ofish.ui.base

import android.content.Context
import android.graphics.Canvas
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView

class ItemDivider(context: Context, orientation: Int) :
    DividerItemDecoration(context, orientation) {

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        if (parent.adapter?.itemCount == 1) return
        super.onDraw(c, parent, state)
    }
}