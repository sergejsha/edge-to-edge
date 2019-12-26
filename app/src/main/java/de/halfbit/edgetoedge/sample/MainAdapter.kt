package de.halfbit.edgetoedge.sample

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import de.halfbit.edgetoedge.sample.examples.SplashScreenFragment
import de.halfbit.edgetoedge.sample.examples.ToolbarWithScrollableContentFragment
import de.halfbit.edgetoedge.sample.examples.ToolbarWithScrollableContentAndFabFragment
import de.halfbit.edgetoedge.sample.examples.ViewpagerFragment

class MainAdapter(private val onItemClicked: OnItemClicked) :
    RecyclerView.Adapter<ItemViewHolder>() {

    private var items = listOf(
        Item(R.string.splash) { SplashScreenFragment() },
        Item(R.string.toolbar) { ToolbarWithScrollableContentFragment() },
        Item(R.string.toolbar_fab) { ToolbarWithScrollableContentAndFabFragment() },
        Item(R.string.toolbar_viewpager) { ViewpagerFragment() }
    )

    override fun getItemCount(): Int = items.size
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder =
        ItemViewHolder(
            itemView = LayoutInflater.from(parent.context).inflate(
                R.layout.fragment_main_item, parent, false
            ),
            onItemClicked = onItemClicked
        )

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(items[position])
    }
}

class Item(
    @StringRes val nameId: Int,
    val factory: FragmentFactory
)

class ItemViewHolder(
    itemView: View, val onItemClicked: OnItemClicked
) : RecyclerView.ViewHolder(itemView) {
    fun bind(item: Item) {
        val textView = itemView as TextView
        textView.setText(item.nameId)
        textView.setOnClickListener { onItemClicked(item.factory) }
    }
}

typealias FragmentFactory = () -> Fragment
typealias OnItemClicked = (FragmentFactory) -> Unit
