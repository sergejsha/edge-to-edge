package de.halfbit.edgetoedge.sample.examples.issue4

import android.os.Bundle
import android.view.View
import de.halfbit.edgetoedge.Edge
import de.halfbit.edgetoedge.edgeToEdge
import de.halfbit.edgetoedge.sample.BaseFragment
import de.halfbit.edgetoedge.sample.R
import kotlinx.android.synthetic.main.fragment_main.*

class Issue4SharedFragment : BaseFragment() {
    override val layoutId: Int get() = R.layout.fragment_issue4_shared

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        edgeToEdge {
            appbar.fit { Edge.TopArc }
        }
    }
}
