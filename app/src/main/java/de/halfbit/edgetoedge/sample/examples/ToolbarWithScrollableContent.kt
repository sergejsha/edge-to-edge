package de.halfbit.edgetoedge.sample.examples

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import de.halfbit.edgetoedge.Edge
import de.halfbit.edgetoedge.edgeToEdge
import de.halfbit.edgetoedge.sample.BaseFragment
import de.halfbit.edgetoedge.sample.R
import kotlinx.android.synthetic.main.fragment_toolbar_with_scrollable_content.*

class ToolbarWithScrollableContent : BaseFragment() {
    override val layoutId: Int get() = R.layout.fragment_toolbar_with_scrollable_content

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        edgeToEdge {
            appbar.fit { Edge.Top }
            recycler.fit { Edge.Bottom }
        }

        recycler.adapter = ImageAdapter(Glide.with(requireActivity()))
    }
}

class ImageAdapter(
    private val requestManager: RequestManager
) : RecyclerView.Adapter<ItemViewHolder>() {

    // All great pictures are referenced from https://unsplash.com/
    private var items = listOf(
        "https://images.unsplash.com/photo-1574270981993-f1df213562b3?$QUALITY",
        "https://images.unsplash.com/photo-1576078766417-80f4b730120c?$QUALITY",
        "https://images.unsplash.com/photo-1573743338941-39db12ef9b64?$QUALITY",
        "https://images.unsplash.com/photo-1571210059434-edf0dc48e414?$QUALITY",
        "https://images.unsplash.com/photo-1568021735466-efd8a4c435af?$QUALITY",
        "https://images.unsplash.com/photo-1568283096533-078a24930eb8?$QUALITY"
    )

    override fun getItemCount(): Int = items.size
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder =
        ItemViewHolder(
            itemView = LayoutInflater.from(parent.context).inflate(
                R.layout.fragment_toolbar_with_scrollable_content_item, parent, false
            ),
            requestManager = requestManager
        )

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(items[position])
    }
}

class ItemViewHolder(
    itemView: View, private val requestManager: RequestManager
) : RecyclerView.ViewHolder(itemView) {
    fun bind(item: String) {
        requestManager.load(item).into(itemView as ImageView)
    }
}

private const val QUALITY =
    "ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=crop&w=640&q=80"