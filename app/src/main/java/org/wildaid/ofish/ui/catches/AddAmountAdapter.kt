package org.wildaid.ofish.ui.catches

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_search_simple.view.*
import org.wildaid.ofish.R

class AddAmountAdapter(
    private val data: List<String>,
    private val onItemClicked: (String) -> Unit
) :
    RecyclerView.Adapter<AddAmountAdapter.AddAmountViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddAmountViewHolder {
        return AddAmountViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_search_simple, parent, false)
        )
    }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: AddAmountViewHolder, position: Int) =
        holder.bind(data[position])

    inner class AddAmountViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: String) = with(itemView) {
            itemView.item_title.text = item
            setOnClickListener {
                onItemClicked.invoke(item)
            }
        }
    }
}