package org.wildaid.ofish.ui.search.simple

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.item_search_simple.view.*
import org.wildaid.ofish.R
import org.wildaid.ofish.ui.search.base.BaseSearchAdapter
import org.wildaid.ofish.ui.search.base.BaseViewHolder

class SimpleSearchAdapter(itemListener: (String) -> Unit) : BaseSearchAdapter<String>(itemListener) {
    override fun createHolder(inflater: LayoutInflater, parent: ViewGroup, viewType: Int) =
        SimpleHolder(inflater.inflate(R.layout.item_search_simple, parent, false))

    class SimpleHolder(view: View) : BaseViewHolder<String>(view) {
        override fun bindItem(item: String) {
            itemView.item_title.text = item
        }
    }
}