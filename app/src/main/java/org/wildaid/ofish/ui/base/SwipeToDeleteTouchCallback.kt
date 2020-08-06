package org.wildaid.ofish.ui.base

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import org.wildaid.ofish.R

/**
 * To use this class you need to write this code:
 *
 *   ItemTouchHelper(SwipeToDeleteTouchCallback(requireContext(), positionNotToRemove) {
 *       hideKeyboard()
 *       fragmentViewModel.removeNote(it)
 *   }).attachToRecyclerView(notes_recycler)
 */

class SwipeToDeleteTouchCallback(
    val context: Context,
    val noSwipePositions: Array<Int> = emptyArray(),
    val onSwipeListener: (Int) -> Unit
) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

    val icon = ContextCompat.getDrawable(context, R.drawable.ic_delete)
    val background = ColorDrawable(context.getColor(R.color.background_screen_with_cards))
    val handler = Handler()

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        handler.post {
            onSwipeListener.invoke(viewHolder.adapterPosition)
        }
    }

    override fun getSwipeDirs(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        if (viewHolder.adapterPosition in noSwipePositions) {
            return 0
        }

        return super.getSwipeDirs(recyclerView, viewHolder)
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)

        val itemView = viewHolder.itemView
        val backgroundCornerOffset = 20
        val iconMargin = (itemView.height - icon!!.intrinsicHeight) / 2
        val iconTop = itemView.top + (itemView.height - icon.intrinsicHeight) / 2
        val iconBottom = iconTop + icon.intrinsicHeight

        if (dX > 0) { // Swiping to the right
            val iconLeft = itemView.left + iconMargin + icon.intrinsicWidth
            val iconRight = itemView.left + iconMargin
            icon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
            background.setBounds(
                itemView.left, itemView.top,
                itemView.left + dX.toInt() + backgroundCornerOffset,
                itemView.bottom
            )
        } else if (dX < 0) { // Swiping to the left
            val iconLeft = itemView.right - iconMargin - icon.intrinsicWidth
            val iconRight = itemView.right - iconMargin
            icon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
            background.setBounds(
                itemView.right + dX.toInt() - backgroundCornerOffset,
                itemView.top, itemView.right, itemView.bottom
            )
        } else { // view is unSwiped
            background.setBounds(0, 0, 0, 0)
        }

        background.draw(c)
        icon.draw(c)
    }
}