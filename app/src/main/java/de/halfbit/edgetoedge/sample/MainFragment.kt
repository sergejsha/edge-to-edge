package de.halfbit.edgetoedge.sample

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import de.halfbit.edgetoedge.Edge
import de.halfbit.edgetoedge.edgeToEdge
import kotlinx.android.synthetic.main.fragment_main.*

class MainFragment : BaseFragment() {
    override val layoutId: Int get() = R.layout.fragment_main

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        edgeToEdge {
            appbar.fit { Edge.Top }
            recycler.fit { Edge.Bottom }
        }

        recycler.adapter = MainAdapter { createFragment ->
            requireActivity()
                .supportFragmentManager
                .beginTransaction()
                .replace(R.id.container, createFragment())
                .addToBackStack(null)
                .commit()
        }
    }
}

abstract class BaseFragment : Fragment() {

    protected abstract val layoutId: Int

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(layoutId, container, false)
}