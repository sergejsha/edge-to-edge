package de.halfbit.edgetoedge

import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Space
import androidx.core.view.ScrollingView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.WeakHashMap

@DslMarker
annotation class EdgeToEdgeDsl

@EdgeToEdgeDsl
class EdgeToEdgeBuilder(
    private val rootView: View,
    private val window: Window
) {

    private val edgeToEdge: EdgeToEdge =
        rootView.getTag(R.id.edgetoedge) as? EdgeToEdge
            ?: EdgeToEdge().also { rootView.setTag(R.id.edgetoedge, it) }

    fun View.fit(block: FittingBuilder.() -> Edge) {
        FittingBuilder(
            adjustment = if (this is Space) Adjustment.Height else Adjustment.Padding,
            clipToPadding = if (this is ScrollingView && this is ViewGroup) false else null,
            consumeInsets = false
        ).also { builder ->

            val edge = block(builder)
            val layoutMargin = layoutParams as? ViewGroup.MarginLayoutParams
            val fitting = Fitting(
                view = this,
                edge = edge,
                adjustment = builder.adjustment,
                clipToPadding = builder.clipToPadding,
                consumeInsets = builder.consumeInsets,
                paddingTop = paddingTop,
                paddingBottom = paddingBottom,
                marginTop = layoutMargin?.topMargin ?: 0,
                marginBottom = layoutMargin?.bottomMargin ?: 0
            )

            builder.clipToPadding?.let {
                check(this is ViewGroup) {
                    "'clipToPadding' can only be applied to ViewGroup, actual: $this"
                }
                clipToPadding = it
            }

            edgeToEdge.fittings[fitting.view] = fitting
        }
    }

    @PublishedApi
    internal fun build() {
        val edgeToEdgeEnabled = window.decorView.systemUiVisibility and
                (View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION) > 0
        if (!edgeToEdgeEnabled) window.setEdgeToEdgeFlags()

        rootView.onApplyWindowInsets { insets ->
            var consumeTop = false
            var consumeBottom = false
            for (fitting in edgeToEdge.fittings.values) {
                with(fitting) {
                    when (edge) {
                        Edge.Top -> {
                            consumeTop = consumeInsets
                            when (adjustment) {
                                Adjustment.Padding -> applyTopInsetAsPadding(insets)
                                Adjustment.Margin -> applyTopInsetAsMargin(insets)
                                Adjustment.Height -> applyTopInsetAsHeight(insets)
                            }
                        }
                        Edge.Bottom -> {
                            consumeBottom = consumeInsets
                            when (adjustment) {
                                Adjustment.Padding -> applyBottomInsetAsPadding(insets)
                                Adjustment.Margin -> applyBottomInsetAsMargin(insets)
                                Adjustment.Height -> applyBottomInsetAsHeight(insets)
                            }
                        }
                        Edge.TopBottom -> {
                            consumeTop = consumeInsets
                            consumeBottom = consumeInsets
                            when (fitting.adjustment) {
                                Adjustment.Padding -> applyTopAndBottomInsetsAsPadding(insets)
                                Adjustment.Margin -> applyTopAndBottomInsetAsMargin(insets)
                                Adjustment.Height -> error(
                                    "Height adjustment can only be used either Top or Bottom."
                                )
                            }
                        }
                    }
                }
            }

            if (consumeTop || consumeBottom) {
                insets.replaceSystemWindowInsets(
                    insets.systemWindowInsetLeft,
                    if (consumeTop) 0 else insets.systemWindowInsetTop,
                    insets.systemWindowInsetRight,
                    if (consumeBottom) 0 else insets.systemWindowInsetBottom
                )
            } else insets
        }
    }

    private fun View.onApplyWindowInsets(
        block: (insets: WindowInsetsCompat) -> WindowInsetsCompat
    ) {
        if (!edgeToEdge.listening) {
            edgeToEdge.listening = true
            ViewCompat.setOnApplyWindowInsetsListener(this) { _, insets -> block(insets) }
        }

        if (isAttachedToWindow) ViewCompat.requestApplyInsets(this)
        else addOnAttachStateChangeListener(
            object : View.OnAttachStateChangeListener {
                override fun onViewDetachedFromWindow(view: View) {}
                override fun onViewAttachedToWindow(view: View) {
                    view.removeOnAttachStateChangeListener(this)
                    ViewCompat.requestApplyInsets(view)
                }
            }
        )
    }
}

@EdgeToEdgeDsl
class FittingBuilder(
    var adjustment: Adjustment,
    var clipToPadding: Boolean?,
    var consumeInsets: Boolean
)

sealed class Edge {
    object Top : Edge() {
        @Suppress("UNUSED_PARAMETER")
        operator fun plus(bottom: Bottom): Edge = TopBottom
    }

    object Bottom : Edge()
    internal object TopBottom : Edge()
}

enum class Adjustment { Padding, Margin, Height }

internal data class Fitting(
    val view: View,
    val adjustment: Adjustment,
    val edge: Edge,
    val clipToPadding: Boolean?,
    val consumeInsets: Boolean,
    val paddingTop: Int,
    val paddingBottom: Int,
    val marginTop: Int,
    val marginBottom: Int
)

internal data class EdgeToEdge(
    val fittings: WeakHashMap<View, Fitting> = WeakHashMap(),
    var listening: Boolean = false
)

private fun Fitting.applyTopInsetAsPadding(insets: WindowInsetsCompat) {
    val top = paddingTop + insets.systemWindowInsetTop
    if (view.paddingTop != top) view.setPadding(
        view.paddingLeft, top, view.paddingRight, view.paddingBottom
    )
}

private fun Fitting.applyBottomInsetAsPadding(insets: WindowInsetsCompat) {
    val bottom = paddingBottom + insets.systemWindowInsetBottom
    if (view.paddingBottom != bottom) view.setPadding(
        view.paddingLeft, view.paddingTop, view.paddingRight, bottom
    )
}

private fun Fitting.applyTopAndBottomInsetsAsPadding(insets: WindowInsetsCompat) {
    val top = paddingTop + insets.systemWindowInsetTop
    val bottom = paddingBottom + insets.systemWindowInsetBottom
    if (view.paddingTop != top || view.paddingBottom != bottom) view.setPadding(
        view.paddingLeft, top, view.paddingRight, bottom
    )
}

private fun Fitting.applyTopInsetAsMargin(insets: WindowInsetsCompat) {
    val layoutParams = view.layoutParams as ViewGroup.MarginLayoutParams
    val top = marginTop + insets.systemWindowInsetTop
    if (top != layoutParams.topMargin) {
        layoutParams.topMargin = top
        view.layoutParams = layoutParams
    }
}

private fun Fitting.applyBottomInsetAsMargin(insets: WindowInsetsCompat) {
    val layoutParams = view.layoutParams as ViewGroup.MarginLayoutParams
    val bottom = marginBottom + insets.systemWindowInsetBottom
    if (bottom != layoutParams.bottomMargin) {
        layoutParams.bottomMargin = bottom
        view.layoutParams = layoutParams
    }
}

private fun Fitting.applyTopAndBottomInsetAsMargin(insets: WindowInsetsCompat) {
    val layoutParams = view.layoutParams as ViewGroup.MarginLayoutParams
    val top = marginTop + insets.systemWindowInsetTop
    val bottom = marginBottom + insets.systemWindowInsetBottom
    if (top != layoutParams.topMargin || bottom != layoutParams.bottomMargin) {
        layoutParams.topMargin = top
        layoutParams.bottomMargin = bottom
        view.layoutParams = layoutParams
    }
}

private fun Fitting.applyTopInsetAsHeight(insets: WindowInsetsCompat) {
    if (view.height != insets.systemWindowInsetTop) {
        val layoutParams = view.layoutParams
        layoutParams.height = View.MeasureSpec.makeMeasureSpec(
            insets.systemWindowInsetTop, View.MeasureSpec.EXACTLY
        )
        view.layoutParams = layoutParams
    }
}

private fun Fitting.applyBottomInsetAsHeight(insets: WindowInsetsCompat) {
    if (view.height != insets.systemWindowInsetBottom) {
        val layoutParams = view.layoutParams
        layoutParams.height = View.MeasureSpec.makeMeasureSpec(
            insets.systemWindowInsetBottom, View.MeasureSpec.EXACTLY
        )
        view.layoutParams = layoutParams
    }
}
