package de.halfbit.edgetoedge.sample.examples.issue4

import android.content.Intent
import android.os.Bundle
import android.view.View
import de.halfbit.edgetoedge.Edge
import de.halfbit.edgetoedge.edgeToEdge
import de.halfbit.edgetoedge.sample.BaseFragment
import de.halfbit.edgetoedge.sample.R
import kotlinx.android.synthetic.main.fragment_issue4_launcher.*
import kotlinx.android.synthetic.main.fragment_main.appbar

class Issue4LauncherFragment : BaseFragment() {
    override val layoutId: Int get() = R.layout.fragment_issue4_launcher

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        edgeToEdge {
            appbar.fit { Edge.TopArc }
        }

        button1.setOnClickListener {
            startActivity(Intent(context, Issue4Activity::class.java))
        }

        button2.setOnClickListener {
            startActivity(Intent(context, Issue4CoordinatorActivity::class.java))
        }
    }
}
