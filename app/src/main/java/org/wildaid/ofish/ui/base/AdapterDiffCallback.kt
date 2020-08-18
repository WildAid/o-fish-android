package org.wildaid.ofish.ui.base

import androidx.recyclerview.widget.DiffUtil

class AdapterDiffCallback<T>(newList: List<T>, oldList: List<T>) : DiffUtil.Callback() {
    private var oldItems: List<T> = oldList
    private var newItems: List<T> = newList

    override fun getOldListSize(): Int {
        return oldItems.size
    }

    override fun getNewListSize(): Int {
        return newItems.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldItems[oldItemPosition] === newItems[newItemPosition]
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldItems[oldItemPosition] == (newItems[newItemPosition])
    }

    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        // You can return particular field for changed item.
        return super.getChangePayload(oldItemPosition, newItemPosition)
    }
}