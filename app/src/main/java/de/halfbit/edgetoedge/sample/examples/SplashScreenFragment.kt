package de.halfbit.edgetoedge.sample.examples

import android.os.Bundle
import android.view.View
import de.halfbit.edgetoedge.Edge
import de.halfbit.edgetoedge.edgeToEdge
import de.halfbit.edgetoedge.sample.BaseFragment
import de.halfbit.edgetoedge.sample.R
import kotlinx.android.synthetic.main.fragment_splash_screen.*

class SplashScreenFragment : BaseFragment() {
    override val layoutId: Int get() = R.layout.fragment_splash_screen

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        edgeToEdge {
            container.fit { Edge.All }
        }
    }
}
