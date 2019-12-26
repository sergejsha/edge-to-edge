package de.halfbit.edgetoedge.sample.commons

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import de.halfbit.edgetoedge.sample.R

class ImagesAdapter(context: Context) : RecyclerView.Adapter<ItemViewHolder>() {

    private val requestManager: RequestManager = Glide.with(context)

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
                R.layout.item_imageview, parent, false
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
