package de.halfbit.edgetoedge.sample

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import de.halfbit.edgetoedge.sample.examples.*

class MainAdapter(
    private val onClick: (OnClick) -> Unit
) : RecyclerView.Adapter<ItemViewHolder>() {

    private var items = listOf(
        Item(R.string.splash, OnClick.CreateFragment { SplashScreenFragment() }),
        Item(R.string.toolbar,OnClick.CreateFragment { ToolbarWithScrollableContentFragment() }),
        Item(R.string.toolbar_fab, OnClick.CreateFragment { ToolbarWithScrollableContentAndFabFragment() }),
        Item(R.string.toolbar_viewpager, OnClick.CreateFragment { ToolbarWithViewpagerFragment() }),
        Item(R.string.bottom_sheet_dialog, OnClick.CreateFragment { BottomSheetDialogFragment() }),
        Item(R.string.constraint_layout_transitions, OnClick.CreateFragment { ConstraintLayoutTransitionsFragment() }),
        Item(R.string.full_screen_activity, OnClick.CreateActivity(FullScreenActivity::class.java))
    )

    override fun getItemCount(): Int = items.size
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder =
        ItemViewHolder(
            itemView = LayoutInflater.from(parent.context).inflate(
                R.layout.fragment_main_item, parent, false
            ),
            onClick = onClick
        )

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(items[position])
    }
}

class Item(
    val nameId: Int,
    val onClick: OnClick
)

sealed class OnClick {
    class CreateFragment(val createFragment: () -> Fragment) : OnClick()
    class CreateActivity(val activity: Class<out Activity>) : OnClick()
}

class ItemViewHolder(
    itemView: View,
    val onClick: (OnClick) -> Unit
) : RecyclerView.ViewHolder(itemView) {
    fun bind(item: Item) {
        val textView = itemView as TextView
        textView.setText(item.nameId)
        textView.setOnClickListener { onClick(item.onClick) }
    }
}
