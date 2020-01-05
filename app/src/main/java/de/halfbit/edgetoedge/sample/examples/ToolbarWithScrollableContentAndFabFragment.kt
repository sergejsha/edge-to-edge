package de.halfbit.edgetoedge.sample.examples

import android.os.Bundle
import android.view.View
import com.google.android.material.snackbar.Snackbar
import de.halfbit.edgetoedge.Edge
import de.halfbit.edgetoedge.edgeToEdge
import de.halfbit.edgetoedge.sample.BaseFragment
import de.halfbit.edgetoedge.sample.R
import de.halfbit.edgetoedge.sample.commons.ImagesAdapter
import kotlinx.android.synthetic.main.fragment_toolbar_with_scrollable_content_and_fab.*

class ToolbarWithScrollableContentAndFabFragment : BaseFragment() {

    override val layoutId: Int get() = R.layout.fragment_toolbar_with_scrollable_content_and_fab

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        edgeToEdge {
            appbar.fit { Edge.Left + Edge.Top + Edge.Right }
            recycler.fit { Edge.Bottom }
            fab.fit { Edge.Right }
            bottomSpace.fit { Edge.Bottom }
        }

        fab.setOnClickListener {
            Snackbar.make(container, "Kaboom!", Snackbar.LENGTH_LONG).show()
        }

        recycler.adapter = ImagesAdapter(requireContext())
    }
}
