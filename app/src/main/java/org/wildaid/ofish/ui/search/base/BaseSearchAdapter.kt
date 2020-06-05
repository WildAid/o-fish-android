package org.wildaid.ofish.ui.search.base

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import org.wildaid.ofish.ui.base.AdapterDiffCallback

abstract class BaseSearchAdapter<T>(val itemListener: (T) -> Unit):
    RecyclerView.Adapter<BaseViewHolder<T>>() {

    protected val dataList: ArrayList<T> = ArrayList()

    abstract fun createHolder(
        inflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): BaseViewHolder<T>

    fun setItems(items: List<T>) {
        val diffResult = DiffUtil.calculateDiff(AdapterDiffCallback(items, dataList))
        diffResult.dispatchUpdatesTo(this)
        dataList.clear()
        dataList.addAll(items)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<T> {
        val inflater = LayoutInflater.from(parent.context)
        return createHolder(inflater, parent, viewType)
    }

    override fun getItemCount() = dataList.size

    override fun onBindViewHolder(holder: BaseViewHolder<T>, position: Int) {
        val item = dataList[position]
        holder.bindItem(item)
        holder.itemView.setOnClickListener { itemListener.invoke(item) }
    }
}

abstract class BaseViewHolder<T>(view: View) : RecyclerView.ViewHolder(view) {
    abstract fun bindItem(item: T)
}
