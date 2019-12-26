package de.halfbit.edgetoedge.sample.examples

import android.os.Bundle
import android.view.View
import de.halfbit.edgetoedge.Edge
import de.halfbit.edgetoedge.edgeToEdge
import de.halfbit.edgetoedge.sample.BaseFragment
import de.halfbit.edgetoedge.sample.R
import de.halfbit.edgetoedge.sample.commons.ImagesAdapter
import kotlinx.android.synthetic.main.fragment_toolbar_with_scrollable_content.*

class ToolbarWithScrollableContentFragment : BaseFragment() {
    override val layoutId: Int get() = R.layout.fragment_toolbar_with_scrollable_content

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        edgeToEdge {
            appbar.fit { Edge.Top }
            recycler.fit { Edge.Bottom }
        }

        recycler.adapter = ImagesAdapter(requireContext())
    }
}
