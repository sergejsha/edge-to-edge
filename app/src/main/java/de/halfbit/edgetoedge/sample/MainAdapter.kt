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
        Item(
            nameId = R.string.splash,
            onClick = OnClick.CreateFragment { SplashScreenFragment() }
        ),
        Item(
            nameId = R.string.toolbar,
            onClick = OnClick.CreateFragment { ToolbarWithScrollableContentFragment() }
        ),
        Item(
            nameId = R.string.toolbar_fab,
            onClick = OnClick.CreateFragment { ToolbarWithScrollableContentAndFabFragment() }
        ),
        Item(
            nameId = R.string.toolbar_viewpager,
            onClick = OnClick.CreateFragment { ToolbarWithViewpagerFragment() }
        ),
        Item(
            nameId = R.string.bottom_sheet_dialog,
            onClick = OnClick.CreateFragment { BottomSheetDialogFragment() }
        ),
        Item(
            nameId = R.string.constraint_layout_transitions,
            onClick = OnClick.CreateFragment { ConstraintLayoutTransitionsFragment() }
        ),
        Item(
            nameId = R.string.full_screen_activity,
            onClick = OnClick.CreateActivity(FullScreenActivity::class.java)
        )
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
