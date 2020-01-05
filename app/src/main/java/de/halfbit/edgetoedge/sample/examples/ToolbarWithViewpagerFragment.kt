package de.halfbit.edgetoedge.sample.examples

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import de.halfbit.edgetoedge.Edge
import de.halfbit.edgetoedge.edgeToEdge
import de.halfbit.edgetoedge.sample.BaseFragment
import de.halfbit.edgetoedge.sample.R
import kotlinx.android.synthetic.main.fragment_toolbar_with_viewpager.*
import kotlinx.android.synthetic.main.fragment_toolbar_with_viewpager_page.*
import kotlinx.android.synthetic.main.fragment_toolbar_with_viewpager_page.container as page

class ToolbarWithViewpagerFragment : BaseFragment() {
    override val layoutId: Int get() = R.layout.fragment_toolbar_with_viewpager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewpager.adapter = PagesAdapter(this)

        edgeToEdge {
            appbar.fit { Edge.Left + Edge.Top + Edge.Right }
        }
    }
}

class PageFragment : BaseFragment() {
    override val layoutId: Int get() = R.layout.fragment_toolbar_with_viewpager_page
    private val color: String get() = requireArguments().getString(COLOR) as String
    private val index: Int get() = requireArguments().getInt(INDEX)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        page.setBackgroundColor(Color.parseColor(color))
        indexText.text = (index + 1).toString()

        edgeToEdge {
            bottomText.fit { Edge.Bottom }
        }
    }

    companion object {
        private const val COLOR = "color"
        private const val INDEX = "index"
        fun create(color: String, index: Int) = PageFragment().apply {
            arguments = Bundle().apply {
                putString(COLOR, color)
                putInt(INDEX, index)
            }
        }
    }
}

private class PagesAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    private val colors = listOf(
        "#FFE0B2", "#E1BEE7", "#C5CAE9", "#DCEDC8", "#B3E5FC"
    )

    override fun getItemCount(): Int = 5
    override fun createFragment(position: Int): Fragment =
        PageFragment.create(colors[position % colors.size], position)
}