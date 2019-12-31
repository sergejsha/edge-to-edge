package de.halfbit.edgetoedge.sample.examples

import android.os.Bundle
import android.view.View
import android.view.animation.AnticipateOvershootInterpolator
import androidx.annotation.LayoutRes
import androidx.constraintlayout.widget.ConstraintSet
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import com.bumptech.glide.Glide
import de.halfbit.edgetoedge.Adjustment
import de.halfbit.edgetoedge.Edge
import de.halfbit.edgetoedge.edgeToEdge
import de.halfbit.edgetoedge.sample.BaseFragment
import de.halfbit.edgetoedge.sample.R
import de.halfbit.edgetoedge.sample.commons.QUALITY
import kotlinx.android.synthetic.main.fragment_constraint_layout_transition.*

class ConstraintLayoutTransitionsFragment : BaseFragment() {

    private var collapse = false
    private val transition = ChangeBounds().apply {
        interpolator = AnticipateOvershootInterpolator(1.0f)
        duration = 700
    }

    override val layoutId: Int = R.layout.fragment_constraint_layout_transition
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Glide.with(requireContext()).load(IMAGE).into(image)

        edgeToEdge {
            tapToExtend.fit {
                adjustment = Adjustment.Margin
                Edge.Bottom
            }
            description.fit { Edge.Bottom }
        }

        tapToExtend.setOnClickListener {
            if (collapse) {
                transitionInto(R.layout.fragment_constraint_layout_transition)
                // fixme: replace edgeToEdge call with requestApplyInsets() call
                edgeToEdge { }
            } else {
                transitionInto(R.layout.fragment_constraint_layout_transition_expanded)
            }
            collapse = collapse.not()
        }
    }

    private fun transitionInto(@LayoutRes layoutId: Int) {
        ConstraintSet().apply {
            clone(context, layoutId)
            TransitionManager.beginDelayedTransition(container, transition)
            applyTo(container)
        }
    }
}

private const val IMAGE = "https://images.unsplash.com/photo-1552840253-b19267dbe6fc?$QUALITY"
