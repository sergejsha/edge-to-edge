package de.halfbit.edgetoedge.sample

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView

class MainAdapter(val onItemClicked: OnItemClicked) : RecyclerView.Adapter<ItemViewHolder>() {

    private var items = listOf(
        Item("Option 1") { MainFragment() },
        Item("Option 2") { MainFragment() },
        Item("Option 3") { MainFragment() },
        Item("Option 4") { MainFragment() },
        Item("Option 5") { MainFragment() },
        Item("Option 6") { MainFragment() },
        Item("Option 7") { MainFragment() },
        Item("Option 8") { MainFragment() },
        Item("Option 9") { MainFragment() },
        Item("Option 10") { MainFragment() },
        Item("Option 11") { MainFragment() },
        Item("Option 12") { MainFragment() },
        Item("Option 13") { MainFragment() },
        Item("Option 14") { MainFragment() }
    )

    override fun getItemCount(): Int = items.size
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder =
        ItemViewHolder(
            itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_main_item, parent, false),
            onItemClicked = onItemClicked
        )

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(items[position])
    }
}

class Item(
    val name: String,
    val factory: FragmentFactory
)

class ItemViewHolder(
    itemView: View, val onItemClicked: OnItemClicked
) : RecyclerView.ViewHolder(itemView) {
    fun bind(item: Item) {
        val textView = itemView as TextView
        textView.text = item.name
        textView.setOnClickListener { onItemClicked(item.factory) }
    }
}

typealias FragmentFactory = () -> Fragment
typealias OnItemClicked = (FragmentFactory) -> Unit