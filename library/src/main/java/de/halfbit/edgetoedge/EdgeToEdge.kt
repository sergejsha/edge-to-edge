package de.halfbit.edgetoedge

import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Space
import androidx.core.view.ScrollingView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.lang.ref.WeakReference
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
            clipToPadding = if (this is ScrollingView && this is ViewGroup) false else null
        ).also { builder ->

            val edge = block(builder)
            val layoutMargin = layoutParams as? ViewGroup.MarginLayoutParams
            val fitting = Fitting(
                view = WeakReference(this),
                edge = edge,
                adjustment = builder.adjustment,
                clipToPadding = builder.clipToPadding,
                paddingTop = paddingTop,
                paddingBottom = paddingBottom,
                marginTop = layoutMargin?.topMargin ?: 0,
                marginBottom = layoutMargin?.bottomMargin ?: 0
            )

            builder.clipToPadding?.let {
                check(this is ViewGroup) {
                    "'clipToPadding' can only be applied to a ViewGroup, actual: $this"
                }
                clipToPadding = it
            }

            edgeToEdge.fittings[this] = fitting
        }
    }

    inline fun View.fitPadding(crossinline block: FittingBuilder.() -> Edge) {
        fit {
            adjustment = Adjustment.Padding
            block()
        }
    }

    inline fun View.fitMargin(crossinline block: FittingBuilder.() -> Edge) {
        fit {
            adjustment = Adjustment.Margin
            block()
        }
    }

    inline fun View.fitHeight(crossinline block: FittingBuilder.() -> Edge) {
        fit {
            adjustment = Adjustment.Height
            block()
        }
    }

    fun View.unfit() {
        edgeToEdge.fittings.remove(this)
    }

    @PublishedApi
    internal fun build() {
        val edgeToEdgeEnabled = window.decorView.systemUiVisibility and
                (View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION) > 0
        if (!edgeToEdgeEnabled) window.setEdgeToEdgeFlags()

        rootView.onApplyWindowInsets { insets ->
            for (fitting in edgeToEdge.fittings.values) {
                val view = fitting.view.get() ?: continue
                with(fitting) {
                    when (edge) {
                        Edge.Top -> {
                            when (adjustment) {
                                Adjustment.Padding -> applyTopInsetAsPadding(insets, view)
                                Adjustment.Margin -> applyTopInsetAsMargin(insets, view)
                                Adjustment.Height -> applyTopInsetAsHeight(insets, view)
                            }
                        }
                        Edge.Bottom -> {
                            when (adjustment) {
                                Adjustment.Padding -> applyBottomInsetAsPadding(insets, view)
                                Adjustment.Margin -> applyBottomInsetAsMargin(insets, view)
                                Adjustment.Height -> applyBottomInsetAsHeight(insets, view)
                            }
                        }
                        Edge.TopBottom -> {
                            when (adjustment) {
                                Adjustment.Padding -> applyTopAndBottomInsetsAsPadding(insets, view)
                                Adjustment.Margin -> applyTopAndBottomInsetsAsMargin(insets, view)
                                Adjustment.Height -> error(
                                    "Height adjustment can only be allied to " +
                                            " either Top or Bottom edge."
                                )
                            }
                        }
                    }
                }
            }
            insets
        }
    }

    private fun View.onApplyWindowInsets(
        block: (insets: WindowInsetsCompat) -> WindowInsetsCompat
    ) {
        if (!edgeToEdge.listening) {
            edgeToEdge.listening = true
            ViewCompat.setOnApplyWindowInsetsListener(this) { _, insets -> block(insets) }
        }
        dispatchWindowInsets()
    }
}

@EdgeToEdgeDsl
class FittingBuilder(
    var adjustment: Adjustment,
    var clipToPadding: Boolean?
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
    val view: WeakReference<View>,
    val adjustment: Adjustment,
    val edge: Edge,
    val clipToPadding: Boolean?,
    val paddingTop: Int,
    val paddingBottom: Int,
    val marginTop: Int,
    val marginBottom: Int
)

internal data class EdgeToEdge(
    val fittings: WeakHashMap<View, Fitting> = WeakHashMap(),
    var listening: Boolean = false
)

internal fun View.dispatchWindowInsets() {
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

private fun Fitting.applyTopInsetAsPadding(insets: WindowInsetsCompat, view: View) {
    val top = paddingTop + insets.systemWindowInsetTop
    if (view.paddingTop != top) view.setPadding(
        view.paddingLeft, top, view.paddingRight, view.paddingBottom
    )
}

private fun Fitting.applyBottomInsetAsPadding(insets: WindowInsetsCompat, view: View) {
    val bottom = paddingBottom + insets.systemWindowInsetBottom
    if (view.paddingBottom != bottom) view.setPadding(
        view.paddingLeft, view.paddingTop, view.paddingRight, bottom
    )
}

private fun Fitting.applyTopAndBottomInsetsAsPadding(insets: WindowInsetsCompat, view: View) {
    val top = paddingTop + insets.systemWindowInsetTop
    val bottom = paddingBottom + insets.systemWindowInsetBottom
    if (view.paddingTop != top || view.paddingBottom != bottom) view.setPadding(
        view.paddingLeft, top, view.paddingRight, bottom
    )
}

private fun Fitting.applyTopInsetAsMargin(insets: WindowInsetsCompat, view: View) {
    val layoutParams = view.layoutParams as ViewGroup.MarginLayoutParams
    val top = marginTop + insets.systemWindowInsetTop
    if (top != layoutParams.topMargin) {
        layoutParams.topMargin = top
        view.layoutParams = layoutParams
    }
}

private fun Fitting.applyBottomInsetAsMargin(insets: WindowInsetsCompat, view: View) {
    val layoutParams = view.layoutParams as ViewGroup.MarginLayoutParams
    val bottom = marginBottom + insets.systemWindowInsetBottom
    if (bottom != layoutParams.bottomMargin) {
        layoutParams.bottomMargin = bottom
        view.layoutParams = layoutParams
    }
}

private fun Fitting.applyTopAndBottomInsetsAsMargin(insets: WindowInsetsCompat, view: View) {
    val layoutParams = view.layoutParams as ViewGroup.MarginLayoutParams
    val top = marginTop + insets.systemWindowInsetTop
    val bottom = marginBottom + insets.systemWindowInsetBottom
    if (top != layoutParams.topMargin || bottom != layoutParams.bottomMargin) {
        layoutParams.topMargin = top
        layoutParams.bottomMargin = bottom
        view.layoutParams = layoutParams
    }
}

private fun applyTopInsetAsHeight(insets: WindowInsetsCompat, view: View) {
    if (view.height != insets.systemWindowInsetTop) {
        val layoutParams = view.layoutParams
        layoutParams.height = View.MeasureSpec.makeMeasureSpec(
            insets.systemWindowInsetTop, View.MeasureSpec.EXACTLY
        )
        view.layoutParams = layoutParams
    }
}

private fun applyBottomInsetAsHeight(insets: WindowInsetsCompat, view: View) {
    if (view.height != insets.systemWindowInsetBottom) {
        val layoutParams = view.layoutParams
        layoutParams.height = View.MeasureSpec.makeMeasureSpec(
            insets.systemWindowInsetBottom, View.MeasureSpec.EXACTLY
        )
        view.layoutParams = layoutParams
    }
}
